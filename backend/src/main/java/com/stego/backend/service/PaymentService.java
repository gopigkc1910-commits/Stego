package com.stego.backend.service;

import com.stego.backend.entity.Order;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentService {

    @Value("${stripe.secret.key:sk_test_51Pxxxxxxxxxx}")
    private String stripeSecretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    /**
     * Creates a Stripe Payment Intent for an order.
     * Returns the client_secret which the frontend uses to complete the payment.
     */
    public String createPaymentIntent(Order order) throws StripeException {
        // Convert amount to cents (Stripe requirement)
        long amountInCents = order.getTotalAmount().multiply(new java.math.BigDecimal(100)).longValue();

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency("inr")
                .setDescription("Stego Order #" + order.getId())
                .putMetadata("order_id", String.valueOf(order.getId()))
                .putMetadata("user_email", order.getUser().getEmail())
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build()
                )
                .build();

        PaymentIntent intent = PaymentIntent.create(params);
        return intent.getClientSecret();
    }
}
