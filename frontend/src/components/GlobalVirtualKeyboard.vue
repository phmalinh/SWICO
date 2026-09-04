<template>
  <div
    v-if="visible"
    data-global-keyboard
    class="fixed inset-x-0 bottom-0 z-[2200] max-h-[46vh] overflow-y-auto border-t border-slate-300 bg-white p-2.5 shadow-2xl md:left-auto md:right-3 md:bottom-3 md:w-[24rem] md:max-w-[calc(100vw-1rem)] md:rounded-lg md:border"
    :style="keyboardPositionStyle"
    @mousedown.prevent
  >
    <div class="mb-2 flex flex-wrap items-center gap-1.5">
      <div
        class="min-w-0 flex-1 cursor-move select-none truncate rounded border border-slate-300 bg-slate-50 px-2.5 py-2 text-sm font-black text-slate-900"
        @pointerdown.stop.prevent="startDrag"
      >
        {{ displayValue }}
      </div>
      <button
        type="button"
        class="rounded border px-3 py-2 text-sm font-black"
        :class="mode === 'text' ? 'border-sky-500 bg-sky-50 text-sky-700' : 'border-slate-300 text-slate-600'"
        @click="switchMode('text')"
      >
        ABC
      </button>
      <button
        type="button"
        class="rounded border px-3 py-2 text-sm font-black"
        :class="mode === 'lower' ? 'border-sky-500 bg-sky-50 text-sky-700' : 'border-slate-300 text-slate-600'"
        @click="switchMode('lower')"
      >
        abc
      </button>
      <button
        type="button"
        class="rounded border px-3 py-2 text-sm font-black"
        :class="mode === 'number' ? 'border-sky-500 bg-sky-50 text-sky-700' : 'border-slate-300 text-slate-600'"
        @click="switchMode('number')"
      >
        123
      </button>
      <button type="button" class="rounded border border-slate-300 px-3 py-2 text-sm font-bold text-slate-600" @click="close">
        {{ t('productionEntry.keypad.ok') }}
      </button>
    </div>

    <div class="space-y-2">
      <div v-for="(row, rowIndex) in currentRows" :key="rowIndex" class="flex justify-center gap-1">
        <button
          v-for="key in row"
          :key="key"
          type="button"
          class="h-9 min-w-7 rounded border border-slate-300 bg-slate-50 px-1.5 text-sm font-black text-slate-900 active:bg-sky-100 sm:h-9 sm:min-w-8 sm:px-2"
          @click="pressKey(key)"
        >
          {{ key }}
        </button>
      </div>
      <div class="grid grid-cols-3 gap-2">
        <button type="button" class="h-10 rounded border border-rose-200 bg-rose-50 text-sm font-black text-rose-700 active:bg-rose-100" @click="clearValue">
          {{ t('productionEntry.keypad.clear') }}
        </button>
        <button type="button" class="h-10 rounded border border-slate-300 bg-slate-50 text-sm font-black text-slate-700 active:bg-slate-100" @click="pressKey(' ')">
          {{ t('productionEntry.keypad.space') }}
        </button>
        <button type="button" class="h-10 rounded border border-slate-300 bg-slate-50 text-sm font-black text-slate-700 active:bg-slate-100" @click="backspace">
          {{ t('productionEntry.keypad.backspace') }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import { useI18n } from '@/i18n'

const { t } = useI18n()

const textRows = [
  ['Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P'],
  ['A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L'],
  ['Z', 'X', 'C', 'V', 'B', 'N', 'M', '-', '/', '.'],
]
const lowerRows = textRows.map(row => row.map(key => key.length === 1 ? key.toLowerCase() : key))
const numberRows = [
  ['1', '2', '3'],
  ['4', '5', '6'],
  ['7', '8', '9'],
  ['-', '0', '/'],
]

const visible = ref(false)
const activeInput = ref(null)
const customTarget = ref(null)
const mode = ref('text')
const position = ref(null)
const activeDrag = ref(null)
const currentValue = ref('')

const currentRows = computed(() => {
  if (mode.value === 'number') return numberRows
  if (mode.value === 'lower') return lowerRows
  return textRows
})

const displayValue = computed(() => {
  if (customTarget.value) {
    if (customTarget.value.masked) return currentValue.value ? '•'.repeat(currentValue.value.length) : customTarget.value.placeholder || ''
    return currentValue.value || customTarget.value.placeholder || t('common.search')
  }
  const input = activeInput.value
  if (!input) return ''
  if (input.type === 'password') return currentValue.value ? '•'.repeat(currentValue.value.length) : input.placeholder || t('common.password')
  return currentValue.value || input.placeholder || input.getAttribute('aria-label') || t('common.search')
})

const keyboardPositionStyle = computed(() => {
  if (!position.value) return {}
  return {
    left: `${position.value.x}px`,
    top: `${position.value.y}px`,
    right: 'auto',
    bottom: 'auto',
    width: `${position.value.width}px`,
  }
})

function shouldUseKeyboard(input) {
  if (!(input instanceof HTMLInputElement || input instanceof HTMLTextAreaElement)) return false
  if (input.closest('[data-global-keyboard]')) return false
  if (input.closest('[data-no-global-keyboard="true"]')) return false
  if (input.closest('.el-date-editor')) return false
  if (input.disabled || input.readOnly) return false
  if (input.type && ['button', 'checkbox', 'color', 'date', 'file', 'hidden', 'radio', 'range', 'submit', 'time'].includes(input.type)) return false
  return true
}

function openForInput(input) {
  if (!shouldUseKeyboard(input)) return
  if (visible.value && input === activeInput.value && !customTarget.value) return
  customTarget.value = null
  activeInput.value = input
  currentValue.value = input.value || ''
  visible.value = true
  mode.value = shouldStartNumberMode(input) ? 'number' : 'text'
}

function openForCustomTarget(options = {}) {
  customTarget.value = options
  activeInput.value = null
  currentValue.value = String(options.value ?? '')
  visible.value = true
  mode.value = options.mode || (options.numeric ? 'number' : 'text')
}

function shouldStartNumberMode(input) {
  return input.type === 'number'
    || input.inputMode === 'numeric'
    || input.inputMode === 'decimal'
    || input.closest('.el-input-number')
}

function close() {
  commitActiveSelectValue()
  customTarget.value?.onCommit?.(currentValue.value)
  visible.value = false
  customTarget.value?.onClose?.()
}

function commitActiveSelectValue() {
  const input = activeInput.value
  if (!input || !isElementSelectInput(input) || !currentValue.value) return
  setInputValue(currentValue.value)
  input.dispatchEvent(new KeyboardEvent('keydown', {
    bubbles: true,
    cancelable: true,
    key: 'Enter',
    code: 'Enter',
  }))
}

function switchMode(nextMode) {
  mode.value = nextMode
  if (activeInput.value && isElementSelectInput(activeInput.value)) {
    setInputValue(currentValue.value || '')
  }
}

function setInputValue(value, selectionStart = value.length, selectionEnd = selectionStart) {
  if (customTarget.value) {
    const nextValue = normalizeCustomValue(value)
    currentValue.value = nextValue
    customTarget.value.onInput?.(nextValue)
    return
  }
  const input = activeInput.value
  if (!input) return
  const nextValue = String(value ?? '')
  input.value = value
  currentValue.value = nextValue
  input.dispatchEvent(new InputEvent('input', { bubbles: true, data: nextValue, inputType: 'insertText' }))
  input.dispatchEvent(new Event('change', { bubbles: true }))
  nextTick(() => {
    input.value = nextValue
    input.focus()
    try {
      input.setSelectionRange(selectionStart, selectionEnd)
    } catch {
      // Number inputs do not support text selection in every browser.
    }
  })
}

function pressKey(key) {
  if (customTarget.value) {
    setInputValue(`${currentValue.value || ''}${key}`)
    return
  }
  const input = activeInput.value
  if (!input) return
  if (isElementSelectInput(input)) {
    setInputValue(`${currentValue.value || ''}${key}`)
    return
  }
  const start = input.selectionStart ?? input.value.length
  const end = input.selectionEnd ?? start
  const value = `${input.value.slice(0, start)}${key}${input.value.slice(end)}`
  const cursor = start + key.length
  setInputValue(value, cursor)
}

function backspace() {
  if (customTarget.value) {
    setInputValue(String(currentValue.value || '').slice(0, -1))
    return
  }
  const input = activeInput.value
  if (!input) return
  if (isElementSelectInput(input)) {
    setInputValue(String(currentValue.value || '').slice(0, -1))
    return
  }
  const start = input.selectionStart ?? input.value.length
  const end = input.selectionEnd ?? start
  if (start === 0 && end === 0) return
  const deleteStart = start === end ? Math.max(start - 1, 0) : start
  const value = `${input.value.slice(0, deleteStart)}${input.value.slice(end)}`
  setInputValue(value, deleteStart)
}

function clearValue() {
  setInputValue('', 0)
}

function isElementSelectInput(input) {
  return Boolean(input?.closest?.('.el-select'))
}

function normalizeCustomValue(value) {
  const text = String(value ?? '')
  if (!customTarget.value?.numeric) return text
  const digits = text.replace(/\D/g, '')
  const normalized = digits.replace(/^0+(?=\d)/, '')
  const max = Number(customTarget.value.max ?? 999999)
  const number = normalized ? Math.min(Number(normalized), max) : 0
  return number > 0 ? String(number) : ''
}

function syncActiveValue(event) {
  if (event.target === activeInput.value) {
    if (isElementSelectInput(event.target)) return
    currentValue.value = event.target.value || ''
  }
}

function handleCustomOpen(event) {
  openForCustomTarget(event.detail || {})
}

function handleCustomUpdate(event) {
  const detail = event.detail || {}
  if (!customTarget.value || (detail.id && customTarget.value.id !== detail.id)) return
  currentValue.value = String(detail.value ?? '')
  if (detail.placeholder !== undefined) customTarget.value.placeholder = detail.placeholder
  if (detail.mode) mode.value = detail.mode
}

function handleFocusIn(event) {
  openForInput(event.target)
}

function handlePointerDown(event) {
  if (event.target.closest('[data-global-keyboard]')) return
  if (shouldUseKeyboard(event.target)) return
  close()
}

function startDrag(event) {
  if (event.pointerType === 'mouse' && event.button !== 0) return
  const panel = event.currentTarget?.closest?.('[data-global-keyboard]')
  if (!panel) return
  const rect = panel.getBoundingClientRect()
  activeDrag.value = {
    offsetX: event.clientX - rect.left,
    offsetY: event.clientY - rect.top,
    width: rect.width,
    height: rect.height,
  }
  position.value = {
    x: rect.left,
    y: rect.top,
    width: rect.width,
  }
  window.addEventListener('pointermove', handleDrag)
  window.addEventListener('pointerup', stopDrag, { once: true })
  window.addEventListener('pointercancel', stopDrag, { once: true })
}

function handleDrag(event) {
  const drag = activeDrag.value
  if (!drag) return
  const margin = 8
  const maxX = Math.max(window.innerWidth - drag.width - margin, margin)
  const maxY = Math.max(window.innerHeight - drag.height - margin, margin)
  position.value = {
    x: Math.min(Math.max(event.clientX - drag.offsetX, margin), maxX),
    y: Math.min(Math.max(event.clientY - drag.offsetY, margin), maxY),
    width: drag.width,
  }
}

function stopDrag() {
  activeDrag.value = null
  window.removeEventListener('pointermove', handleDrag)
  window.removeEventListener('pointerup', stopDrag)
  window.removeEventListener('pointercancel', stopDrag)
}

onMounted(() => {
  document.addEventListener('focusin', handleFocusIn)
  document.addEventListener('input', syncActiveValue, true)
  document.addEventListener('pointerdown', handlePointerDown)
  window.addEventListener('global-virtual-keyboard:open', handleCustomOpen)
  window.addEventListener('global-virtual-keyboard:update', handleCustomUpdate)
})

onUnmounted(() => {
  document.removeEventListener('focusin', handleFocusIn)
  document.removeEventListener('input', syncActiveValue, true)
  document.removeEventListener('pointerdown', handlePointerDown)
  window.removeEventListener('global-virtual-keyboard:open', handleCustomOpen)
  window.removeEventListener('global-virtual-keyboard:update', handleCustomUpdate)
  stopDrag()
})
</script>
