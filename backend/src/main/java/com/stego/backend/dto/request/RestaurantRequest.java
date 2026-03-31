package com.stego.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalTime;

@Data
public class RestaurantRequest {

    @NotBlank(message = "Restaurant name is required")
    @Size(max = 200)
    private String name;

    @Size(max = 1000)
    private String description;

    @NotBlank(message = "Address is required")
    @Size(max = 500)
    private String address;

    private Double latitude;
    private Double longitude;
    private String phone;
    private String imageUrl;
    private LocalTime openingTime;
    private LocalTime closingTime;
}
