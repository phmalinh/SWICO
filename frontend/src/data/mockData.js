export const mockProducts = [
  { id: 1, partNumber: 'PN-001', partName: 'Vỏ hộp A1', cycleTimeSeconds: 12.5 },
  { id: 2, partNumber: 'PN-002', partName: 'Nắp đậy B2', cycleTimeSeconds: 8 },
  { id: 3, partNumber: 'PN-003', partName: 'Khay nhựa C3', cycleTimeSeconds: 15.2 },
  { id: 4, partNumber: 'PN-004', partName: 'Bộ lọc D4', cycleTimeSeconds: 22 },
  { id: 5, partNumber: 'PN-005', partName: 'Van xả E5', cycleTimeSeconds: 18.5 },
]

export const mockLines = [
  { id: 1, lineCode: 'A1', description: 'Chuyền A1 - Lắp ráp' },
  { id: 2, lineCode: 'A2', description: 'Chuyền A2 - Đúc nhựa' },
  { id: 3, lineCode: 'A4', description: 'Chuyền A4 - Phun' },
  { id: 4, lineCode: 'B1', description: 'Chuyền B1 - Đóng gói' },
]

export const mockShifts = [
  { id: 1, shiftName: '全天1 (06:00-18:00)', standardTimeMinutes: 720 },
  { id: 2, shiftName: '白班 (06:00-14:00)', standardTimeMinutes: 480 },
  { id: 3, shiftName: '晚班 (14:00-22:00)', standardTimeMinutes: 480 },
  { id: 4, shiftName: '夜班 (22:00-06:00)', standardTimeMinutes: 480 },
]

export const mockMachines = [
  { id: 1, machineCode: 'TC-31', lineCode: 'A1', description: 'Máy ép TC-31' },
  { id: 2, machineCode: 'TC-32', lineCode: 'A1', description: 'Máy ép TC-32' },
  { id: 3, machineCode: 'TC-41', lineCode: 'A2', description: 'Máy phun TC-41' },
  { id: 4, machineCode: 'TC-42', lineCode: 'A4', description: 'Máy phun TC-42' },
  { id: 5, machineCode: 'PK-01', lineCode: 'B1', description: 'Máy đóng gói PK-01' },
]

export const mockReports = [
  { id: 1, reportDate: '2026-07-28', lineCode: 'A1', shiftName: '白班', machineCode: 'TC-31', partNumber: 'PN-001', partName: 'Vỏ hộp A1', operatingMinutes: 420, downtimeMinutes: 30, inputQuantity: 1800, goodQuantity: 1750, defectQuantity: 50, oee: 0.82, availabilityRate: 0.93, performanceRate: 0.91, qualityRate: 0.97, createdAt: '2026-07-28T08:15:00' },
  { id: 2, reportDate: '2026-07-28', lineCode: 'A2', shiftName: '白班', machineCode: 'TC-41', partNumber: 'PN-002', partName: 'Nắp đậy B2', operatingMinutes: 400, downtimeMinutes: 45, inputQuantity: 2500, goodQuantity: 2400, defectQuantity: 100, oee: 0.71, availabilityRate: 0.89, performanceRate: 0.88, qualityRate: 0.96, createdAt: '2026-07-28T08:30:00' },
  { id: 3, reportDate: '2026-07-28', lineCode: 'A4', shiftName: '白班', machineCode: 'TC-42', partNumber: 'PN-003', partName: 'Khay nhựa C3', operatingMinutes: 450, downtimeMinutes: 20, inputQuantity: 1600, goodQuantity: 1550, defectQuantity: 50, oee: 0.88, availabilityRate: 0.96, performanceRate: 0.92, qualityRate: 0.97, createdAt: '2026-07-28T09:00:00' },
  { id: 4, reportDate: '2026-07-28', lineCode: 'B1', shiftName: '白班', machineCode: 'PK-01', partNumber: 'PN-004', partName: 'Bộ lọc D4', operatingMinutes: 380, downtimeMinutes: 60, inputQuantity: 900, goodQuantity: 850, defectQuantity: 50, oee: 0.58, availabilityRate: 0.84, performanceRate: 0.78, qualityRate: 0.94, createdAt: '2026-07-28T09:45:00' },
]

export const mockUsers = [
  { id: 1, username: 'operator01', fullName: 'Nguyễn Văn A', role: 'OPERATOR', lineCode: 'A1', active: true },
  { id: 2, username: 'leader01', fullName: 'Trần Thị B', role: 'LEADER', lineCode: 'A1', active: true },
  { id: 3, username: 'manager01', fullName: 'Lê Văn C', role: 'MANAGER', lineCode: null, active: true },
  { id: 4, username: 'admin', fullName: 'Admin SWICO', role: 'ADMIN', lineCode: null, active: true },
]

export const mockAuditLogs = [
  { id: 1, action: 'CREATE', entity: 'DailyProductionReport', entityId: 101, username: 'operator01', detail: 'Tạo báo cáo ca A1 - PN-001', timestamp: '2026-07-28T08:15:00' },
  { id: 2, action: 'UPDATE', entity: 'DailyProductionReport', entityId: 101, username: 'leader01', detail: 'Sửa số lượng lỗi: 60 -> 50', timestamp: '2026-07-28T08:45:00' },
  { id: 3, action: 'CREATE', entity: 'Product', entityId: 5, username: 'manager01', detail: 'Thêm mã hàng PN-005', timestamp: '2026-07-27T14:20:00' },
  { id: 4, action: 'DELETE', entity: 'Product', entityId: 99, username: 'admin', detail: 'Xóa mã hàng PN-099 (trùng lặp)', timestamp: '2026-07-26T10:00:00' },
  { id: 5, action: 'UPDATE', entity: 'User', entityId: 2, username: 'admin', detail: 'Đổi quyền leader01: OPERATOR -> LEADER', timestamp: '2026-07-25T16:30:00' },
]

export const roleMenus = {
  ROLE_OPERATOR: ['1.1', '5.1'],
  ROLE_LEADER: ['1.1', '1.2', '2.2', '5.1'],
  ROLE_MANAGER: ['2.1', '2.2', '2.3', '3.1', '3.2', '3.3', '3.4', '3.5', '3.6', '5.1'],
  ROLE_ADMIN: ['1.1', '1.2', '2.1', '2.2', '2.3', '3.1', '3.2', '3.3', '3.4', '3.5', '3.6', '4.1', '4.2', '5.1'],
}

export const roleLabels = {
  ROLE_OPERATOR: { label: 'Công nhân', color: 'blue' },
  ROLE_LEADER: { label: 'Tổ trưởng', color: 'emerald' },
  ROLE_MANAGER: { label: 'Quản lý', color: 'amber' },
  ROLE_ADMIN: { label: 'Admin', color: 'rose' },
}
