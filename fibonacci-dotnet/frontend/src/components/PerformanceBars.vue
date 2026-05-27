<script setup lang="ts">
import { computed } from 'vue'
import type { AlgorithmResult } from '../types/fibonacci'

const props = defineProps<{ results: AlgorithmResult[] }>()

// Full class strings to avoid Tailwind purge stripping dynamic variants.
const BAR_COLOR: Record<string, string> = {
  recursive:       'bg-red-500',
  memoized:        'bg-blue-500',
  iterative:       'bg-green-500',
  'fast-doubling': 'bg-purple-500',
}

const maxTime = computed(() =>
  Math.max(...props.results.map(r => r.timeSeconds), Number.EPSILON)
)

function barWidth(timeSeconds: number): string {
  return `${(timeSeconds / maxTime.value) * 100}%`
}
</script>

<template>
  <section>
    <h2 class="text-base font-semibold text-gray-100 mb-4">Relative Performance</h2>
    <div class="space-y-3">
      <div
        v-for="r in results"
        :key="r.name"
        class="flex items-center gap-3"
      >
        <span class="text-xs text-gray-300 w-44 truncate flex-shrink-0">{{ r.label }}</span>
        <div class="flex-1 bg-gray-700 rounded-full h-4 overflow-hidden">
          <div
            :class="['h-full rounded-full transition-all duration-700', BAR_COLOR[r.name] ?? 'bg-gray-400']"
            :style="{ width: barWidth(r.timeSeconds) }"
          />
        </div>
        <span class="text-xs text-gray-400 w-28 text-right flex-shrink-0 tabular-nums">
          {{ r.timeSeconds.toFixed(6) }} s
        </span>
      </div>
    </div>
  </section>
</template>
