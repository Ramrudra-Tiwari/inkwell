package com.inkwell.payment;

import com.razorpay.Utils;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    @Value("${razorpay.key-secret}")
    private String keySecret;

    private final PaymentHistoryStore paymentHistoryStore;
    private final com.inkwell.payment.client.RazorpayGateway razorpayGateway;

    public PaymentService(PaymentHistoryStore paymentHistoryStore, com.inkwell.payment.client.RazorpayGateway razorpayGateway) {
        this.paymentHistoryStore = paymentHistoryStore;
        this.razorpayGateway = razorpayGateway;
    }

    public Map<String, Object> createOrder(int amount, String keyId, String keySecret) throws RazorpayException {
        return razorpayGateway.createOrder(amount, keyId, keySecret);
    }

    public Map<String, Object> createPaymentLink(int amount, String keyId, String keySecret) throws RazorpayException {
        return razorpayGateway.createPaymentLink(amount, keyId, keySecret);
    }

    public boolean verifyPayment(String orderId, String paymentId, String signature) {
        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", orderId);
            options.put("razorpay_payment_id", paymentId);
            options.put("razorpay_signature", signature);

            boolean valid = Utils.verifyPaymentSignature(options, keySecret);
            if (valid) {
                log.info("Payment Success");
            }
            return valid;
        } catch (Exception e) {
            return false;
        }
    }

    public void recordSuccessfulPayment(Map<String, Object> request) {
        PaymentRecord record = new PaymentRecord();
        record.setPaymentId((String) request.get("razorpay_payment_id"));
        record.setOrderId((String) request.get("razorpay_order_id"));
        record.setSupporterUserId(getInteger(request.get("supporterUserId")));
        record.setSupporterName((String) request.get("supporterName"));
        record.setSupporterEmail((String) request.get("supporterEmail"));
        record.setAuthorId(getInteger(request.get("authorId")));
        record.setAuthorName((String) request.get("authorName"));
        record.setPostId(getInteger(request.get("postId")));
        record.setPostTitle((String) request.get("postTitle"));
        record.setAmount(getInteger(request.get("amount")));
        record.setCurrency((String) request.getOrDefault("currency", "INR"));
        record.setStatus("SUCCESS");
        record.setPaidAt(OffsetDateTime.now().toString());
        paymentHistoryStore.save(record);
    }

    public List<PaymentRecord> getPaymentHistory(Integer supporterUserId) {
        return paymentHistoryStore.getBySupporterUserId(supporterUserId);
    }

    private Integer getInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            return Integer.parseInt(text);
        }
        return null;
    }
}
