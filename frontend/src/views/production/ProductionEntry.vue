<template>
  <div class="mx-auto w-full px-3 py-2">
    <div class="grid grid-cols-1 lg:grid-cols-12 gap-3 items-start">
      <section class="lg:col-span-9 rounded-lg border border-slate-200 bg-white shadow-sm overflow-hidden">
        <!-- Header rút gọn -->
        <div class="border-b border-slate-200 bg-slate-50 px-4 py-2.5 flex items-center justify-between">
          <div>
            <span class="text-[10px] font-black uppercase tracking-wider text-sky-600 block leading-tight">{{ t('productionEntry.eyebrow') }}</span>
            <h2 class="text-lg font-black text-slate-900 leading-tight">{{ t('productionEntry.pageTitle') }}</h2>
          </div>
          <div class="rounded bg-amber-100 px-2.5 py-1 text-xs font-bold text-amber-800 border border-amber-300">
            {{ t('productionEntry.touchMode') }}
          </div>
        </div>
        <el-form :model="form" label-position="top" class="p-3 space-y-2.5">
          <!-- Dòng 1: Cấu hình chung (Chuyền, Máy, Ca, Công ty) -->
          <div class="grid grid-cols-2 md:grid-cols-3 gap-2.5 " >
            <el-form-item :label="t('productionEntry.reportDate')" class="!mb-0 ">
              <el-date-picker v-model="form.reportDate" type="date" value-format="YYYY-MM-DD" class="w-full el-form-item__content" size="default"/>
            </el-form-item>
            <el-form-item :label="t('productionEntry.line')" class="!mb-0">
              <el-select v-model="form.lineCode" size="default" class="w-full" :placeholder="t('productionEntry.selectLine')" @change="onLineChange">
                <el-option v-for="l in lines" :key="l.lineCode" :label="`${l.lineCode} - ${l.description}`" :value="l.lineCode" />
              </el-select>
            </el-form-item>
            <el-form-item :label="t('productionEntry.machine')" class="!mb-0">
              <el-select v-model="form.machineCode" size="default" class="w-full" :placeholder="t('productionEntry.selectMachine')">
                <el-option v-for="m in filteredMachines" :key="m.machineCode" :label="`${m.machineCode} - ${m.description}`" :value="m.machineCode" />
              </el-select>
            </el-form-item>

            <el-form-item :label="t('productionEntry.shift')" class="!mb-0">
              <el-select v-model="form.shiftName" size="default" class="w-full" :placeholder="t('productionEntry.selectShift')">
                <el-option v-for="s in shifts" :key="s.shiftName" :label="s.shiftName" :value="s.shiftName" />
              </el-select>
            </el-form-item>
            <!-- <el-form-item :label="t('productionEntry.company')" class="!mb-0">
              <el-input v-model="form.company" size="default" :placeholder="t('productionEntry.enterCompany')" />
            </el-form-item> -->
             <el-form-item :label="t('productionEntry.company')" class="!mb-0">
              <el-select v-model="form.company" size="default" class="w-full" :placeholder="t('productionEntry.enterCompany')" filterable>
                <el-option v-for="r in company" :key="r" :company="r" :value="r" :label="r" />
              </el-select>
            </el-form-item>
            <el-form-item :label="t('productionEntry.responsibleLeader')" class="!mb-0">
              <el-select
                v-model="form.responsibleLeader"
                size="default"
                class="w-full"
                :placeholder="t('productionEntry.enterResponsibleLeader')"
                clearable
                filterable
              >
                <el-option
                  v-for="leader in leaders"
                  :key="leader.username"
                  :label="leader.label"
                  :value="leader.value"
                />
              </el-select>
            </el-form-item>
          </div>
  
          <div class="rounded-lg border border-slate-200 bg-slate-50 p-2.5">
            <div class="grid grid-cols-1 md:grid-cols-12 gap-2.5 items-end">
              <el-form-item :label="t('productionEntry.partNumber')" class="!mb-0 md:col-span-4">
                <el-select
                  v-model="form.partNumber"
                  size="default"
                  class="w-full"
                  filterable
                  :placeholder="t('productionEntry.scanBarcode')"
                  @change="onProductChange"
                >
                  <el-option v-for="p in products" :key="p.partNumber" :label="p.partNumber" :value="p.partNumber">
                    <span class="font-bold">{{ p.partNumber }}</span>
                    <span class="ml-2 text-xs text-slate-400">{{ p.partName }} ({{ p.cycleTimeSeconds }}s)</span>
                  </el-option>
                </el-select>
              </el-form-item>

              <el-form-item :label="t('productionEntry.partName')" class="!mb-0 md:col-span-4">
                <el-input v-model="form.partName" size="default" readonly placeholder="-" />
              </el-form-item>
              
              <el-form-item :label="t('productionEntry.cycleTime')" class="!mb-0 md:col-span-4">
                <el-input v-model="form.cycleTime" size="default" readonly placeholder="-" />
              </el-form-item>

              <el-form-item :label="t('productionEntry.processes')" class="!mb-0 md:col-span-12">
                <el-select
                  v-model="form.processes"
                  multiple
                  filterable
                  size="default"
                  class="w-full"
                  :placeholder="t('productionEntry.selectProcesses')"
                  @change="onProcessSelectionChange"
                >
                  <el-option
                    v-for="process in processOptions"
                    :key="process.id"
                    :label="formatProcessOption(process)"
                    :value="process.id"
                  />
                </el-select>
              </el-form-item>
            </div>
          </div>

          <!-- Dòng 3: Số liệu sản xuất thực tế -->
          <!-- Dòng 3: Thông số thời gian & Mục tiêu ca -->
          <div class="grid grid-cols-2 md:grid-cols-3 gap-2.5">
            <el-form-item :label="t('productionEntry.shiftTime')" class="!mb-0">
              <el-input 
                :model-value="formatNumber(selectedShiftMinutes, 0)" 
                size="default" 
                readonly 
                class="font-bold text-sky-700" 
              />
            </el-form-item>

            <el-form-item :label="t('productionEntry.dailyTarget')" class="!mb-0">
              <el-input 
                :model-value="formatNumber(dailyTargetPreview, 0)" 
                size="default" 
                readonly 
                class="font-bold text-indigo-700" 
              />
            </el-form-item>

            <el-form-item :label="t('productionEntry.actualMinutes')" class="!mb-0">
              <el-input 
                :model-value="formatNumber(actualOperatingMinutes, 0)" 
                size="default" 
                readonly 
                class="font-bold text-emerald-700" 
              />
            </el-form-item>
          </div>

          <!-- Dòng 4: Số lượng nhập & kết quả sản xuất -->
          <div class="mt-2.5 grid grid-cols-2 md:grid-cols-4 gap-2.5">
            <el-form-item :label="t('productionEntry.inputQuantity')" class="!mb-0">
              <div class="el-form-item__content flex min-w-0 overflow-hidden rounded border border-slate-300 bg-white">
                <el-button
                  size="default"
                  class="!h-[2.125rem] !w-10 !rounded-none !border-0 !border-r !border-slate-300 !bg-slate-50 !p-0"
                  @click="decrementQuantity('inputQuantity')"
                >
                  <Minus class="h-4 w-4" />
                </el-button>
                <el-input-number
                  v-model="form.inputQuantity"
                  :min="0"
                  :max="999999"
                  size="default"
                  class="min-w-0 flex-1 quantity-stepper-input"
                  :controls="false"
                />
                <el-button
                  size="default"
                  class="!h-[2.125rem] !w-10 !rounded-none !border-0 !border-l !border-slate-300 !bg-slate-50 !p-0"
                  @click="incrementQuantity('inputQuantity')"
                >
                  <Plus class="h-4 w-4" />
                </el-button>
              </div>
            </el-form-item>

            <el-form-item :label="t('productionEntry.goodQuantity')" class="!mb-0">
              <el-input :model-value="formatNumber(calculatedGoodQuantity, 0)" size="default" readonly class="font-bold text-emerald-700" />
            </el-form-item>

            <el-form-item :label="t('productionEntry.internalDefectQuantity')" class="!mb-0">
              <div class=" el-form-item__content flex min-w-0 overflow-hidden rounded border border-slate-300 bg-white">
                <el-button
                  size="default"
                  class="!h-[2.125rem] !w-10 !rounded-none !border-0 !border-r !border-slate-300 !bg-slate-50 !p-0"
                  @click="decrementQuantity('internalDefectQuantity')"
                >
                  <Minus class="h-4 w-4" />
                </el-button>
                <el-input-number
                  v-model="form.internalDefectQuantity"
                  :min="0"
                  :max="form.inputQuantity || 999999"
                  size="default"
                  class="min-w-0 flex-1 quantity-stepper-input"
                  :controls="false"
                />
                <el-button
                  size="default"
                  class="!h-[2.125rem] !w-10 !rounded-none !border-0 !border-l !border-slate-300 !bg-slate-50 !p-0"
                  @click="incrementQuantity('internalDefectQuantity')"
                >
                  <Plus class="h-4 w-4" />
                </el-button>
              </div>
            </el-form-item>

            <el-form-item :label="t('productionEntry.externalDefectQuantity')" class="!mb-0">
              <div class="el-form-item__content flex min-w-0 overflow-hidden rounded border border-slate-300 bg-white">
                <el-button
                  size="default"
                  class="!h-[2.125rem] !w-10 !rounded-none !border-0 !border-r !border-slate-300 !bg-slate-50 !p-0"
                  @click="decrementQuantity('externalDefectQuantity')"
                >
                  <Minus class="h-4 w-4" />
                </el-button>
                <el-input-number
                  v-model="form.externalDefectQuantity"
                  :min="0"
                  :max="form.inputQuantity || 999999"
                  size="default"
                  class="min-w-0 flex-1 quantity-stepper-input"
                  :controls="false"
                />
                <el-button
                  size="default"
                  class="!h-[2.125rem] !w-10 !rounded-none !border-0 !border-l !border-slate-300 !bg-slate-50 !p-0"
                  @click="incrementQuantity('externalDefectQuantity')"
                >
                  <Plus class="h-4 w-4" />
                </el-button>
              </div>
            </el-form-item>
          </div>

          <div class="mt-2.5 grid grid-cols-1 md:grid-cols-3 gap-2.5">
            <template v-for="(item, index) in form.downtimeItems" :key="index">
              <el-form-item :label="index === 0 ? t('productionEntry.downtimeCategory') : ''" class="!mb-0">
                <el-select
                  v-model="item.reasonCategoryCode"
                  size="default"
                  class="w-full"
                  :placeholder="t('productionEntry.downtimeCategory')"
                  clearable
                  filterable
                  @change="item.reason = ''"
                >
                  <el-option v-for="category in downtimeCategories" :key="category.reasonCategoryCode" :label="category.label" :value="category.reasonCategoryCode" />
                </el-select>
              </el-form-item>
              <el-form-item :label="index === 0 ? t('productionEntry.downtimeReason') : ''" class="!mb-0">
                <el-select
                  v-model="item.reason"
                  size="default"
                  class="w-full"
                  :placeholder="t('productionEntry.downtimeReason')"
                  filterable
                >
                  <el-option v-for="r in filteredDowntimeReasons(item.reasonCategoryCode)" :key="r.value" :label="r.label" :value="r.value" />
                </el-select>
              </el-form-item>
              <el-form-item :label="index === 0 ? t('productionEntry.downtimeMinutes') : ''" class="!mb-0">
                <div class="flex gap-1.5">
                  <div class="flex min-w-0 flex-1 overflow-hidden rounded border border-slate-300 bg-white">
                    <el-button
                      size="default"
                      class="!h-[2.125rem] !w-10 !rounded-none !border-0 !border-r !border-slate-300 !bg-slate-50 !p-0"
                      @click="decrementDowntimeMinutes(index)"
                    >
                      <Minus class="h-4 w-4" />
                    </el-button>
                    <el-input-number
                      v-model="item.minutes"
                      :min="0"
                      :max="1440"
                      size="default"
                      class="min-w-0 flex-1 downtime-minutes-input"
                      :controls="false"
                    />
                    <el-button
                      size="default"
                      class="!h-[2.125rem] !w-10 !rounded-none !border-0 !border-l !border-slate-300 !bg-slate-50 !p-0"
                      @click="incrementDowntimeMinutes(index)"
                    >
                      <Plus class="h-4 w-4" />
                    </el-button>
                  </div>
                  <el-button
                    type="primary"
                    size="default"
                    class="!h-[2.125rem] !w-[2.125rem] !p-0"
                    @click="addDowntimeItem(index)"
                  >
                    <Plus class="h-4 w-4" />
                  </el-button>
                  <el-button
                    v-if="form.downtimeItems.length > 1"
                    type="danger"
                    plain
                    size="default"
                    class="!h-[2.125rem] !w-[2.125rem] !p-0"
                    @click="removeDowntimeItem(index)"
                  >
                    <Trash2 class="h-4 w-4" />
                  </el-button>
                </div>
              </el-form-item>
            </template>
          </div>
          <!-- Nút bấm Action -->
            <div class="pt-2 flex flex-wrap items-center gap-3">
              <el-button type="primary" size="large" class="flex-1 !h-10 text-base font-bold" :loading="saving" @click="saveReport">
                <Save class="mr-2 h-4 w-4" />
                {{ t('productionEntry.save') }}
              </el-button>
              <el-button type="danger" size="large" class="!h-10 text-base" :disabled="selectedReports.length === 0" @click="deleteSelectedReports">
                {{ t('productionEntry.deleteSelected') }}
              </el-button>
              <el-button type="info" size="large" class="!h-10 text-base" @click="searchMyReports">
                {{ t('productionEntry.search') }}
              </el-button>
            <el-button size="large" class="!h-10 text-base" @click="resetForm">
              <RotateCcw class="mr-2 h-4 w-4" />
              {{ t('productionEntry.reset') }}
            </el-button>
          </div>

          <div v-if="isOperator" class="mt-4 rounded-lg border border-slate-200 bg-slate-50 p-4">
            <div class="mb-3 flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
              <div>
                <h3 class="text-sm font-black text-slate-900">{{ t('productionEntry.myReportsTitle') }}</h3>
              </div>
              <el-button link type="primary" size="small" @click="loadMyReports">{{ t('productionEntry.refresh') }}</el-button>
            </div>
            <el-table
              @selection-change="handleSelectionChange"
              @row-click="handleRowClick"
              :data="paginatedReports"
              stripe
              highlight-current-row
              row-key="id"
              style="width: 100%"
              size="small"
              v-loading="myReportsLoading"
            >
              <el-table-column type="selection" width="48" />
              <el-table-column prop="reportDate" :label="t('productionEntry.table.reportDate')" width="90" align="center" />
              <el-table-column prop="lineCode" :label="t('productionEntry.table.lineCode')" width="100" align="center" />
              <el-table-column prop="shiftName" :label="t('productionEntry.table.shiftName')" width="90" align="center" />
              <el-table-column prop="machineCode" :label="t('productionEntry.table.machineCode')" width="70" align="center" />
              <el-table-column prop="partNumber" :label="t('productionEntry.table.partNumber')" width="80" />
              <el-table-column prop="partName" :label="t('productionEntry.table.partName')" min-width="80" show-overflow-tooltip />
              <el-table-column :label="t('productionEntry.table.processIds')" min-width="120" show-overflow-tooltip>
                <template #default="{ row }">{{ formatProcessIds(row.processIds) }}</template>
              </el-table-column>
              <el-table-column prop="company" :label="t('productionEntry.table.company')" min-width="80" show-overflow-tooltip />
              <el-table-column :label="t('productionEntry.table.operatorName')" width="140" align="center" show-overflow-tooltip>
                <template #default="{ row }">{{ row.operatorName || row.createdBy || '-' }}</template>
              </el-table-column>
              <el-table-column :label="t('productionEntry.table.responsibleLeader')" width="150" align="center" show-overflow-tooltip>
                <template #default="{ row }">{{ row.responsibleLeader || '-' }}</template>
              </el-table-column>
              <el-table-column prop="downtimeReason" :label="t('productionEntry.table.downtimeReason')" min-width="160" show-overflow-tooltip />
              <el-table-column :label="t('productionEntry.table.responsibility')" min-width="100" align="center">
                <template #default="{ row }">{{ formatPercent(row.responsibility) }}</template>
              </el-table-column>
              <el-table-column :label="t('productionEntry.table.deductionPercent')" width="90" align="center">
                <template #default="{ row }">{{ formatPercent(row.deductionPercent) }}</template>
              </el-table-column>
              <el-table-column prop="totalOperatingMinutes" :label="t('productionEntry.table.totalOperatingMinutes')" width="70" align="center" />
              <el-table-column prop="downtimeMinutes" :label="t('productionEntry.table.downtimeMinutes')" width="70" align="center" />
              <el-table-column prop="inputQuantity" :label="t('productionEntry.table.inputQuantity')" width="70" align="center" />
              <el-table-column prop="goodQuantity" :label="t('productionEntry.table.goodQuantity')" width="70" align="center" />
              <el-table-column prop="internalDefectQuantity" :label="t('productionEntry.table.internalDefectQuantity')" width="80" align="center" />
              <el-table-column prop="externalDefectQuantity" :label="t('productionEntry.table.externalDefectQuantity')" width="80" align="center" />
              <el-table-column prop="defectQuantity" :label="t('productionEntry.table.defectQuantity')" width="70" align="center" />
              <el-table-column prop="shiftStandardTimeMinutes" :label="t('productionEntry.table.shiftStandardTimeMinutes')" width="70" align="center" />
              <el-table-column prop="dailyTargetQuantity" :label="t('productionEntry.table.dailyTargetQuantity')" width="70" align="center" />
              <el-table-column prop="productionEfficiency" :label="t('productionEntry.table.productionEfficiency')" width="70" align="center" />
              <el-table-column prop="availabilityRate" :label="t('productionEntry.table.availabilityRate')" width="70" align="center" />
              <el-table-column prop="performanceRate" :label="t('productionEntry.table.performanceRate')" width="70" align="center" />
              <el-table-column prop="qualityRate" :label="t('productionEntry.table.qualityRate')" width="70" align="center" />
              <el-table-column prop="oee" :label="t('productionEntry.table.oee')" width="90" align="center" />
              <el-table-column prop="evaluationLabel" :label="t('productionEntry.table.evaluationLabel')" width="70" align="center" />
            </el-table>
            <div class="flex justify-end px-4 py-3 border-t border-slate-200 bg-slate-50">
              <el-pagination
                v-model:current-page="currentPage"
                :page-size="pageSize"
                :total="myReports.length"
                layout="prev, pager, next"
                background
              />
            </div>
          </div>
        </el-form>
      </section>

      <!-- KHỐI KẾT QUẢ/OEE (RIGHT COLUMN - 3 Cols) -->
      <aside class="lg:col-span-3">
        <div class="rounded-lg border border-slate-200 bg-white p-3 shadow-sm">
          <h3 class="text-xs font-black uppercase tracking-wider text-slate-500 border-b pb-1.5 mb-2">{{ t('productionEntry.results') }}</h3>
          
          <div v-if="showResult" class="space-y-1.5">
            <div v-for="item in oeeCards" :key="item.key" class="rounded border px-2.5 py-1.5 flex items-center justify-between" :class="item.cardClass">
              <span class="text-[11px] font-bold text-slate-600 uppercase">{{ item.label }}</span>
              <span class="text-base font-black" :class="item.textClass">{{ item.value }}</span>
            </div>
            
            <div class="mt-2 rounded py-1.5 text-center text-xs font-black uppercase tracking-wide" :class="resultBadgeClass">
              {{ resultLabel }}
            </div>
          </div>

          <div v-else class="py-12 border-2 border-dashed border-slate-200 rounded-lg text-center text-xs font-semibold text-slate-400">
            {{ t('productionEntry.noResult') }}
          </div>

          <div class="mt-4 rounded-lg border border-slate-200 bg-slate-50 p-3 text-sm text-slate-700">
            <h4 class="font-bold mb-2 text-slate-900">{{ t('productionEntry.instructions.title') }}</h4>
            <ul class="list-disc list-inside space-y-1">
              <li>{{ t('productionEntry.instructions.edit') }}</li>
              <li>{{ t('productionEntry.instructions.delete') }}</li>
              <li>{{ t('productionEntry.instructions.search') }}</li>
            </ul>
          </div>
        </div>
      </aside>

    </div>
  </div>
