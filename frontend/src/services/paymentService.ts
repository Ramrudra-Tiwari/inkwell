import apiClient from "../api/axiosInstance";
import { PaymentHistoryItem } from "../types/payment";

const paymentService = {
  getPaymentHistory: async (userId: number) => {
    const { data } = await apiClient.get<PaymentHistoryItem[]>(`/payments/history/${userId}`);
    return data;
  }
};

export default paymentService;
