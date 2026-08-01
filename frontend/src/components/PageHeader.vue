<template>
  <div class="mb-6">
    <div class="flex flex-col gap-4 rounded-lg border border-slate-200 bg-white px-5 py-4 shadow-sm sm:flex-row sm:items-center sm:justify-between">
      <div>
        <p v-if="eyebrow" class="mb-1 text-xs font-bold uppercase tracking-wider text-sky-600">{{ eyebrow }}</p>
        <h1 class="text-2xl font-extrabold tracking-tight text-slate-900">{{ title }}</h1>
        <p v-if="subtitle" class="mt-1 text-sm text-slate-500">{{ subtitle }}</p>
      </div>
      <div v-if="$slots.actions" class="flex shrink-0 items-center gap-2">
        <slot name="actions" />
      </div>
    </div>
    <div v-if="$slots.default" class="mt-4">
      <slot />
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: { type: String, required: true },
  subtitle: { type: String, default: '' },
  eyebrow: { type: String, default: '' },
})
</script>
