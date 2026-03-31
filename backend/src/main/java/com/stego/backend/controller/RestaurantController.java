package com.stego.backend.controller;

import com.stego.backend.dto.request.RestaurantRequest;
import com.stego.backend.dto.response.ApiResponse;
import com.stego.backend.dto.response.RestaurantResponse;
import com.stego.backend.security.UserDetailsImpl;
import com.stego.backend.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@Tag(name = "Restaurants", description = "Restaurant management and discovery")
public class RestaurantController {

    @Autowired
    private RestaurantService restaurantService;

    @PostMapping
    @Operation(summary = "Register a new restaurant (Owner only)")
    public ResponseEntity<ApiResponse<RestaurantResponse>> create(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody RestaurantRequest request) {
        RestaurantResponse response = restaurantService.createRestaurant(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Restaurant created", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update restaurant details (Owner only)")
    public ResponseEntity<ApiResponse<RestaurantResponse>> update(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody RestaurantRequest request) {
        RestaurantResponse response = restaurantService.updateRestaurant(id, userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Restaurant updated", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get restaurant details")
    public ResponseEntity<ApiResponse<RestaurantResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(restaurantService.getRestaurant(id)));
    }

    @GetMapping
    @Operation(summary = "Browse all active restaurants")
    public ResponseEntity<ApiResponse<List<RestaurantResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(restaurantService.getAllRestaurants()));
    }

    @GetMapping("/nearby")
    @Operation(summary = "Find nearby restaurants by coordinates")
    public ResponseEntity<ApiResponse<List<RestaurantResponse>>> getNearby(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "5") double radiusKm) {
        return ResponseEntity.ok(ApiResponse.success(restaurantService.getNearbyRestaurants(lat, lng, radiusKm)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search restaurants by name")
    public ResponseEntity<ApiResponse<List<RestaurantResponse>>> search(@RequestParam String q) {
        return ResponseEntity.ok(ApiResponse.success(restaurantService.searchRestaurants(q)));
    }

    @GetMapping("/my")
    @Operation(summary = "Get current owner's restaurants")
    public ResponseEntity<ApiResponse<List<RestaurantResponse>>> getMyRestaurants(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(ApiResponse.success(restaurantService.getOwnerRestaurants(userDetails.getId())));
    }

    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Toggle restaurant open/closed status")
    public ResponseEntity<ApiResponse<Void>> toggleOpen(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam boolean isOpen) {
        restaurantService.toggleOpen(id, userDetails.getId(), isOpen);
        return ResponseEntity.ok(ApiResponse.success("Restaurant status updated", null));
    }
}
