package com.stego.backend.service;

import com.stego.backend.dto.request.MenuItemRequest;
import com.stego.backend.dto.response.MenuItemResponse;
import com.stego.backend.entity.MenuItem;
import com.stego.backend.entity.Restaurant;
import com.stego.backend.enums.MenuCategory;
import com.stego.backend.exception.ResourceNotFoundException;
import com.stego.backend.exception.UnauthorizedException;
import com.stego.backend.repository.MenuItemRepository;
import com.stego.backend.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuItemService {

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Transactional
    public MenuItemResponse addMenuItem(Long restaurantId, Long ownerId, MenuItemRequest request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", restaurantId));

        if (!restaurant.getOwner().getId().equals(ownerId)) {
            throw new UnauthorizedException("You can only add items to your own restaurant");
        }

        MenuItem item = MenuItem.builder()
                .restaurant(restaurant)
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .price(request.getPrice())
                .prepTimeMinutes(request.getPrepTimeMinutes() != null ? request.getPrepTimeMinutes() : 15)
                .imageUrl(request.getImageUrl())
                .isAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true)
                .build();

        item = menuItemRepository.save(item);
        return mapToResponse(item);
    }

    @Transactional
    public MenuItemResponse updateMenuItem(Long itemId, Long ownerId, MenuItemRequest request) {
        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", itemId));

        if (!item.getRestaurant().getOwner().getId().equals(ownerId)) {
            throw new UnauthorizedException("You can only edit items in your own restaurant");
        }

        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setCategory(request.getCategory());
        item.setPrice(request.getPrice());
        if (request.getPrepTimeMinutes() != null) item.setPrepTimeMinutes(request.getPrepTimeMinutes());
        if (request.getImageUrl() != null) item.setImageUrl(request.getImageUrl());
        if (request.getIsAvailable() != null) item.setIsAvailable(request.getIsAvailable());

        item = menuItemRepository.save(item);
        return mapToResponse(item);
    }

    @Transactional
    public void deleteMenuItem(Long itemId, Long ownerId) {
        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", itemId));

        if (!item.getRestaurant().getOwner().getId().equals(ownerId)) {
            throw new UnauthorizedException("You can only delete items in your own restaurant");
        }

        // Soft delete by marking unavailable
        item.setIsAvailable(false);
        menuItemRepository.save(item);
    }

    public List<MenuItemResponse> getMenuByRestaurant(Long restaurantId) {
        return menuItemRepository.findByRestaurantIdAndIsAvailableTrue(restaurantId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<MenuItemResponse> getMenuByCategory(Long restaurantId, MenuCategory category) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", restaurantId));
        return menuItemRepository.findByRestaurantAndCategory(restaurant, category).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private MenuItemResponse mapToResponse(MenuItem item) {
        return MenuItemResponse.builder()
                .id(item.getId())
                .restaurantId(item.getRestaurant().getId())
                .name(item.getName())
                .description(item.getDescription())
                .category(item.getCategory())
                .price(item.getPrice())
                .prepTimeMinutes(item.getPrepTimeMinutes())
                .imageUrl(item.getImageUrl())
                .isAvailable(item.getIsAvailable())
                .build();
    }
}
