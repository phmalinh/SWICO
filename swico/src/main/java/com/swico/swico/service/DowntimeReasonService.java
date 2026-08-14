package com.swico.swico.service;

import com.swico.swico.dto.DowntimeReasonResponse;
import com.swico.swico.dto.DowntimeReasonUpsertRequest;
import com.swico.swico.entity.DowntimeReason;
import com.swico.swico.repository.DailyProductionReportRepository;
import com.swico.swico.repository.DowntimeReasonRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DowntimeReasonService {

    private final DowntimeReasonRepository downtimeReasonRepository;
    private final DailyProductionReportRepository reportRepository;

    public DowntimeReasonService(DowntimeReasonRepository downtimeReasonRepository, DailyProductionReportRepository reportRepository) {
        this.downtimeReasonRepository = downtimeReasonRepository;
        this.reportRepository = reportRepository;
    }

    public List<DowntimeReasonResponse> getAll() {
        return downtimeReasonRepository.findAllByOrderBySortOrderAscReasonCodeAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    public DowntimeReasonResponse create(DowntimeReasonUpsertRequest request) {
        DowntimeReason reason = new DowntimeReason();
        applyRequest(reason, request);
        return toResponse(downtimeReasonRepository.save(reason));
    }

    public DowntimeReasonResponse update(Long id, DowntimeReasonUpsertRequest request) {
        DowntimeReason reason = downtimeReasonRepository.findById(id).orElseThrow();
        applyRequest(reason, request);
        return toResponse(downtimeReasonRepository.save(reason));
    }

    public void delete(Long id) {
        DowntimeReason reason = downtimeReasonRepository.findById(id).orElseThrow();
        if (reportRepository.existsByDowntimeReasonContaining(reason.getReasonCode())
                || reportRepository.existsByDowntimeReasonContaining(reason.getReasonText())) {
            throw new IllegalStateException("Không thể xóa lý do dừng " + reason.getReasonCode() + " vì vẫn còn dữ liệu liên quan: báo cáo sản xuất. Vui lòng xóa hoặc chuyển dữ liệu liên quan trước.");
        }
        downtimeReasonRepository.deleteById(id);
    }

    public DowntimeReason ensure(String reasonCode, String reasonText, int sortOrder) {
        return downtimeReasonRepository.findByReasonCode(reasonCode)
                .map(existing -> {
                    existing.setReasonText(reasonText);
                    existing.setSortOrder(sortOrder);
                    if (existing.getActive() == null) {
                        existing.setActive(true);
                    }
                    return downtimeReasonRepository.save(existing);
                })
                .orElseGet(() -> {
                    DowntimeReason reason = new DowntimeReason();
                    reason.setReasonCode(reasonCode);
                    reason.setReasonText(reasonText);
                    reason.setSortOrder(sortOrder);
                    reason.setActive(true);
                    return downtimeReasonRepository.save(reason);
                });
    }

    private void applyRequest(DowntimeReason reason, DowntimeReasonUpsertRequest request) {
        reason.setReasonCode(request.reasonCode().trim());
        reason.setReasonText(request.reasonText().trim());
        reason.setSortOrder(request.sortOrder() != null ? request.sortOrder() : 0);
        reason.setActive(request.active() != null ? request.active() : true);
    }

    private DowntimeReasonResponse toResponse(DowntimeReason reason) {
        return new DowntimeReasonResponse(
                reason.getId(),
                reason.getReasonCode(),
                reason.getReasonText(),
                reason.getSortOrder(),
                reason.getActive()
        );
    }
}
