import type { AIMemoryClient } from '../client.js';
import type { CreateEndUserRequest, EndUserResponse } from '../types.js';

export class EndUsersResource {
  constructor(private readonly client: AIMemoryClient) {}

  /**
   * Cria ou busca um end-user (idempotente por externalId).
   */
  create(data: CreateEndUserRequest): Promise<EndUserResponse> {
    return this.client.request<EndUserResponse>('POST', '/v1/end-users', data);
  }

  /**
   * Lista end-users do tenant.
   */
  list(): Promise<EndUserResponse[]> {
    return this.client.request<EndUserResponse[]>('GET', '/v1/end-users');
  }
}