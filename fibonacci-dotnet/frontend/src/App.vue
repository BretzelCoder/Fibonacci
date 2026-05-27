<script setup lang="ts">
import { computed } from 'vue'
import { useFibonacci } from './composables/useFibonacci'
import InputPanel from './components/InputPanel.vue'
import AlgorithmCard from './components/AlgorithmCard.vue'
import PerformanceBars from './components/PerformanceBars.vue'
import CacheVisualizer from './components/CacheVisualizer.vue'
import WinnerBanner from './components/WinnerBanner.vue'
import type { AlgorithmResult } from './types/fibonacci'

const { response, loading, error, compute } = useFibonacci()

const computedResults = computed<AlgorithmResult[]>(() =>
  response.value?.results.filter(r => !r.skipped) ?? []
)

const memoizedResult = computed<AlgorithmResult | undefined>(() =>
  response.value?.results.find(r => r.name === 'memoized' && !r.skipped)
)
</script>

<template>
  <div class="min-h-screen bg-gray-900 text-gray-100">
    <div class="max-w-5xl mx-auto px-7 py-10">

      <!-- Header -->
      <h1 class="text-3xl font-bold tracking-tight">Fibonacci</h1>
      <p class="text-gray-400 text-sm mt-1">
        Algorithm Comparison — .NET 8 · Cache · Recursion · Iteration · Fast Doubling
      </p>

      <hr class="border-gray-700 my-6" />

      <!-- Input -->
      <InputPanel :loading="loading" @compute="compute" />

      <!-- Error -->
      <div
        v-if="error"
        class="mt-4 flex items-start gap-2 p-3 bg-red-900/30 border border-red-700 rounded-lg text-red-300 text-sm"
      >
        <svg class="w-4 h-4 mt-0.5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        {{ error }}
      </div>

      <template v-if="response">
        <hr class="border-gray-700 my-6" />

        <!-- Algorithm cards -->
        <div class="flex flex-wrap gap-3">
          <AlgorithmCard
            v-for="result in response.results"
            :key="result.name"
            :result="result"
          />
        </div>

        <!-- Integrity check -->
        <div class="flex items-center gap-2 mt-5">
          <template v-if="response.allMatch">
            <svg class="w-5 h-5 text-green-400 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <span class="text-sm text-gray-300">All computed results are identical — integrity check passed.</span>
          </template>
          <template v-else>
            <svg class="w-5 h-5 text-red-400 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <span class="text-sm text-red-300">Integrity check failed — results do not match!</span>
          </template>
        </div>

        <!-- Performance bars -->
        <template v-if="computedResults.length > 0">
          <hr class="border-gray-700 my-6" />
          <PerformanceBars :results="computedResults" />
        </template>

        <!-- Cache visualizer -->
        <template v-if="memoizedResult">
          <hr class="border-gray-700 my-6" />
          <CacheVisualizer :result="memoizedResult" />
        </template>

        <!-- Winner -->
        <hr class="border-gray-700 my-6" />
        <WinnerBanner :name="response.bestName" :time-seconds="response.bestTimeSeconds" />
      </template>

    </div>
  </div>
</template>
