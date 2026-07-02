<script setup lang="ts">
defineProps<{ name: string; timeSeconds: number }>()
</script>

<template>
  <div class="flex items-center gap-3 px-5 py-4 bg-amber-900/20 border border-amber-700/40 rounded-xl">
    <svg class="w-7 h-7 text-amber-400 flex-shrink-0" fill="currentColor" viewBox="0 0 24 24">
      <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" />
    </svg>
    <div>
      <span class="text-sm font-semibold text-amber-200">Fastest: {{ name }}</span>
      <span class="text-xs text-amber-400 ml-2 tabular-nums">{{ timeSeconds.toFixed(6) }} s</span>
    </div>
  </div>
</template>
