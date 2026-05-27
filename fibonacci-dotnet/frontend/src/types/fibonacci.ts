export interface CacheStats {
  hits: number
  misses: number
  total: number
  hitRatio: number
}

export interface AlgorithmResult {
  name: string
  label: string
  timeComplexity: string
  spaceComplexity: string
  n: number
  result: string | null
  timeSeconds: number
  cacheStats: CacheStats | null
  skipped: boolean
  skipReason: string
}

export interface ComputeResponse {
  n: number
  results: AlgorithmResult[]
  allMatch: boolean
  bestName: string
  bestTimeSeconds: number
}

export interface ComputeRequest {
  n: number
  includeNaive: boolean
}