</template>

<script setup>
import { computed, inject, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Minus, Plus, RotateCcw, Save, Trash2 } from 'lucide-vue-next'
import { masterApi, productionApi } from '@/services/api'
import { getSession } from '@/services/auth'
import { formatPercent, getOeeColor } from '@/composables/useOee'
import { useI18n } from '@/i18n'

const { t } = useI18n()

const products = ref([])
const lines = ref([])
const machines = ref([])
const leaders = ref([])
const processOptions = ref([])
const processNameById = ref({})
const shifts = ref([])
const saving = ref(false)
const showResult = ref(false)
const result = ref(null)
const currentUser = inject('currentUser', computed(() => ({ name: 'Người đăng nhập', fullName: 'Người dùng' })))
const myReports = ref([])
const myReportsLoading = ref(false)
const selectedReports = ref([])
const editedReportId = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)

const paginatedReports = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return myReports.value.slice(start, end)
})

function localTodayString() {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const form = ref({
  reportDate: localTodayString(),
  lineCode: '',
  machineCode: '',
  shiftName: '',
  partNumber: '',
  partName: '',
  cycleTime: '',
  processes: [],
  downtimeMinutes: 30,
  inputQuantity: 0,
  goodQuantity: 0,
  defectQuantity: 0,
  internalDefectQuantity: 0,
  externalDefectQuantity: 0,
  company: 'SWICO',
  responsibleLeader: '',
  downtimeReason: '',
  downtimeReasons: [''],
  downtimeItems: [{ reasonCategoryCode: '', reason: '', minutes: 30 }],
})

const defaultDowntimeReasons = [
  'A. 換刀（粗／精面銑刀、內孔鏜刀、鑽頭等） / Thay dao (dao phay mặt thô + tinh, dao móc lỗ, mũi khoan, ...)',
  'B. 砂輪用盡、更換砂輪（針對磨床組） / Hết đá, thay đá (đối với tổ Mài)',
  'C. 停機等料（等待毛坯） / Ngưng máy chờ phôi',
  'D. 等待前工序來料（針對前工序 C/T 長於後工序） / Chờ hàng công đoạn trước (đối với công đoạn đầu C/T lâu hơn công đoạn sau)',
  'E. 等待調機人員（技術員）調機 / Chờ cán bộ chỉnh máy',
  'F. 等待品檢（QC）首件確認／調機品確認 / Chờ QC xác nhận hàng chỉnh máy',
  'G. 操作人員請假（無替代人員時） / Nhân viên thao tác nghỉ phép (khi không có người thay thế)',
  'H. 其他 / Khác',
]
const company = [
  'SWICO',
  'STRONG WAY',
  '客戶',
  '其他',
]
const downtimeCategories = ref([])
const downtimeReasons = ref(defaultDowntimeReasons.map(value => ({ categoryCode: '', label: value, value })))

function normalizeLeader(user) {
  const fullName = String(user?.fullName || '').trim()
  const username = String(user?.username || '').trim()
  const displayName = fullName || username
  return {
    username,
    value: displayName,
    label: username && fullName ? `${fullName} (${username})` : displayName,
  }
}

function filteredDowntimeReasons(categoryCode) {
  if (!categoryCode) return downtimeReasons.value
  return downtimeReasons.value.filter(reason => reason.categoryCode === categoryCode)
}

function normalizeDowntimeReasons(value) {
  if (Array.isArray(value)) {
    const reasons = value.map(item => String(item || '').trim()).filter(Boolean)
    return reasons.length ? reasons : ['']
  }
  const reasons = String(value || '')
    .split(/\s*[;；]\s*/)
    .map(item => item.trim())
    .filter(Boolean)
  return reasons.length ? reasons : ['']
}

function normalizeDowntimeItems(reasonValue, minutesValue = 0) {
  const reasons = normalizeDowntimeReasons(reasonValue)
  const minutes = Number(minutesValue || 0)
  return reasons.map((value, index) => {
    const match = String(value).match(/^(.*?)(?:\s+-\s+(\d+))$/)
    if (match) {
      return {
        reasonCategoryCode: resolveDowntimeCategoryCode(match[1].trim()),
        reason: match[1].trim(),
        minutes: Number(match[2] || 0),
      }
    }
    return {
      reasonCategoryCode: resolveDowntimeCategoryCode(value),
      reason: value,
      minutes: index === 0 ? minutes : 0,
    }
  })
}

function addDowntimeItem(index = form.value.downtimeItems.length - 1) {
  form.value.downtimeItems.splice(index + 1, 0, { reasonCategoryCode: '', reason: '', minutes: 0 })
}

function decrementDowntimeMinutes(index) {
  const item = form.value.downtimeItems[index]
  if (!item) return
  item.minutes = Math.max(Number(item.minutes || 0) - 1, 0)
}

function incrementDowntimeMinutes(index) {
  const item = form.value.downtimeItems[index]
  if (!item) return
  item.minutes = Math.min(Number(item.minutes || 0) + 1, 1440)
}

function maxQuantityForField(field) {
  if (field === 'inputQuantity') return 999999
  return Number(form.value.inputQuantity || 999999)
}

function decrementQuantity(field) {
  form.value[field] = Math.max(Number(form.value[field] || 0) - 1, 0)
}

function incrementQuantity(field) {
  form.value[field] = Math.min(Number(form.value[field] || 0) + 1, maxQuantityForField(field))
}

function removeDowntimeItem(index) {
  if (form.value.downtimeItems.length === 1) {
    form.value.downtimeItems = [{ reasonCategoryCode: '', reason: '', minutes: 0 }]
    return
  }
  form.value.downtimeItems.splice(index, 1)
}

function formatDowntimeReasonsForSave() {
  return form.value.downtimeItems
    .map(item => {
      const reason = String(item.reason || '').trim()
      if (!reason) return ''
      return `${reason} - ${Number(item.minutes || 0)}`
    })
    .filter(Boolean)
    .join('； ')
}

const totalDowntimeMinutes = computed(() => form.value.downtimeItems.reduce((sum, item) => sum + Number(item.minutes || 0), 0))

function resolveDowntimeCategoryCode(reasonValue) {
  const reason = downtimeReasons.value.find(item => item.value === reasonValue)
  return reason?.categoryCode || ''
}

const filteredMachines = computed(() => {
  if (!form.value.lineCode) return machines.value
  const selectedLine = form.value.lineCode.toLowerCase()
  return machines.value.filter(machine => (machine.lineCode || '').toLowerCase() === selectedLine)
})

const selectedShiftMinutes = computed(() => {
  const shift = shifts.value.find(s => s.shiftName === form.value.shiftName)
  return shift?.standardTimeMinutes ?? null
})

const actualOperatingMinutes = computed(() => {
  const shiftMinutes = Number(selectedShiftMinutes.value || 0)
  const downtime = Number(totalDowntimeMinutes.value || 0)
  if (shiftMinutes <= 0) return 0
  return Math.max(shiftMinutes - downtime, 0)
})

const calculatedGoodQuantity = computed(() => {
  const input = Number(form.value.inputQuantity || 0)
  const defect = Number(totalDefectQuantity.value || 0)
  return Math.max(input - defect, 0)
})

const totalDefectQuantity = computed(() => Number(form.value.internalDefectQuantity || 0) + Number(form.value.externalDefectQuantity || 0))

const responsibilityPreview = computed(() => {
  const input = Number(form.value.inputQuantity || 0)
  if (input <= 0) return 0
  return Number(form.value.internalDefectQuantity || 0) / input
})

const deductionPercentPreview = computed(() => Math.max(Number(responsibilityPreview.value || 0) - 0.0027, 0))

const dailyTargetPreview = computed(() => {
  const actualMinutes = Number(actualOperatingMinutes.value || 0)
  const cycleTime = Number(form.value.cycleTime || 0)
  if (actualMinutes <= 0 || cycleTime <= 0) return null
  return (actualMinutes * 60) / cycleTime
})

const isOperator = computed(() => currentUser.value?.role === 'ROLE_OPERATOR')

async function loadMyReports(params = {}) {
  if (!isOperator.value) {
    myReports.value = []
    return
  }

  myReportsLoading.value = true
  try {
    myReports.value = await productionApi.myReports(params)
  } catch (error) {
    console.warn('Failed to load operator reports', error)
    myReports.value = []
  } finally {
    myReportsLoading.value = false
  }
}

function handleSelectionChange(selection) {
  selectedReports.value = selection || []
}

function handleRowClick(report) {
  if (!report) return
  selectedReports.value = [report]
  populateFormForEdit(report)
  editedReportId.value = report.id
  ElMessage.info(t('productionEntry.messages.selectedForEdit'))
}

function populateFormForEdit(report) {
  form.value.reportDate = report.reportDate || localTodayString()
  form.value.lineCode = report.lineCode || ''
  form.value.machineCode = report.machineCode || ''
  form.value.shiftName = report.shiftName || ''
  form.value.partNumber = report.partNumber || ''
  form.value.partName = report.partName || ''
  form.value.cycleTime = report.cycleTimeSeconds || ''
  form.value.downtimeMinutes = report.downtimeMinutes ?? 30
  form.value.inputQuantity = report.inputQuantity ?? 0
  form.value.goodQuantity = report.goodQuantity ?? 0
  form.value.defectQuantity = report.defectQuantity ?? Math.max(Number(report.inputQuantity || 0) - Number(report.goodQuantity || 0), 0)
  form.value.internalDefectQuantity = report.internalDefectQuantity ?? form.value.defectQuantity
  form.value.externalDefectQuantity = report.externalDefectQuantity ?? 0
  form.value.company = report.company || 'SWICO'
  form.value.responsibleLeader = report.responsibleLeader || ''
  form.value.downtimeReason = report.downtimeReason || ''
  form.value.downtimeReasons = normalizeDowntimeReasons(report.downtimeReason)
  form.value.downtimeItems = normalizeDowntimeItems(report.downtimeReason, report.downtimeMinutes)
}

async function editSelectedReport() {
  if (!selectedReports.value.length) {
    ElMessage.warning(t('productionEntry.messages.selectOneReportForEdit'))
    return
  }
  const report = selectedReports.value[0]
  populateFormForEdit(report)
  editedReportId.value = report.id
  ElMessage.info(t('productionEntry.messages.selectedForEdit'))
}

async function deleteSelectedReports() {
  if (!selectedReports.value.length) {
    ElMessage.warning(t('productionEntry.messages.selectOneReportToDelete'))
    return
  }
  const confirmed = window.confirm(t('productionEntry.messages.deleteConfirm'))
  if (!confirmed) return

  try {
    const ids = selectedReports.value.map(item => item.id)
    await productionApi.deleteReports(ids)
    ElMessage.success(t('productionEntry.messages.deleteSuccess'))
    selectedReports.value = []
    await loadMyReports()
  } catch (error) {
    ElMessage.error(t('productionEntry.messages.deleteFailed', { error: error.message }))
  }
}

function searchMyReports() {
  loadMyReports({
    reportDate: form.value.reportDate,
    lineCode: form.value.lineCode,
    shiftName: form.value.shiftName,
    partNumber: form.value.partNumber,
  })
}

function onProductChange(partNumber) {
  const product = products.value.find(p => p.partNumber === partNumber)
  if (product) {
    form.value.partName = product.partName
    form.value.cycleTime = product.cycleTimeSeconds
    form.value.processes = []
    loadProductProcesses(product.id)
  } else {
    form.value.partName = ''
    form.value.cycleTime = ''
    form.value.processes = []
    processOptions.value = []
  }
}

function formatProcessOption(process) {
  return process?.processCode || ''
}

function onProcessSelectionChange(selectedIds = []) {
  if (!selectedIds.length) return
  const selectedProcess = processOptions.value.find(process => process.id === selectedIds[selectedIds.length - 1])
  if (selectedProcess?.cycleTimeSeconds != null) {
    form.value.cycleTime = selectedProcess.cycleTimeSeconds
  }
}

function onLineChange() {
  const options = filteredMachines.value
  const currentStillValid = options.some(machine => machine.machineCode === form.value.machineCode)
  if (!form.value.lineCode) {
    form.value.machineCode = ''
    return
  }
  if (!currentStillValid) {
    form.value.machineCode = options[0]?.machineCode || ''
  }
}

async function loadProductProcesses(productId) {
  try {
    processOptions.value = await masterApi.getProductProcesses(productId)
    mergeProcessNames(processOptions.value)
  } catch (error) {
    processOptions.value = []
    console.warn('Không tải được danh sách công đoạn', error)
  }
}

function mergeProcessNames(processes = []) {
  processNameById.value = {
    ...processNameById.value,
    ...processes.reduce((map, process) => {
      if (process?.id) map[process.id] = process.processCode || process.process || ''
      return map
    }, {}),
  }
}

async function loadProcessNamesForProducts(productList = products.value) {
  const processGroups = await Promise.all(
    productList
      .filter(product => product.id)
      .map(product => masterApi.getProductProcesses(product.id).catch(() => []))
  )
  processGroups.forEach(mergeProcessNames)
}

function formatProcessIds(processIds) {
  if (!Array.isArray(processIds) || processIds.length === 0) return '-'
  return processIds.map(id => processNameById.value[id]).filter(Boolean).join(' + ') || '-'
}

function resetForm() {
  form.value.reportDate = localTodayString()
  form.value.partNumber = ''
  form.value.partName = ''
  form.value.cycleTime = ''
  form.value.processes = []
  processOptions.value = []
  form.value.downtimeMinutes = 30
  form.value.downtimeReason = ''
  form.value.downtimeReasons = ['']
  form.value.downtimeItems = [{ reasonCategoryCode: '', reason: '', minutes: 30 }]
  form.value.inputQuantity = 0
  form.value.goodQuantity = 0
  form.value.defectQuantity = 0
  form.value.internalDefectQuantity = 0
  form.value.externalDefectQuantity = 0
  form.value.responsibleLeader = ''
  editedReportId.value = null
  selectedReports.value = []
  showResult.value = false
  result.value = null
}

function autoSelectShiftByTime() {
  if (!shifts.value.length || form.value.shiftName) return

  const now = new Date()
  const currentMinutes = now.getHours() * 60 + now.getMinutes()

  const matchingShift = shifts.value.find(shift => {
    const range = extractShiftRange(shift)
    if (!range) return false
    const [startMinutes, endMinutes] = range

    if (startMinutes <= endMinutes) {
      return currentMinutes >= startMinutes && currentMinutes < endMinutes
    }
    return currentMinutes >= startMinutes || currentMinutes < endMinutes
  })

  if (matchingShift) {
    form.value.shiftName = matchingShift.shiftName
  }
}

function extractShiftRange(shift) {
  const startMinutes = shift.startTime ? parseTimeString(shift.startTime) : null
  const endMinutes = shift.endTime ? parseTimeString(shift.endTime) : null
  if (startMinutes != null && endMinutes != null) {
    return [startMinutes, endMinutes]
  }

  const label = shift.shiftName || ''
  const match = label.match(/(\d{1,2}:\d{2})-(\d{1,2}:\d{2})/)
  if (!match) return null

  const parsedStart = parseTimeString(match[1])
  const parsedEnd = parseTimeString(match[2])
  if (parsedStart == null || parsedEnd == null) return null

  return [parsedStart, parsedEnd]
}

function parseTimeString(value) {
  if (!value) return null
  const match = String(value).match(/^(\d{1,2}):(\d{2})$/)
  if (!match) return null
  const hours = Number(match[1])
  const minutes = Number(match[2])
  if (hours < 0 || hours > 23 || minutes < 0 || minutes > 59) return null
  return hours * 60 + minutes
}

async function loadInitialData() {
  try {
    const [productsRes, linesRes, machinesRes, shiftsRes, leadersRes, downtimeCategoriesRes, downtimeReasonsRes] = await Promise.all([
      masterApi.getProducts(),
      masterApi.getLines(),
      masterApi.getMachines(),
      masterApi.getShifts(),
      masterApi.getLeaders(),
      masterApi.getDowntimeReasonCategories().catch(() => []),
      masterApi.getDowntimeReasons().catch(() => []),
    ])

    products.value = productsRes.map(item => ({
      id: item.id,
      partNumber: item.code,
      partName: item.name,
      customer: item.customer || '',
      cycleTimeSeconds: item.cycleTimeSeconds,
    }))
    await loadProcessNamesForProducts(products.value)
    lines.value = linesRes.map(item => ({ id: item.id, lineCode: item.code, description: item.name }))
    machines.value = machinesRes.map(item => ({
      id: item.id,
      machineCode: item.machineCode,
      description: item.description,
      lineCode: item.lineCode || '',
    }))
    shifts.value = shiftsRes.map(item => ({
      id: item.id,
      shiftName: item.name,
      standardTimeMinutes: item.standardTimeMinutes,
      startTime: item.startTime || null,
      endTime: item.endTime || null,
    }))
    leaders.value = (leadersRes || []).map(normalizeLeader).filter(item => item.value)
    downtimeCategories.value = (downtimeCategoriesRes || [])
      .filter(item => item.active !== false)
      .map(item => ({
        reasonCategoryCode: item.reasonCategoryCode || '',
        label: `${item.reasonCategoryCode}. ${item.reasonCategoryText}`.trim(),
      }))
      .filter(item => item.reasonCategoryCode)
    const activeDowntimeReasons = (downtimeReasonsRes || [])
      .filter(item => item.active !== false)
      .map(item => {
        const value = `${item.reasonCode}. ${item.reasonText}`.trim()
        return {
          categoryCode: item.reasonCategoryCode || '',
          label: value,
          value,
        }
      })
      .filter(item => item.value)
    downtimeReasons.value = activeDowntimeReasons.length ? activeDowntimeReasons : defaultDowntimeReasons.map(value => ({ categoryCode: '', label: value, value }))

    if (!form.value.lineCode && lines.value.length) form.value.lineCode = lines.value[0].lineCode
    if (!form.value.machineCode && machines.value.length) {
      const machine = machines.value.find(m => m.lineCode === form.value.lineCode) || machines.value[0]
      form.value.machineCode = machine?.machineCode || ''
    }
    if (form.value.partNumber) {
      const product = products.value.find(p => p.partNumber === form.value.partNumber)
      if (product) await loadProductProcesses(product.id)
    }
    if (!form.value.shiftName && shifts.value.length) {
      autoSelectShiftByTime()
      if (!form.value.shiftName) form.value.shiftName = shifts.value[0].shiftName
    }
    await loadMyReports()
  } catch (error) {
    ElMessage.error(`${t('productionEntry.messages.loadFailed')}: ${error.message}`)
  }
}

async function saveReport() {
  if (!form.value.partNumber) {
    ElMessage.warning(t('productionEntry.messages.selectProduct'))
    return
  }
  if (!getSession()?.token) {
    ElMessage.error(t('productionEntry.messages.loginRequired'))
    return
  }
  saving.value = true
  try {
    const payload = {
      reportDate: form.value.reportDate,
      lineCode: form.value.lineCode,
      shiftName: form.value.shiftName,
      machineCode: form.value.machineCode,
      partNumber: form.value.partNumber,
      partName: form.value.partName,
      cycleTimeSeconds: Number(form.value.cycleTime),
      processIds: form.value.processes,
      totalOperatingMinutes: Number(actualOperatingMinutes.value),
      downtimeMinutes: Number(totalDowntimeMinutes.value),
      inputQuantity: Number(form.value.inputQuantity),
      goodQuantity: Number(calculatedGoodQuantity.value),
      defectQuantity: Number(totalDefectQuantity.value),
      internalDefectQuantity: Number(form.value.internalDefectQuantity || 0),
      externalDefectQuantity: Number(form.value.externalDefectQuantity || 0),
      company: form.value.company,
      responsibleLeader: form.value.responsibleLeader,
      downtimeReason: formatDowntimeReasonsForSave(),
    }

    const saved = editedReportId.value
      ? await productionApi.update(editedReportId.value, payload)
      : await productionApi.create(payload)

    result.value = saved
    showResult.value = true
    ElMessage.success(t('productionEntry.messages.saveSuccess'))
    editedReportId.value = null
    selectedReports.value = []
    await loadMyReports()
  } catch (error) {
    ElMessage.error(`${t('productionEntry.messages.saveFailed')}: ${error.message}`)
  } finally {
    saving.value = false
  }
}

const oeeCards = computed(() => {
  if (!result.value) return []
  const r = result.value
  const oeeColor = getOeeColor(Number(r.oee || 0))
  return [
    { key: 'oee', label: 'OEE', value: formatPercent(Number(r.oee || 0)), textClass: oeeColor.text, cardClass: 'border-slate-200 bg-slate-50' },
    { key: 'prod-eff', label: t('productionEntry.productionEfficiency'), value: formatPercent(Number(r.productionEfficiency || 0)), textClass: 'text-amber-600', cardClass: 'border-amber-100 bg-amber-50' },
    { key: 'a', label: t('reports.dashboard.sections.availability'), value: formatPercent(Number(r.availabilityRate || 0)), textClass: 'text-sky-600', cardClass: 'border-sky-100 bg-sky-50' },
    { key: 'p', label: t('reports.dashboard.sections.performance'), value: formatPercent(Number(r.performanceRate || 0)), textClass: 'text-indigo-600', cardClass: 'border-indigo-100 bg-indigo-50' },
    { key: 'q', label: t('reports.dashboard.sections.quality'), value: formatPercent(Number(r.qualityRate || 0)), textClass: 'text-emerald-600', cardClass: 'border-emerald-100 bg-emerald-50' },
    { key: 'responsibility', label: t('productionEntry.responsibility'), value: formatPercent(Number(r.responsibility || 0)), textClass: 'text-rose-600', cardClass: 'border-rose-100 bg-rose-50' },
    { key: 'deduction', label: t('productionEntry.deductionPercent'), value: formatPercent(Number(r.deductionPercent || 0)), textClass: 'text-rose-600', cardClass: 'border-rose-100 bg-rose-50' },
    { key: 'operator', label: t('productionEntry.table.operatorName'), value: r.operatorName || currentUser.value?.fullName || currentUser.value?.name || '-', textClass: 'text-slate-700', cardClass: 'border-slate-200 bg-white' },
    { key: 'leader', label: t('productionEntry.table.responsibleLeader'), value: r.responsibleLeader || '-', textClass: 'text-slate-700', cardClass: 'border-slate-200 bg-white' },
  ]
})

function formatNumber(value, fractionDigits = 0) {
  if (value === null || value === undefined || value === '') return '-'
  return Number(value).toLocaleString('vi-VN', {
    minimumFractionDigits: fractionDigits,
    maximumFractionDigits: fractionDigits,
  })
}

const resultLabel = computed(() => (result.value ? result.value.evaluationLabel : ''))
const resultBadgeClass = computed(() => {
  const oee = Number(result.value?.oee || 0)
  if (oee >= 0.85) return 'bg-emerald-100 text-emerald-800'
  if (oee >= 0.65) return 'bg-amber-100 text-amber-800'
  return 'bg-rose-100 text-rose-800'
})

watch(() => form.value.lineCode, () => {
  onLineChange()
})

watch([() => form.value.partNumber, products], ([partNumber]) => {
  if (!partNumber) return
  const product = products.value.find(p => p.partNumber === partNumber)
  if (!product) return
  form.value.partName = product.partName
  form.value.cycleTime = product.cycleTimeSeconds
}, { immediate: true })

onMounted(loadInitialData)
</script>

<style scoped>
/* Reset Element Plus compact height */
:deep(.el-form-item__label) {
  font-size: 0.8rem !important;
  font-weight: 700 !important;
  margin-bottom: 0.2rem !important;
  line-height: 1.2 !important;
}

:deep(.el-input__wrapper),
:deep(.el-select__wrapper) {
  min-height: 2.125rem !important;
  height: 2.125rem !important;
  padding-left: 0.5rem !important;
  padding-right: 0.5rem !important;
}

/* tô xám toàn bộ các ô readonly (kể cả ô input wrapper và khung bên trong) */
:deep(.el-input.is-disabled .el-input__wrapper),
:deep(.el-input .el-input__inner[readonly]),
:deep(.el-input__wrapper:has(input[readonly])) {
  background-color: #e2e8f0 !important; /* Màu xám slate-200 rõ ràng */
  border-color: #cbd5e1 !important;
  cursor: not-allowed;
}

:deep(.el-input__inner) {
  font-size: 0.925rem !important;
  font-weight: 700 !important;
}

/* Đảm bảo chữ trong ô readonly vẫn đậm và dễ nhìn */
:deep(.el-input__inner[readonly]) {
  color: #334155 !important; /* Màu chữ xám đậm slate-700 */
}

:deep(.el-input-number) {
  width: 100% !important;
}

:deep(.downtime-minutes-input .el-input__wrapper) {
  box-shadow: none !important;
  border-radius: 0 !important;
}

:deep(.quantity-stepper-input .el-input__wrapper) {
  box-shadow: none !important;
  border-radius: 0 !important;
}
</style>
