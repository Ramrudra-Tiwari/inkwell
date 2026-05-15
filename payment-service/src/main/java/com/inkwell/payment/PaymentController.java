package com.inkwell.payment;

import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;
    private final String keyId;
    private final String keySecret;

    public PaymentController(
            PaymentService paymentService,
            @Value("${razorpay.key-id}") String keyId,
            @Value("${razorpay.key-secret}") String keySecret) {
        this.paymentService = paymentService;
        this.keyId = keyId;
        this.keySecret = keySecret;
    }

    @PostMapping("/create-order")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, Object> request) {
        int amount = ((Number) request.get("amount")).intValue();
        ResponseEntity<Map<String, Object>> invalidResponse = validatePaymentRequest(amount);
        if (invalidResponse != null) {
            return invalidResponse;
        }

        try {
            return ResponseEntity.ok(paymentService.createOrder(amount, keyId, keySecret));
        } catch (RazorpayException ex) {
            log.error("Razorpay order creation failed: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "failure",
                    "message", "Unable to start Razorpay checkout. Check API keys and Razorpay test account payment methods."
            ));
        }
    }

    @PostMapping("/create-link")
    public ResponseEntity<Map<String, Object>> createPaymentLink(@RequestBody Map<String, Object> request) {
        int amount = ((Number) request.get("amount")).intValue();
        ResponseEntity<Map<String, Object>> invalidResponse = validatePaymentRequest(amount);
        if (invalidResponse != null) {
            return invalidResponse;
        }

        try {
            return ResponseEntity.ok(paymentService.createPaymentLink(amount, keyId, keySecret));
        } catch (RazorpayException ex) {
            log.error("Razorpay payment link creation failed: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "failure",
                    "message", "Unable to create Razorpay payment link. Check API keys and Razorpay test account payment methods."
            ));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verify(@RequestBody Map<String, Object> request) {
        boolean valid = paymentService.verifyPayment(
                (String) request.get("razorpay_order_id"),
                (String) request.get("razorpay_payment_id"),
                (String) request.get("razorpay_signature")
        );

        if (valid) {
            paymentService.recordSuccessfulPayment(request);
            return ResponseEntity.ok(Map.of("status", "success"));
        }

        return ResponseEntity.badRequest().body(Map.of("status", "failure"));
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<List<PaymentRecord>> getPaymentHistory(@PathVariable Integer userId) {
        return ResponseEntity.ok(paymentService.getPaymentHistory(userId));
    }

    private ResponseEntity<Map<String, Object>> validatePaymentRequest(int amount) {
        if (amount < 100) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "failure",
                    "message", "Amount must be at least INR 1"
            ));
        }

        if (keyId == null || keyId.isBlank() || keySecret == null || keySecret.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "failure",
                    "message", "Razorpay test keys are missing. Set RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET before starting payment-service."
            ));
        }

        return null;
    }

    @PostMapping("/test-success")
    public ResponseEntity<Map<String, Object>> testSuccess(@RequestBody Map<String, Object> request) {
        int amount = ((Number) request.get("amount")).intValue();
        if (amount < 100) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "failure",
                    "message", "Amount must be at least INR 1"
            ));
        }

        String paymentId = "test_pay_" + System.currentTimeMillis();
        log.info("Payment Success");

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "paymentId", paymentId,
                "amount", amount,
                "currency", "INR"
        ));
    }
}
