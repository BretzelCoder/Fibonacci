<script setup lang="ts">
import { computed } from 'vue'
import type { AlgorithmResult } from '../types/fibonacci'

const props = defineProps<{ result: AlgorithmResult }>()

// Full Tailwind class strings — never build dynamically to avoid purge issues.
const COLOR_MAP: Record<string, { border: string; text: string; badge: string }> = {
  recursive:      { border: 'border-red-500',    text: 'text-red-400',    badge: 'bg-red-500/10'    },
  memoized:       { border: 'border-blue-500',   text: 'text-blue-400',   badge: 'bg-blue-500/10'   },
  iterative:      { border: 'border-green-500',  text: 'text-green-400',  badge: 'bg-green-500/10'  },
  'fast-doubling':{ border: 'border-purple-500', text: 'text-purple-400', badge: 'bg-purple-500/10' },
}
const FALLBACK = { border: 'border-gray-500', text: 'text-gray-400', badge: 'bg-gray-500/10' }

const color = computed(() => COLOR_MAP[props.result.name] ?? FALLBACK)

function shorten(value: string, maxLen = 22): string {
  return value.length <= maxLen ? value : value.slice(0, maxLen - 1) + '…'
}
</script>

<template>
  <div class="bg-gray-800 rounded-xl p-4 w-52 shadow-md flex-shrink-0 flex flex-col gap-2">
    <!-- Header -->
    <div :class="['border-l-4 pl-3', color.border]">
      <span :class="['font-semibold text-sm', color.text]">{{ result.label }}</span>
    </div>

    <!-- Skipped state -->
    <p v-if="result.skipped" class="text-gray-500 text-xs italic">
      {{ result.skipReason }}
    </p>

    <!-- Computed state -->
    <template v-else>
      <p class="text-gray-400 text-xs">F({{ result.n }})</p>

      <p class="font-bold text-sm break-all select-all" :title="result.result ?? ''">
        {{ shorten(result.result ?? '') }}
      </p>

      <div class="flex items-center gap-1.5 text-xs text-gray-300">
        <svg class="w-3.5 h-3.5 text-gray-400 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        {{ result.timeSeconds.toFixed(6) }} s
      </div>

      <div :class="['text-xs px-2 py-1 rounded-md', color.badge, color.text]">
        Time {{ result.timeComplexity }} &nbsp;·&nbsp; Space {{ result.spaceComplexity }}
      </div>

      <!-- Cache stats (memoized only) -->
      <template v-if="result.cacheStats">
        <hr class="border-gray-700" />
        <div class="flex items-center gap-1.5 text-xs text-green-400">
          <svg class="w-3 h-3 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
          </svg>
          Hits &nbsp; {{ result.cacheStats.hits }}
        </div>
        <div class="flex items-center gap-1.5 text-xs text-red-400">
          <svg class="w-3 h-3 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
          Misses {{ result.cacheStats.misses }}
        </div>
      </template>
    </template>
  </div>
</template>
