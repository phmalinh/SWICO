package com.swico.swico.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemDto {
    private String id;
    private String title;       // Tên hiển thị (Việt - Trung)
    private String icon;        // Tên Icon (Lucide / Material)
    private String path;        // Đường dẫn URL
    private List<MenuItemDto> children; // Menu con
}