import { describe, it, expect, beforeEach, vi } from 'vitest';
import { AIMemoryClient } from './client.js';
import {
  AuthenticationError,
  NotFoundError,
  ValidationError,
} from './errors.js';

// Mock global do fetch
const fetchMock = vi.fn();
global.fetch = fetchMock;

describe('AIMemoryClient', () => {
  let client: AIMemoryClient;

  beforeEach(() => {
    fetchMock.mockReset();
    client = new AIMemoryClient({
      apiKey: 'amk_test_xxx',
      baseUrl: 'http://localhost:8081',
      maxRetries: 0,   // sem retry nos testes pra ficar rápido
    });
  });

  it('should throw if apiKey is missing', () => {
    expect(() => new AIMemoryClient({ apiKey: '' }))
      .toThrow('apiKey é obrigatória');
  });

  it('should trim trailing slash from baseUrl', () => {
    const c = new AIMemoryClient({ apiKey: 'x', baseUrl: 'http://test.com/' });
    // acessa propriedade privada via cast
    expect((c as any).baseUrl).toBe('http://test.com');
  });

  it('should include Authorization header', async () => {
    fetchMock.mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: async () => ({ ok: true }),
    });

    await client.request('GET', '/test');

    expect(fetchMock).toHaveBeenCalledWith(
      'http://localhost:8081/test',
      expect.objectContaining({
        headers: expect.objectContaining({
          'Authorization': 'Bearer amk_test_xxx',
        }),
      })
    );
  });

  it('should throw AuthenticationError on 401', async () => {
    fetchMock.mockResolvedValueOnce({
      ok: false,
      status: 401,
      json: async () => ({ message: 'Invalid key' }),
    });

    await expect(client.request('GET', '/test'))
      .rejects.toThrow(AuthenticationError);
  });

  it('should throw NotFoundError on 404', async () => {
    fetchMock.mockResolvedValueOnce({
      ok: false,
      status: 404,
      json: async () => ({ message: 'Not found' }),
    });

    await expect(client.request('GET', '/test'))
      .rejects.toThrow(NotFoundError);
  });

  it('should throw ValidationError on 400', async () => {
    fetchMock.mockResolvedValueOnce({
      ok: false,
      status: 400,
      json: async () => ({ message: 'Bad request' }),
    });

    await expect(client.request('POST', '/test', {}))
      .rejects.toThrow(ValidationError);
  });

  it('should return void for 204', async () => {
    fetchMock.mockResolvedValueOnce({
      ok: true,
      status: 204,
    });

    const result = await client.request('DELETE', '/test');
    expect(result).toBeUndefined();
  });
});