package com.inkwell.payment;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentControllerTest {

    @Test
    void createOrder_rejectsAmountBelowOneRupee() throws Exception {
        PaymentController controller = new PaymentController(mock(PaymentService.class), "key", "secret");

        ResponseEntity<Map<String, Object>> response = controller.createOrder(Map.of("amount", 99));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("failure", response.getBody().get("status"));
    }

    @Test
    void createOrder_returnsServiceOrderForValidAmount() throws Exception {
        PaymentService paymentService = mock(PaymentService.class);
        PaymentController controller = new PaymentController(paymentService, "key", "secret");
        when(paymentService.createOrder(100, "key", "secret")).thenReturn(Map.of("orderId", "order_123"));

        ResponseEntity<Map<String, Object>> response = controller.createOrder(Map.of("amount", 100));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("order_123", response.getBody().get("orderId"));
    }

    @Test
    void createPaymentLink_rejectsAmountBelowOneRupee() throws Exception {
        PaymentController controller = new PaymentController(mock(PaymentService.class), "key", "secret");

        ResponseEntity<Map<String, Object>> response = controller.createPaymentLink(Map.of("amount", 99));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("failure", response.getBody().get("status"));
    }

    @Test
    void createPaymentLink_returnsServiceLinkForValidAmount() throws Exception {
        PaymentService paymentService = mock(PaymentService.class);
        PaymentController controller = new PaymentController(paymentService, "key", "secret");
        when(paymentService.createPaymentLink(100, "key", "secret")).thenReturn(Map.of("shortUrl", "https://rzp.io/link"));

        ResponseEntity<Map<String, Object>> response = controller.createPaymentLink(Map.of("amount", 100));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("https://rzp.io/link", response.getBody().get("shortUrl"));
    }

    @Test
    void verify_recordsPaymentWhenSignatureIsValid() {
        PaymentService paymentService = mock(PaymentService.class);
        PaymentController controller = new PaymentController(paymentService, "key", "secret");
        Map<String, Object> request = Map.of(
                "razorpay_order_id", "order_123",
                "razorpay_payment_id", "pay_123",
                "razorpay_signature", "signature"
        );
        when(paymentService.verifyPayment("order_123", "pay_123", "signature")).thenReturn(true);

        ResponseEntity<Map<String, Object>> response = controller.verify(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("success", response.getBody().get("status"));
        verify(paymentService).recordSuccessfulPayment(request);
    }

    @Test
    void verify_returnsBadRequestWhenSignatureIsInvalid() {
        PaymentService paymentService = mock(PaymentService.class);
        PaymentController controller = new PaymentController(paymentService, "key", "secret");
        Map<String, Object> request = Map.of(
                "razorpay_order_id", "order_123",
                "razorpay_payment_id", "pay_123",
                "razorpay_signature", "bad"
        );
        when(paymentService.verifyPayment("order_123", "pay_123", "bad")).thenReturn(false);

        ResponseEntity<Map<String, Object>> response = controller.verify(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("failure", response.getBody().get("status"));
    }

    @Test
    void getPaymentHistory_returnsRecordsForUser() {
        PaymentService paymentService = mock(PaymentService.class);
        PaymentController controller = new PaymentController(paymentService, "key", "secret");
        PaymentRecord paymentHistoryEntry = new PaymentRecord();
        when(paymentService.getPaymentHistory(7)).thenReturn(List.of(paymentHistoryEntry));

        ResponseEntity<List<PaymentRecord>> response = controller.getPaymentHistory(7);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testSuccess_returnsSyntheticSuccessfulPayment() {
        PaymentController controller = new PaymentController(mock(PaymentService.class), "key", "secret");

        ResponseEntity<Map<String, Object>> response = controller.testSuccess(Map.of("amount", 100));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("success", response.getBody().get("status"));
        assertTrue(((String) response.getBody().get("paymentId")).startsWith("test_pay_"));
    }
}
