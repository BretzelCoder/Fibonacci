<script setup lang="ts">
import { ref, computed } from 'vue'

const MAX_N = 5000
const MAX_N_NAIVE = 35
const SLIDER_MAX = 100

const emit = defineEmits<{
  compute: [n: number, includeNaive: boolean]
}>()

defineProps<{ loading: boolean }>()

const n = ref(30)
const includeNaive = ref(true)
const inputError = ref('')

const showSlowWarning = computed(() => n.value > MAX_N_NAIVE && includeNaive.value)
const sliderValue = computed(() => Math.min(n.value, SLIDER_MAX))

function onSlider(e: Event) {
  n.value = Number((e.target as HTMLInputElement).value)
  inputError.value = ''
}

function onNumberInput(e: Event) {
  const raw = (e.target as HTMLInputElement).value
  const v = Number(raw)
  if (!isNaN(v) && Number.isInteger(v) && v >= 0 && v <= MAX_N) {
    n.value = v
    inputError.value = ''
  }
}

function onNumberBlur(e: Event) {
  const raw = (e.target as HTMLInputElement).value
  const v = Number(raw)
  if (isNaN(v) || !Number.isInteger(v) || v < 0 || v > MAX_N) {
    inputError.value = `Enter an integer between 0 and ${MAX_N}.`
    n.value = Math.max(0, Math.min(MAX_N, Math.round(v) || 0))
  }
}

function submit() {
  if (n.value < 0 || n.value > MAX_N) {
    inputError.value = `Enter an integer between 0 and ${MAX_N}.`
    return
  }
  emit('compute', n.value, includeNaive.value)
}
</script>

<template>
  <div class="space-y-3">
    <!-- Slider + number input -->
    <div class="flex items-center gap-4">
      <input
        type="range"
        :min="0"
        :max="SLIDER_MAX"
        :value="sliderValue"
        @input="onSlider"
        class="flex-1 accent-blue-500 cursor-pointer"
      />
      <input
        type="number"
        :value="n"
        :min="0"
        :max="MAX_N"
        @input="onNumberInput"
        @blur="onNumberBlur"
        class="w-24 bg-gray-800 border border-gray-600 rounded-lg px-3 py-1.5 text-center text-sm text-gray-100
               focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500
               [appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none"
      />
    </div>

    <p v-if="inputError" class="text-red-400 text-xs">{{ inputError }}</p>

    <p v-if="showSlowWarning" class="text-amber-400 text-xs leading-relaxed">
      ⚠️ n &gt; {{ MAX_N_NAIVE }} — the naive recursive variant will be automatically skipped (O(2^n) cost).
    </p>

    <!-- Controls row -->
    <div class="flex items-center justify-between">
      <label class="flex items-center gap-2 text-sm text-gray-300 cursor-pointer select-none">
        <input
          type="checkbox"
          v-model="includeNaive"
          class="w-4 h-4 rounded accent-blue-500 cursor-pointer"
        />
        Include naive recursion
      </label>

      <div class="flex items-center gap-3">
        <svg
          v-if="loading"
          class="animate-spin w-5 h-5 text-blue-400"
          fill="none"
          viewBox="0 0 24 24"
        >
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
        </svg>

        <button
          @click="submit"
          :disabled="loading"
          class="flex items-center gap-2 bg-blue-700 hover:bg-blue-600 active:bg-blue-800
                 disabled:opacity-50 disabled:cursor-not-allowed
                 text-white text-sm font-medium px-4 py-2 rounded-lg transition-colors"
        >
          <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 24 24">
            <path d="M8 5v14l11-7z" />
          </svg>
          Compute
        </button>
      </div>
    </div>
  </div>
</template>
