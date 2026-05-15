package com.inkwell.payment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentHistoryStore paymentHistoryStore;

    @Mock
    private com.inkwell.payment.client.RazorpayGateway razorpayGateway;

    @Test
    void verifyPayment_returnsFalseForInvalidSignatureInput() {
        PaymentService paymentService = new PaymentService(paymentHistoryStore, razorpayGateway);

        assertFalse(paymentService.verifyPayment("order_1", "pay_1", "bad-signature"));
    }

    @Test
    void recordSuccessfulPayment_mapsRequestIntoPaymentRecord() {
        PaymentService paymentService = new PaymentService(paymentHistoryStore, razorpayGateway);

        paymentService.recordSuccessfulPayment(Map.ofEntries(
                Map.entry("razorpay_payment_id", "pay_123"),
                Map.entry("razorpay_order_id", "order_123"),
                Map.entry("supporterUserId", "7"),
                Map.entry("supporterName", "Supporter"),
                Map.entry("supporterEmail", "supporter@inkwell.com"),
                Map.entry("authorId", 15),
                Map.entry("authorName", "Author"),
                Map.entry("postId", 44),
                Map.entry("postTitle", "Story"),
                Map.entry("amount", 2500),
                Map.entry("currency", "INR")
        ));

        ArgumentCaptor<PaymentRecord> captor = ArgumentCaptor.forClass(PaymentRecord.class);
        verify(paymentHistoryStore).save(captor.capture());

        PaymentRecord paymentHistoryEntry = captor.getValue();
        assertEquals("pay_123", paymentHistoryEntry.getPaymentId());
        assertEquals("order_123", paymentHistoryEntry.getOrderId());
        assertEquals(7, paymentHistoryEntry.getSupporterUserId());
        assertEquals(15, paymentHistoryEntry.getAuthorId());
        assertEquals(44, paymentHistoryEntry.getPostId());
        assertEquals(2500, paymentHistoryEntry.getAmount());
        assertEquals("SUCCESS", paymentHistoryEntry.getStatus());
    }

    @Test
    void getPaymentHistory_returnsStoreRecords() {
        PaymentService paymentService = new PaymentService(paymentHistoryStore, razorpayGateway);
        PaymentRecord paymentHistoryEntry = new PaymentRecord();
        paymentHistoryEntry.setSupporterUserId(7);
        when(paymentHistoryStore.getBySupporterUserId(7)).thenReturn(List.of(paymentHistoryEntry));

        List<PaymentRecord> history = paymentService.getPaymentHistory(7);

        assertEquals(1, history.size());
        assertEquals(7, history.get(0).getSupporterUserId());
    }

    @Test
    void createOrder_delegatesToGateway() throws Exception {
        PaymentService paymentService = new PaymentService(paymentHistoryStore, razorpayGateway);
        when(razorpayGateway.createOrder(100, "key", "secret")).thenReturn(Map.of("orderId", "order_123"));

        assertEquals("order_123", paymentService.createOrder(100, "key", "secret").get("orderId"));
    }

    @Test
    void createPaymentLink_delegatesToGateway() throws Exception {
        PaymentService paymentService = new PaymentService(paymentHistoryStore, razorpayGateway);
        when(razorpayGateway.createPaymentLink(100, "key", "secret")).thenReturn(Map.of("shortUrl", "https://rzp.io/link"));

        assertEquals("https://rzp.io/link", paymentService.createPaymentLink(100, "key", "secret").get("shortUrl"));
    }
}
