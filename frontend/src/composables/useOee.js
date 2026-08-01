export function calculateOee({ operatingMinutes, downtimeMinutes, inputQty, goodQty, defectQty, cycleTimeSeconds, standardMinutes }) {
  const runTime = operatingMinutes || 0
  const downTime = downtimeMinutes || 0
  const input = inputQty || 0
  const good = goodQty || 0
  const defect = defectQty || 0
  const ct = cycleTimeSeconds || 0
  const stdMin = standardMinutes || 480

  const availability = runTime > 0 ? (runTime - downTime) / runTime : 0
  const targetQty = ct > 0 && stdMin > 0 ? (stdMin * 60) / ct : 0
  const performance = targetQty > 0 ? input / targetQty : 0
  const quality = input > 0 ? good / input : 0
  const oee = availability * performance * quality

  return {
    availability: Math.min(Math.max(availability, 0), 1),
    performance: Math.min(Math.max(performance, 0), 1),
    quality: Math.min(Math.max(quality, 0), 1),
    oee: Math.min(Math.max(oee, 0), 1),
    targetQty: Math.round(targetQty * 100) / 100,
  }
}

export function formatPercent(value) {
  return `${(value * 100).toFixed(1)}%`
}

export function getOeeColor(oee) {
  if (oee >= 0.85) return { bg: 'bg-emerald-500', text: 'text-emerald-600', ring: 'ring-emerald-200', label: 'Tốt' }
  if (oee >= 0.65) return { bg: 'bg-amber-500', text: 'text-amber-600', ring: 'ring-amber-200', label: 'Trung bình' }
  return { bg: 'bg-rose-500', text: 'text-rose-600', ring: 'ring-rose-200', label: 'Cảnh báo' }
}
