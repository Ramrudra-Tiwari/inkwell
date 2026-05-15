import apiClient from "../api/axiosInstance";
import {
  Notification,
  Subscriber,
  SubscriptionRequestPayload,
  SubscriptionResponse
} from "../types/messaging";

const messagingService = {
  getNotificationsForUser: async (userId: number) => {
    const { data } = await apiClient.get<Notification[]>(`/api/v1/notifications/user/${userId}`);
    return data;
  },

  getUnreadCountForUser: async (userId: number) => {
    const { data } = await apiClient.get<number>(`/api/v1/notifications/user/${userId}/unread/count`);
    return data;
  },

  markNotificationAsRead: async (notificationId: number) => {
    await apiClient.put(`/api/v1/notifications/${notificationId}/read`);
  },

  markAllNotificationsAsRead: async (userId: number) => {
    await apiClient.put(`/api/v1/notifications/user/${userId}/read-all`);
  },

  subscribeToNewsletter: async (payload: SubscriptionRequestPayload) => {
    const { data } = await apiClient.post<SubscriptionResponse>("/api/v1/newsletter/subscribe", payload);
    return data;
  },

  confirmSubscription: async (token: string) => {
    const { data } = await apiClient.get<SubscriptionResponse>("/api/v1/newsletter/confirm", {
      params: { token }
    });
    return data;
  },

  getSubscriberByEmail: async (email: string) => {
    const { data } = await apiClient.get<Subscriber>("/api/v1/newsletter/subscriber", {
      params: { email }
    });
    return data;
  },
  
  deleteNotification: async (notificationId: number) => {
    await apiClient.delete(`/api/v1/notifications/${notificationId}`);
  },

  unsubscribeFromNewsletter: async (email: string, token: string) => {
    await apiClient.post("/api/v1/newsletter/unsubscribe", null, {
      params: { email, token }
    });
  },
  
  sendNewsletterCampaign: async (subject: string, body: string) => {
    const { data } = await apiClient.post<void>("/api/v1/newsletter/campaign", {
      subject,
      body
    });
    return data;
  }
};

export default messagingService;
