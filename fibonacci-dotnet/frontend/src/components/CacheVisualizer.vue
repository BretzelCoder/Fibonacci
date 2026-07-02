<script setup lang="ts">
import type { AlgorithmResult } from '../types/fibonacci'

defineProps<{ result: AlgorithmResult }>()
</script>

<template>
  <section v-if="result.cacheStats">
    <h2 class="text-base font-semibold text-gray-100 mb-1">
      Cache Statistics
      <span class="text-xs font-normal text-gray-400 ml-1">(IMemoryCache — memoized variant)</span>
    </h2>
    <p class="text-xs text-gray-500 mb-4">
      Total calls: {{ result.cacheStats.total }}
      &nbsp;·&nbsp;
      Hit ratio: {{ (result.cacheStats.hitRatio * 100).toFixed(1) }}%
    </p>

    <div class="space-y-3">
      <!-- Hits row -->
      <div class="flex items-center gap-3">
        <span class="text-xs text-green-400 w-16 flex-shrink-0">Hits</span>
        <div class="flex-1 bg-gray-700 rounded-full h-4 overflow-hidden">
          <div
            class="h-full rounded-full bg-green-500 transition-all duration-700"
            :style="{ width: `${result.cacheStats.hitRatio * 100}%` }"
          />
        </div>
        <span class="text-xs text-gray-400 w-36 text-right flex-shrink-0 tabular-nums">
          {{ result.cacheStats.hits }}
          ({{ (result.cacheStats.hitRatio * 100).toFixed(0) }}%)
        </span>
      </div>

      <!-- Misses row -->
      <div class="flex items-center gap-3">
        <span class="text-xs text-red-400 w-16 flex-shrink-0">Misses</span>
        <div class="flex-1 bg-gray-700 rounded-full h-4 overflow-hidden">
          <div
            class="h-full rounded-full bg-red-500 transition-all duration-700"
            :style="{ width: `${(1 - result.cacheStats.hitRatio) * 100}%` }"
          />
        </div>
        <span class="text-xs text-gray-400 w-36 text-right flex-shrink-0 tabular-nums">
          {{ result.cacheStats.misses }}
          ({{ ((1 - result.cacheStats.hitRatio) * 100).toFixed(0) }}%)
        </span>
      </div>
    </div>
  </section>
</template>
