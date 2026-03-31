package com.stego.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantResponse {

    private Long id;
    private String name;
    private String description;
    private String address;
    private Double latitude;
    private Double longitude;
    private String phone;
    private String imageUrl;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private Boolean isOpen;
    private Double avgRating;
    private Integer totalReviews;
    private Double distanceKm;
    private LocalDateTime createdAt;
}
