import { ref } from 'vue'
import { fibonacciApi } from '../services/fibonacciApi'
import type { ComputeResponse } from '../types/fibonacci'

export function useFibonacci() {
  const response = ref<ComputeResponse | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function compute(n: number, includeNaive: boolean): Promise<void> {
    loading.value = true
    error.value = null
    try {
      response.value = await fibonacciApi.compute({ n, includeNaive })
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'An unexpected error occurred.'
      response.value = null
    } finally {
      loading.value = false
    }
  }

  return { response, loading, error, compute }
}
