package com.stego.backend.repository;

import com.stego.backend.entity.MenuItem;
import com.stego.backend.entity.Restaurant;
import com.stego.backend.enums.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findByRestaurantAndIsAvailableTrue(Restaurant restaurant);

    List<MenuItem> findByRestaurant(Restaurant restaurant);

    List<MenuItem> findByRestaurantAndCategory(Restaurant restaurant, MenuCategory category);

    List<MenuItem> findByRestaurantIdAndIsAvailableTrue(Long restaurantId);
}
