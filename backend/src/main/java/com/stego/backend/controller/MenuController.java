package com.stego.backend.controller;

import com.stego.backend.dto.request.MenuItemRequest;
import com.stego.backend.dto.response.ApiResponse;
import com.stego.backend.dto.response.MenuItemResponse;
import com.stego.backend.enums.MenuCategory;
import com.stego.backend.security.UserDetailsImpl;
import com.stego.backend.service.MenuItemService;
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
@Tag(name = "Menu Items", description = "Restaurant menu management")
public class MenuController {

    @Autowired
    private MenuItemService menuItemService;

    @GetMapping("/api/restaurants/{restaurantId}/menu")
    @Operation(summary = "Get restaurant menu (public)")
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getMenu(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(ApiResponse.success(menuItemService.getMenuByRestaurant(restaurantId)));
    }

    @GetMapping("/api/restaurants/{restaurantId}/menu/filter")
    @Operation(summary = "Get menu items by category")
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getMenuByCategory(
            @PathVariable Long restaurantId,
            @RequestParam MenuCategory category) {
        return ResponseEntity.ok(ApiResponse.success(menuItemService.getMenuByCategory(restaurantId, category)));
    }

    @PostMapping("/api/restaurants/{restaurantId}/menu")
    @Operation(summary = "Add menu item (Owner only)")
    public ResponseEntity<ApiResponse<MenuItemResponse>> addItem(
            @PathVariable Long restaurantId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody MenuItemRequest request) {
        MenuItemResponse response = menuItemService.addMenuItem(restaurantId, userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Menu item added", response));
    }

    @PutMapping("/api/menu/{itemId}")
    @Operation(summary = "Update menu item (Owner only)")
    public ResponseEntity<ApiResponse<MenuItemResponse>> updateItem(
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody MenuItemRequest request) {
        MenuItemResponse response = menuItemService.updateMenuItem(itemId, userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Menu item updated", response));
    }

    @DeleteMapping("/api/menu/{itemId}")
    @Operation(summary = "Delete menu item (Owner only)")
    public ResponseEntity<ApiResponse<Void>> deleteItem(
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        menuItemService.deleteMenuItem(itemId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Menu item deleted", null));
    }
}
