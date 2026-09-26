import type { AIMemoryClient } from '../client.js';
import type { RegisterTenantRequest, RegisterTenantResponse } from '../types.js';

export class AuthResource {
  constructor(private readonly client: AIMemoryClient) {}

  /**
   * Registra um novo tenant e retorna a API key.
   *
   * ⚠️ A API key é mostrada APENAS UMA VEZ.
   */
  register(data: RegisterTenantRequest): Promise<RegisterTenantResponse> {
    return this.client.request<RegisterTenantResponse>(
      'POST',
      '/v1/auth/register',
      data,
      { skipAuth: true }
    );
  }
}