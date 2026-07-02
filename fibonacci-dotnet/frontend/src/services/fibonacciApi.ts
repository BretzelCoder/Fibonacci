import type { ComputeRequest, ComputeResponse } from '../types/fibonacci'

const BASE_URL = '/api/fibonacci'

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const payload = await response.json().catch(() => ({})) as Record<string, unknown>
    throw new Error(
      typeof payload['message'] === 'string'
        ? payload['message']
        : `Request failed (HTTP ${response.status})`
    )
  }
  return response.json() as Promise<T>
}

export const fibonacciApi = {
  async compute(request: ComputeRequest): Promise<ComputeResponse> {
    const response = await fetch(`${BASE_URL}/compute`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request),
    })
    return handleResponse<ComputeResponse>(response)
  },
}
