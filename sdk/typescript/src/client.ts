import {
  AuthenticationError,
  ForbiddenError,
  NetworkError,
  NotFoundError,
  RateLimitError,
  QuotaExceededError,
  ServerError,
  ValidationError,
} from "./errors.js";
import type { ClientOptions } from "./types.js";
import { AuthResource } from "./resources/auth.js";
import { EndUsersResource } from "./resources/endUsers.js";
import { MemoriesResource } from "./resources/memories.js";
import { UsageResource } from "./resources/usage.js";

const DEFAULT_BASE_URL = "http://localhost:8081";
const DEFAULT_TIMEOUT = 60_000; // 60s
const DEFAULT_MAX_RETRIES = 2;

/** Estrutura bruta dos erros da API. */
interface ErrorBody {
  message: string;
  quotaType?: string;
  current?: number;
  limit?: number;
  plan?: string;
  resetAt?: string;
  upgradeUrl?: string;
}

export class AIMemoryClient {
  readonly auth: AuthResource;
  readonly endUsers: EndUsersResource;
  readonly memories: MemoriesResource;
  readonly usage: UsageResource;

  private readonly apiKey: string;
  private readonly baseUrl: string;
  private readonly timeout: number;
  private readonly maxRetries: number;

  constructor(options: ClientOptions) {
    if (!options.apiKey) {
      throw new Error("apiKey é obrigatória");
    }

    this.apiKey = options.apiKey;
    this.baseUrl = (options.baseUrl ?? DEFAULT_BASE_URL).replace(/\/$/, "");
    this.timeout = options.timeout ?? DEFAULT_TIMEOUT;
    this.maxRetries = options.maxRetries ?? DEFAULT_MAX_RETRIES;

    this.auth = new AuthResource(this);
    this.endUsers = new EndUsersResource(this);
    this.memories = new MemoriesResource(this);
    this.usage = new UsageResource(this);
  }

  /**
   * Faz uma requisição HTTP. Uso interno.
   */
  async request<T>(
    method: "GET" | "POST" | "DELETE" | "PUT" | "PATCH",
    path: string,
    body?: unknown,
    options?: { skipAuth?: boolean },
  ): Promise<T> {
    const url = `${this.baseUrl}${path}`;
    const headers: Record<string, string> = {
      "Content-Type": "application/json",
      Accept: "application/json",
    };

    if (!options?.skipAuth) {
      headers["Authorization"] = `Bearer ${this.apiKey}`;
    }

    const init: RequestInit = {
      method,
      headers,
      body: body ? JSON.stringify(body) : undefined,
    };

    return this.fetchWithRetry<T>(url, init);
  }

  /**
   * Fetch com retry exponencial em erros retornáveis (5xx, 429, timeout).
   */
  private async fetchWithRetry<T>(url: string, init: RequestInit): Promise<T> {
    let lastError: Error | null = null;

    for (let attempt = 0; attempt <= this.maxRetries; attempt++) {
      try {
        const controller = new AbortController();
        const timer = setTimeout(() => controller.abort(), this.timeout);

        const response = await fetch(url, {
          ...init,
          signal: controller.signal,
        });

        clearTimeout(timer);

        // ============ Sucesso ============
        if (response.ok) {
          if (response.status === 204) {
            return undefined as T;
          }
          return (await response.json()) as T;
        }

        // ============ Erros ============
        const errorBody = await this.parseErrorBody(response);

        // 401 — sem retry
        if (response.status === 401) {
          throw new AuthenticationError(errorBody.message);
        }

        // 403 — sem retry
        if (response.status === 403) {
          throw new ForbiddenError(errorBody.message);
        }

        // 404 — sem retry
        if (response.status === 404) {
          throw new NotFoundError(errorBody.message);
        }

        // 400 / 422 — sem retry
        if (response.status === 400 || response.status === 422) {
          throw new ValidationError(errorBody.message, errorBody);
        }

        // 402 — quota excedida (sem retry, não vai adiantar)
        if (response.status === 402) {
          throw new QuotaExceededError(errorBody.message, {
            quotaType:
              (errorBody.quotaType as "memories" | "tokens") ?? "memories",
            current: errorBody.current ?? 0,
            limit: errorBody.limit ?? 0,
            plan: errorBody.plan ?? "UNKNOWN",
            resetAt: errorBody.resetAt ?? "",
            upgradeUrl: errorBody.upgradeUrl ?? "",
          });
        }

        // 429 — retry com backoff
        if (response.status === 429) {
          lastError = new RateLimitError(errorBody.message);
          if (attempt < this.maxRetries) {
            await this.sleep(this.backoff(attempt));
          }
          continue;
        }

        // 5xx — retry com backoff
        if (response.status >= 500) {
          lastError = new ServerError(errorBody.message, response.status);
          if (attempt < this.maxRetries) {
            await this.sleep(this.backoff(attempt));
          }
          continue;
        }

        // Outros 4xx — sem retry
        throw new ServerError(errorBody.message, response.status);
      } catch (err) {
        // Re-throw erros "finais" (não-retry)
        if (
          err instanceof AuthenticationError ||
          err instanceof ForbiddenError ||
          err instanceof NotFoundError ||
          err instanceof ValidationError ||
          err instanceof QuotaExceededError
        ) {
          throw err;
        }

        // Erro de rede / timeout
        if (err instanceof Error && err.name === "AbortError") {
          lastError = new NetworkError(`Timeout após ${this.timeout}ms`);
        } else {
          lastError = new NetworkError(
            err instanceof Error ? err.message : "Erro desconhecido",
            err,
          );
        }

        if (attempt < this.maxRetries) {
          await this.sleep(this.backoff(attempt));
        }
      }
    }

    throw lastError ?? new NetworkError("Requisição falhou após retries");
  }

  private backoff(attempt: number): number {
    return Math.min(1000 * Math.pow(2, attempt), 10_000);
  }

  private sleep(ms: number): Promise<void> {
    return new Promise((resolve) => setTimeout(resolve, ms));
  }

  /**
   * Extrai a mensagem e todos os campos relevantes do erro HTTP.
   */
  private async parseErrorBody(response: Response): Promise<ErrorBody> {
    try {
      const json = await response.json();
      return {
        message: json.message ?? `HTTP ${response.status}`,
        quotaType: json.quotaType,
        current: json.current,
        limit: json.limit,
        plan: json.plan,
        resetAt: json.resetAt,
        upgradeUrl: json.upgradeUrl,
      };
    } catch {
      return { message: `HTTP ${response.status} ${response.statusText}` };
    }
  }
}
