package com.inkwell.payment;

public class PaymentRecord {

    private String paymentId;
    private String orderId;
    private Integer supporterUserId;
    private String supporterName;
    private String supporterEmail;
    private Integer authorId;
    private String authorName;
    private Integer postId;
    private String postTitle;
    private Integer amount;
    private String currency;
    private String status;
    private String paidAt;

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Integer getSupporterUserId() {
        return supporterUserId;
    }

    public void setSupporterUserId(Integer supporterUserId) {
        this.supporterUserId = supporterUserId;
    }

    public String getSupporterName() {
        return supporterName;
    }

    public void setSupporterName(String supporterName) {
        this.supporterName = supporterName;
    }

    public String getSupporterEmail() {
        return supporterEmail;
    }

    public void setSupporterEmail(String supporterEmail) {
        this.supporterEmail = supporterEmail;
    }

    public Integer getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Integer authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public Integer getPostId() {
        return postId;
    }

    public void setPostId(Integer postId) {
        this.postId = postId;
    }

    public String getPostTitle() {
        return postTitle;
    }

    public void setPostTitle(String postTitle) {
        this.postTitle = postTitle;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(String paidAt) {
        this.paidAt = paidAt;
    }
}
