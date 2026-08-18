package com.swico.swico.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MenuItemDto {
    private String id;
    private String title;       // Tên hiển thị (Việt - Trung)
    private String icon;        // Tên Icon (Lucide / Material)
    private String path;        // Đường dẫn URL
    private List<MenuItemDto> children; // Menu con

    public MenuItemDto() {
    }

    public MenuItemDto(String id, String title, String icon, String path, List<MenuItemDto> children) {
        this.id = id;
        this.title = title;
        this.icon = icon;
        this.path = path;
        this.children = children;
    }
}
