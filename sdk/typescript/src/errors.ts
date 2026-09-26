/**
 * Erros customizados do SDK.
 */


import type { QuotaExceededDetails } from './types.js';

export class AIMemoryError extends Error {
  constructor(message: string) {
    super(message);
    this.name = 'AIMemoryError';
  }
}

export class AuthenticationError extends AIMemoryError {
  constructor(message = 'API key inválida ou ausente') {
    super(message);
    this.name = 'AuthenticationError';
  }
}

export class ForbiddenError extends AIMemoryError {
  constructor(message = 'Acesso negado') {
    super(message);
    this.name = 'ForbiddenError';
  }
}

export class NotFoundError extends AIMemoryError {
  constructor(message = 'Recurso não encontrado') {
    super(message);
    this.name = 'NotFoundError';
  }
}

export class ValidationError extends AIMemoryError {
  constructor(message: string, public readonly details?: unknown) {
    super(message);
    this.name = 'ValidationError';
  }
}

export class RateLimitError extends AIMemoryError {
  constructor(message = 'Rate limit atingido') {
    super(message);
    this.name = 'RateLimitError';
  }
}

export class ServerError extends AIMemoryError {
  constructor(message: string, public readonly statusCode: number) {
    super(message);
    this.name = 'ServerError';
  }
}

export class NetworkError extends AIMemoryError {
  constructor(message: string, public readonly cause?: unknown) {
    super(message);
    this.name = 'NetworkError';
  }
}

export class QuotaExceededError extends AIMemoryError {
  constructor(
    message: string,
    public readonly details: QuotaExceededDetails
  ) {
    super(message);
    this.name = 'QuotaExceededError';
  }
}