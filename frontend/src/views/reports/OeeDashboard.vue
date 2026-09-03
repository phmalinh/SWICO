<template>
  <div>
    <PageHeader
      :eyebrow="t('reports.dashboard.eyebrow')"
      :title="t('reports.dashboard.pageTitle')"
      :subtitle="t('reports.dashboard.pageSubtitle')"
    >
      <template #actions>
        <el-tag type="success" effect="dark" size="large">
          <span class="mr-2 inline-block h-2 w-2 animate-pulse rounded-full bg-white"></span>
          {{ t('reports.dashboard.live') }}
        </el-tag>
      </template>
    </PageHeader>

    <section class="mb-5 rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
      <div class="grid grid-cols-1 gap-4 md:grid-cols-3">
        <el-form-item :label="t('reports.dashboard.filters.dateFrom')" class="!mb-0">
          <el-date-picker
            v-model="filters.dateFrom"
            type="date"
            value-format="YYYY-MM-DD"
            class="!w-full"
            @change="loadDashboard"
          />
        </el-form-item>
        <el-form-item :label="t('reports.dashboard.filters.dateTo')" class="!mb-0">
          <el-date-picker
            v-model="filters.dateTo"
            type="date"
            value-format="YYYY-MM-DD"
            class="!w-full"
            @change="loadDashboard"
          />
        </el-form-item>
        <el-form-item :label="t('reports.dashboard.filters.line')" class="!mb-0">
          <el-select
            v-model="filters.lineCode"
            clearable
            filterable
            class="w-full"
            :placeholder="t('reports.dashboard.filters.all')"
            @change="loadDashboard"
          >
            <el-option :label="t('reports.dashboard.filters.all')" value="" />
            <el-option v-for="line in lines" :key="line.lineCode" :label="line.lineCode" :value="line.lineCode" />
          </el-select>
        </el-form-item>
      </div>
    </section>

    <div class="mb-5 grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
      <div v-for="stat in summaryStats" :key="stat.label" class="stat-card">
        <div class="flex items-center justify-between">
          <span class="text-xs font-black uppercase tracking-wider text-slate-500">{{ stat.label }}</span>
          <component :is="stat.icon" class="h-5 w-5" :class="stat.iconColor" />
        </div>
        <div class="mt-2 text-3xl font-black" :class="stat.valueColor">{{ stat.value }}</div>
        <div class="text-xs font-semibold text-slate-400">{{ stat.sub }}</div>
      </div>
    </div>

    <div class="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
      <article
        v-for="line in lineDetails"
        :key="line.code"
        class="rounded-lg border bg-white p-4 shadow-sm transition hover:shadow-md"
        :class="line.borderClass"
      >
        <div class="mb-4 flex items-center justify-between">
          <div>
            <div class="text-xs font-black uppercase tracking-wider text-slate-400">{{ t('reports.dashboard.sections.lineDetails') }}</div>
            <div class="text-2xl font-black text-slate-900">{{ line.code }}</div>
          </div>
          <el-tag :type="line.tagType" effect="dark">{{ line.status }}</el-tag>
        </div>
        <div class="mb-4 text-5xl font-black" :class="line.oeeColor">{{ line.oee }}%</div>
        <div class="space-y-3">
          <div v-for="metric in line.metrics" :key="metric.label">
            <div class="mb-1 flex justify-between text-xs font-bold text-slate-500">
              <span>{{ metric.label }}</span>
              <span>{{ metric.value }}%</span>
            </div>
            <div class="h-2.5 overflow-hidden rounded-full bg-slate-100">
              <div class="h-full rounded-full" :class="metric.barColor" :style="{ width: metric.value + '%' }"></div>
            </div>
          </div>
        </div>
      </article>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onBeforeUnmount, ref } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent, MarkLineComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import { Activity, Gauge, TrendingUp, AlertTriangle } from 'lucide-vue-next'
import PageHeader from '@/components/PageHeader.vue'
import { masterApi, productionApi } from '@/services/api'
import { useI18n } from '@/i18n'

use([CanvasRenderer, BarChart, LineChart, GridComponent, TooltipComponent, LegendComponent, MarkLineComponent])

const { t } = useI18n()

const dashboard = ref(null)
const loading = ref(false)
const lines = ref([])
let refreshTimer = null

