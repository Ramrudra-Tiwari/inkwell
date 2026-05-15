package com.inkwell.payment.client;

import com.razorpay.Order;
import com.razorpay.PaymentLink;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RazorpayGateway {

    private static final String AMOUNT = "amount";
    private static final String CURRENCY = "currency";
    private static final String INR = "INR";

    public Map<String, Object> createOrder(int amount, String keyId, String keySecret) throws RazorpayException {
        RazorpayClient razorpayClient = new RazorpayClient(keyId, keySecret);

        JSONObject orderRequest = new JSONObject();
        orderRequest.put(AMOUNT, amount);
        orderRequest.put(CURRENCY, INR);
        orderRequest.put("receipt", "inkwell_" + System.currentTimeMillis());
        orderRequest.put("payment_capture", 1);
        orderRequest.put("partial_payment", false);

        Order order = razorpayClient.orders.create(orderRequest);

        return Map.of(
                "id", order.get("id"),
                "orderId", order.get("id"),
                AMOUNT, amount,
                CURRENCY, INR,
                "keyId", keyId
        );
    }

    public Map<String, Object> createPaymentLink(int amount, String keyId, String keySecret) throws RazorpayException {
        RazorpayClient razorpayClient = new RazorpayClient(keyId, keySecret);

        JSONObject customer = new JSONObject();
        customer.put("name", "InkWell Supporter");
        customer.put("email", "supporter@example.com");
        customer.put("contact", "+919876543210");

        JSONObject notify = new JSONObject();
        notify.put("sms", false);
        notify.put("email", false);

        JSONObject notes = new JSONObject();
        notes.put("platform", "InkWell");
        notes.put("purpose", "Support author");

        JSONObject method = new JSONObject();
        method.put("card", true);
        method.put("netbanking", true);
        method.put("upi", true);
        method.put("wallet", true);

        JSONObject checkout = new JSONObject();
        checkout.put("method", method);

        JSONObject options = new JSONObject();
        options.put("checkout", checkout);

        JSONObject linkRequest = new JSONObject();
        linkRequest.put(AMOUNT, amount);
        linkRequest.put(CURRENCY, INR);
        linkRequest.put("accept_partial", false);
        linkRequest.put("reference_id", "inkwell_" + System.currentTimeMillis());
        linkRequest.put("description", "Support author on InkWell");
        linkRequest.put("customer", customer);
        linkRequest.put("notify", notify);
        linkRequest.put("reminder_enable", false);
        linkRequest.put("notes", notes);
        linkRequest.put("options", options);

        PaymentLink paymentLink = razorpayClient.paymentLink.create(linkRequest);

        return Map.of(
                "id", paymentLink.get("id"),
                "shortUrl", paymentLink.get("short_url"),
                AMOUNT, amount,
                CURRENCY, INR
        );
    }
}
