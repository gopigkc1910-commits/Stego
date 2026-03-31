package com.stego.backend.dto.response;

import com.stego.backend.enums.MenuCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemResponse {

    private Long id;
    private Long restaurantId;
    private String name;
    private String description;
    private MenuCategory category;
    private BigDecimal price;
    private Integer prepTimeMinutes;
    private String imageUrl;
    private Boolean isAvailable;
}
