package com.swico.swico.controller;

import com.swico.swico.dto.MenuItemDto;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/menu")
@CrossOrigin(origins = "*")
public class MenuController {

    @GetMapping
    public List<MenuItemDto> getMenuByRole(@RequestParam(defaultValue = "ROLE_OPERATOR") String role) {
        List<MenuItemDto> menu = new ArrayList<>();

        // 1. NHÓM BÁO CÁO SẢN XUẤT (Công nhân, Tổ trưởng, Manager, Admin)
        if (role.equals("ROLE_OPERATOR") || role.equals("ROLE_LEADER") || 
            role.equals("ROLE_MANAGER") || role.equals("ROLE_ADMIN")) {
            
            List<MenuItemDto> prodChildren = new ArrayList<>();
            prodChildren.add(new MenuItemDto("1.1", "Nhập Báo Cáo Ca / 填寫報表", "edit_note", "/production/entry", null));
            
            if (!role.equals("ROLE_OPERATOR")) {
                prodChildren.add(new MenuItemDto("1.2", "Lịch Sử Trong Ngày / 当日記錄", "history", "/production/history", null));
            }

            menu.add(new MenuItemDto("1", "Báo Cáo Sản Xuất / 生產報表", "precision_manufacturing", "/production", prodChildren));
        }

        // 2. NHÓM GIÁM SÁT & BÁO CÁO (Tổ trưởng, Manager, Admin)
        if (role.equals("ROLE_LEADER") || role.equals("ROLE_MANAGER") || role.equals("ROLE_ADMIN")) {
            List<MenuItemDto> reportChildren = new ArrayList<>();
            reportChildren.add(new MenuItemDto("2.1", "Dashboard OEE Realtime", "dashboard", "/reports/oee-dashboard", null));
            reportChildren.add(new MenuItemDto("2.2", "Tra Cứu Báo Cáo / 查詢報表", "search", "/reports/search", null));
            reportChildren.add(new MenuItemDto("2.3", "Xuất Excel V9 / 導出Excel", "file_download", "/reports/export-v9", null));

            menu.add(new MenuItemDto("2", "Giám Sát & Báo Cáo / 監控與報表", "analytics", "/reports", reportChildren));
        }

        // 3. NHÓM QUẢN LÝ DANH MỤC (Manager, Admin)
        if (role.equals("ROLE_MANAGER") || role.equals("ROLE_ADMIN")) {
            List<MenuItemDto> masterChildren = new ArrayList<>();
            masterChildren.add(new MenuItemDto("3.1", "Mã Hàng & C/T / 料號與C/T", "inventory_2", "/master/products", null));
            masterChildren.add(new MenuItemDto("3.2", "Chuyền Sản Xuất / 線別", "view_stream", "/master/lines", null));
            masterChildren.add(new MenuItemDto("3.3", "Ca Làm Việc / 班別時數", "schedule", "/master/shifts", null));
            masterChildren.add(new MenuItemDto("3.4", "Máy / Thiết Bị / 機台", "build", "/master/machines", null));

            menu.add(new MenuItemDto("3", "Quản Lý Danh Mục / 主數據管理", "settings_applications", "/master", masterChildren));
        }

        // 4. NHÓM QUẢN TRỊ HỆ THỐNG (Chỉ Admin)
        if (role.equals("ROLE_ADMIN")) {
            List<MenuItemDto> systemChildren = new ArrayList<>();
            systemChildren.add(new MenuItemDto("4.1", "Tài Khoản & Quyền / 賬號權限", "manage_accounts", "/system/users", null));
            systemChildren.add(new MenuItemDto("4.2", "Nhật Ký Hệ Thống / 系統日誌", "receipt_long", "/system/logs", null));

            menu.add(new MenuItemDto("4", "Quản Trị Hệ Thống / 系統管理", "admin_panel_settings", "/system", systemChildren));
        }

        return menu;
    }
}