function localTodayString() {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const filters = ref({
  dateFrom: localTodayString(),
  dateTo: localTodayString(),
  lineCode: '',
})

const summaryStats = computed(() => {
  const avgOee = dashboard.value?.averageOee ?? 0
  const avgQuality = dashboard.value?.averageQuality ?? 0
  const activeLines = dashboard.value?.activeLines ?? 0
  const warningCount = dashboard.value?.warningCount ?? 0

  return [
    { label: t('reports.dashboard.stats.avgOee'), value: `${(Number(avgOee) * 100).toFixed(1)}%`, sub: t('reports.dashboard.labels.wholeFactory'), icon: Gauge, iconColor: 'text-sky-500', valueColor: 'text-sky-600' },
    { label: t('reports.dashboard.stats.activeLines'), value: `${activeLines}/${activeLines}`, sub: t('reports.dashboard.labels.production'), icon: Activity, iconColor: 'text-emerald-500', valueColor: 'text-emerald-600' },
    { label: t('reports.dashboard.stats.avgQuality'), value: `${(Number(avgQuality) * 100).toFixed(1)}%`, sub: t('reports.dashboard.labels.today'), icon: TrendingUp, iconColor: 'text-indigo-500', valueColor: 'text-indigo-600' },
    { label: t('reports.dashboard.stats.warnings'), value: String(warningCount), sub: t('reports.dashboard.labels.belowTarget'), icon: AlertTriangle, iconColor: 'text-rose-500', valueColor: 'text-rose-600' },
  ]
})

const lineChartOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 46, right: 18, top: 28, bottom: 34 },
  xAxis: { type: 'category', data: dashboard.value?.lines?.map(l => l.lineCode) ?? [], axisLabel: { fontWeight: 800 } },
  yAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%' } },
  series: [{
    name: 'OEE',
    type: 'bar',
    barWidth: 42,
    data: (dashboard.value?.lines ?? []).map(l => ({
      value: (Number(l.oee || 0) * 100).toFixed(1),
      itemStyle: { color: l.oee >= 0.85 ? '#10b981' : l.oee >= 0.65 ? '#f59e0b' : '#f43f5e', borderRadius: [6, 6, 0, 0] },
    })),
    markLine: { data: [{ yAxis: 85, label: { formatter: `${t('reports.dashboard.labels.target')} 85%` }, lineStyle: { color: '#0284c7', type: 'dashed' } }] },
  }],
}))

const trendChartOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 46, right: 18, top: 28, bottom: 34 },
  xAxis: { type: 'category', data: ['06:00', '08:00', '10:00', '12:00', '14:00', '16:00'] },
  yAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%' } },
  series: [
    { name: 'OEE', type: 'line', smooth: true, data: [72, 78, 81, 79, 83, 80], lineStyle: { width: 3, color: '#0284c7' }, areaStyle: { color: 'rgba(2,132,199,0.10)' }, itemStyle: { color: '#0284c7' } },
    { name: 'Q', type: 'line', smooth: true, data: [95, 96, 97, 96, 94, 96], lineStyle: { width: 2, color: '#10b981' }, itemStyle: { color: '#10b981' } },
    { name: t('reports.dashboard.labels.target'), type: 'line', data: [85, 85, 85, 85, 85, 85], lineStyle: { type: 'dashed', color: '#94a3b8' }, symbol: 'none' },
  ],
}))

const loadDashboard = async () => {
  loading.value = true
  try {
    dashboard.value = await productionApi.dashboard({
      from: filters.value.dateFrom,
      to: filters.value.dateTo,
      lineCode: filters.value.lineCode,
    })
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const loadLines = async () => {
  try {
    const data = await masterApi.getLines()
    lines.value = (data || []).map(item => ({
      lineCode: item.code ?? item.lineCode ?? item.name,
    })).filter(item => item.lineCode)
  } catch (error) {
    console.error(error)
    lines.value = []
  }
}

const lineDetails = computed(() =>
  (dashboard.value?.lines ?? []).map(r => {
    const oeePct = (Number(r.oee || 0) * 100).toFixed(1)
    const isGood = r.oee >= 0.85
    const isWarn = r.oee >= 0.65
    return {
      code: r.lineCode,
      oee: oeePct,
      oeeColor: isGood ? 'text-emerald-600' : isWarn ? 'text-amber-600' : 'text-rose-600',
      borderClass: isGood ? 'border-emerald-200' : isWarn ? 'border-amber-200' : 'border-rose-200',
      tagType: isGood ? 'success' : isWarn ? 'warning' : 'danger',
      status: isGood ? t('reports.dashboard.sections.lineStatusStable') : isWarn ? t('reports.dashboard.sections.lineStatusWatch') : t('reports.dashboard.sections.lineStatusWarning'),
      metrics: [
        { label: t('reports.dashboard.sections.availability'), value: (Number(r.availabilityRate || 0) * 100).toFixed(0), barColor: 'bg-sky-500' },
        { label: t('reports.dashboard.sections.performance'), value: (Number(r.performanceRate || 0) * 100).toFixed(0), barColor: 'bg-indigo-500' },
        { label: t('reports.dashboard.sections.quality'), value: (Number(r.qualityRate || 0) * 100).toFixed(0), barColor: r.qualityRate < 0.95 ? 'bg-rose-500' : 'bg-emerald-500' },
      ],
    }
  })
)

onMounted(() => {
  loadLines()
  loadDashboard()
})

onBeforeUnmount(() => {
  if (refreshTimer) window.clearInterval(refreshTimer)
})
</script>
