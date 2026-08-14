package com.swico.swico.service;

import com.swico.swico.dto.MachineResponse;
import com.swico.swico.dto.MachineUpsertRequest;
import com.swico.swico.entity.Line;
import com.swico.swico.entity.Machine;
import com.swico.swico.repository.DailyProductionReportRepository;
import com.swico.swico.repository.LineRepository;
import com.swico.swico.repository.MachineRepository;
import com.swico.swico.repository.ProductProcessRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MachineServiceTest {

    @Test
    void createShouldPersistLineCodeWhenProvided() {
        MachineRepository machineRepository = mock(MachineRepository.class);
        LineRepository lineRepository = mock(LineRepository.class);
        DailyProductionReportRepository reportRepository = mock(DailyProductionReportRepository.class);
        ProductProcessRepository productProcessRepository = mock(ProductProcessRepository.class);
        MachineService service = new MachineService(machineRepository, lineRepository, reportRepository, productProcessRepository);

        Line line = new Line();
        line.setId(7L);
        line.setLineCode("A1");
        when(lineRepository.findByLineCode("A1")).thenReturn(Optional.of(line));

        when(machineRepository.save(any(Machine.class))).thenAnswer(invocation -> {
            Machine machine = invocation.getArgument(0);
            machine.setId(1L);
            return machine;
        });

        MachineResponse response = service.create(new MachineUpsertRequest("TC-01", "Test machine", "A1", null, null, null));

        ArgumentCaptor<Machine> machineCaptor = ArgumentCaptor.forClass(Machine.class);
        verify(machineRepository).save(machineCaptor.capture());

        assertEquals("A1", response.lineCode());
        assertEquals("A1", machineCaptor.getValue().getLine().getLineCode());
    }
}
