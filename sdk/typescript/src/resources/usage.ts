import type { AIMemoryClient } from '../client.js';
import type { UsageResponse } from '../types.js';

export class UsageResource {
  constructor(private readonly client: AIMemoryClient) {}

  /**
   * Consulta uso atual do tenant (mês corrente + quotas do plano).
   */
  get(): Promise<UsageResponse> {
    return this.client.request<UsageResponse>('GET', '/v1/usage');
  }
}