export interface Notification {
  notificationId: number;
  recipientId: number;
  actorId?: number | null;
  type: string;
  title: string;
  message: string;
  isRead: boolean;
  createdAt: string;
}

export interface SubscriptionRequestPayload {
  email: string;
  userId?: number;
  preferences?: string;
}

export interface SubscriptionResponse {
  subscriberId: number;
  email: string;
  status: "PENDING" | "ACTIVE" | "UNSUBSCRIBED";
  message: string;
  statusCode?: number;
}

export interface Subscriber {
  subscriberId: number;
  email: string;
  userId?: number | null;
  status: "PENDING" | "ACTIVE" | "UNSUBSCRIBED";
  token?: string;
  preferences?: string | null;
  subscribedAt: string;
}
