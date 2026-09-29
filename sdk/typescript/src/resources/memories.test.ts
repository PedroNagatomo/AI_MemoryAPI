import { describe, it, expect, beforeEach, vi } from 'vitest';
import { AIMemoryClient } from '../client.js';

const fetchMock = vi.fn();
global.fetch = fetchMock;

describe('MemoriesResource', () => {
  let client: AIMemoryClient;

  beforeEach(() => {
    fetchMock.mockReset();
    client = new AIMemoryClient({
      apiKey: 'amk_test_xxx',
      baseUrl: 'http://localhost:8081',
      maxRetries: 0,
    });
  });

  it('should call ingest endpoint', async () => {
    const mockResponse = {
      extracted: 2,
      persisted: 2,
      tokensUsed: 1000,
      memories: [],
    };
    fetchMock.mockResolvedValueOnce({
      ok: true,
      status: 201,
      json: async () => mockResponse,
    });

    const result = await client.memories.ingest({
      endUserId: 'uuid-123',
      messages: [{ role: 'user', content: 'Hi' }],
    });

    expect(result.extracted).toBe(2);
    expect(fetchMock).toHaveBeenCalledWith(
      'http://localhost:8081/v1/memories/ingest',
      expect.objectContaining({ method: 'POST' })
    );
  });

  it('should call search endpoint', async () => {
    fetchMock.mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: async () => ({ query: 'cafe', count: 0, memories: [] }),
    });

    const result = await client.memories.search({
      endUserId: 'uuid-123',
      query: 'cafe',
    });

    expect(result.count).toBe(0);
  });

  it('should build query string for list', async () => {
    fetchMock.mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: async () => [],
    });

    await client.memories.list('uuid-123', { limit: 5, offset: 10 });

    const calledUrl = fetchMock.mock.calls[0][0];
    expect(calledUrl).toContain('endUserId=uuid-123');
    expect(calledUrl).toContain('limit=5');
    expect(calledUrl).toContain('offset=10');
  });
});