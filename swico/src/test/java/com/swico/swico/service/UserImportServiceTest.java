package com.swico.swico.service;

import com.swico.swico.entity.Role;
import com.swico.swico.entity.User;
import com.swico.swico.repository.UserRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserImportServiceTest {

    @Test
    void importShouldUseEmployeeCodeAsUsernameAndEncodeExcelPassword() throws Exception {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        UserImportService service = new UserImportService(userRepository, passwordEncoder);
        Map<String, User> savedUsers = new HashMap<>();

        when(userRepository.findByUsername("260306")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            savedUsers.put(user.getUsername(), user);
            return user;
        });

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "ds.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                workbookBytes()
        );

        var result = service.importWorkbook(file);
        User imported = savedUsers.get("260306");

        assertEquals(1, result.created());
        assertEquals("TÀO PHI LONG 曹飛龍", imported.getFullName());
        assertEquals(Role.ROLE_LEADER, imported.getRole());
        assertTrue(passwordEncoder.matches("CBDB", imported.getPassword()));
    }

    private byte[] workbookBytes() throws Exception {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("Sheet1");
            Row header = sheet.createRow(0);
            header.createCell(1).setCellValue("TÊN NHÂN VIÊN");
            header.createCell(2).setCellValue("CHỨC VỤ");
            header.createCell(3).setCellValue("TỔ");
            header.createCell(4).setCellValue("MÃ SỐ");
            header.createCell(5).setCellValue("NGÀY VÀO LÀM");
            header.createCell(6).setCellValue("MẬT KHẨU");

            Row row = sheet.createRow(1);
            row.createCell(1).setCellValue("TÀO PHI LONG\n曹飛龍");
            row.createCell(2).setCellValue("CB dự bị \n預備幹部");
            row.createCell(3).setCellValue("CNC+TC");
            row.createCell(4).setCellValue("260306");
            row.createCell(5).setCellValue("12/03/2026");
            row.createCell(6).setCellValue(" CBDB ");

            workbook.write(output);
            return output.toByteArray();
        }
    }
}
