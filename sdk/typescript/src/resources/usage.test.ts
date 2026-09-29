import { describe, it, expect, beforeEach, vi } from "vitest";
import { AIMemoryClient } from "../client.js";

const fetchMock = vi.fn();
global.fetch = fetchMock;

describe("UsageResource", () => {
  let client: AIMemoryClient;

  beforeEach(() => {
    fetchMock.mockReset();
    client = new AIMemoryClient({
      apiKey: "amk_test_xxx",
      baseUrl: "http://localhost:8081",
      maxRetries: 0,
    });
  });

  it("should fetch usage from /v1/usage", async () => {
    const mockUsage = {
      period: "2026-09",
      plan: "FREE",
      memoriesUsed: 10,
      memoriesQuota: 1000,
      memoriesPercent: 0.01,
      tokensUsed: 5000,
      tokensQuota: 50000,
      tokensPercent: 0.1,
      extractionsCount: 5,
      searchesCount: 2,
      apiCallsCount: 15,
      resetAt: "2026-10-01T00:00:00",
    };
    fetchMock.mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: async () => mockUsage,
    });

    const result = await client.usage.get();

    expect(result.plan).toBe("FREE");
    expect(result.memoriesUsed).toBe(10);
    expect(result.tokensUsed).toBe(5000);
  });

  it("should call correct endpoint", async () => {
    fetchMock.mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: async () => ({}),
    });

    await client.usage.get();

    expect(fetchMock).toHaveBeenCalledWith(
      "http://localhost:8081/v1/usage",
      expect.objectContaining({
        method: "GET",
        headers: expect.objectContaining({
          Authorization: "Bearer amk_test_xxx",
        }),
      }),
    );
  });

  it("should send Authorization header", async () => {
    fetchMock.mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: async () => ({}),
    });

    await client.usage.get();

    const callHeaders = fetchMock.mock.calls[0][1].headers;
    expect(callHeaders["Authorization"]).toBe("Bearer amk_test_xxx");
  });
});
