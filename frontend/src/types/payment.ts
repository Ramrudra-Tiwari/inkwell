export interface PaymentHistoryItem {
  paymentId: string;
  orderId: string;
  supporterUserId?: number | null;
  supporterName?: string | null;
  supporterEmail?: string | null;
  authorId?: number | null;
  authorName?: string | null;
  postId?: number | null;
  postTitle?: string | null;
  amount: number;
  currency: string;
  status: string;
  paidAt: string;
}
