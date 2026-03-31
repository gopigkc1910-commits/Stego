package com.stego.backend.service;

import com.stego.backend.entity.Order;
import com.stego.backend.entity.Restaurant;
import com.stego.backend.enums.OrderStatus;
import com.stego.backend.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EstimationService {

    @Autowired
    private OrderRepository orderRepository;

    /**
     * Smart wait-time estimation for order ready time.
     * Logic:
     * 1. Base item prep time (max of all items).
     * 2. Current queue weight (each active order adds a dynamic delay).
     * 3. Historical delay factor (actual vs estimated for this restaurant).
     */
    public LocalDateTime estimateReadyTime(Restaurant restaurant, int maxItemPrepTime) {
        // 1. Current Active Queue Weight
        List<OrderStatus> activeStatuses = List.of(OrderStatus.PENDING, OrderStatus.ACCEPTED, OrderStatus.PREPARING);
        long activeCount = orderRepository.countActiveOrders(restaurant.getId(), activeStatuses);
        
        // Dynamic weight: Each active order adds between 2-5 minutes based on restaurant load
        int queueWeight = (activeCount > 10) ? 5 : 3;
        int queueDelay = (int) (activeCount * queueWeight);

        // 2. Historical Performance (AI Delay Factor)
        // Get last 5 completed orders for this restaurant to calculate real-world speed
        List<Order> recentOrders = orderRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurant.getId()).stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED && o.getActualReadyTime() != null && o.getEstimatedReadyTime() != null)
                .limit(5)
                .collect(Collectors.toList());

        double avgDelayMinutes = 0;
        if (!recentOrders.isEmpty()) {
            long totalDelaySec = recentOrders.stream()
                    .mapToLong(o -> Duration.between(o.getEstimatedReadyTime(), o.getActualReadyTime()).getSeconds())
                    .sum();
            avgDelayMinutes = (totalDelaySec / (double) recentOrders.size()) / 60.0;
        }

        // 3. Final Calculation
        // base + queue delay + historical error margin (capped at 15 mins)
        int totalPredictedMinutes = maxItemPrepTime + queueDelay + (int) Math.max(0, Math.min(15, avgDelayMinutes));

        return LocalDateTime.now().plusMinutes(totalPredictedMinutes);
    }
}
