package com.stego.backend.controller;

import com.stego.backend.dto.request.ReviewRequest;
import com.stego.backend.dto.response.ApiResponse;
import com.stego.backend.dto.response.ReviewResponse;
import com.stego.backend.security.UserDetailsImpl;
import com.stego.backend.service.ReviewService;
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
@RequestMapping("/api/reviews")
@Tag(name = "Reviews", description = "Ratings and reviews for restaurants")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping
    @Operation(summary = "Submit a review for a completed order")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody ReviewRequest request) {
        ReviewResponse response = reviewService.createReview(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review submitted", response));
    }

    @GetMapping("/restaurant/{restaurantId}")
    @Operation(summary = "Get all reviews for a restaurant")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getRestaurantReviews(
            @PathVariable Long restaurantId) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.getRestaurantReviews(restaurantId)));
    }

    @GetMapping("/my")
    @Operation(summary = "Get current user's reviews")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getMyReviews(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.getUserReviews(userDetails.getId())));
    }
}
