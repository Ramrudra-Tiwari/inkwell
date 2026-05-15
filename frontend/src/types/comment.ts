export type CommentStatus = "APPROVED" | "PENDING" | "REJECTED" | "DELETED";

export interface Comment {
  commentId: number;
  postId: number;
  authorId: number;
  parentCommentId: number | null;
  content: string;
  likesCount: number;
  status: CommentStatus;
  createdAt: string;
  updatedAt: string;
}

export interface CommentRequestPayload {
  postId: number;
  authorId: number;
  parentCommentId?: number | null;
  content: string;
}

export interface ThreadedComment extends Comment {
  replies: Comment[];
}

export interface CommentApiError {
  timestamp?: string;
  status?: number;
  error?: string;
  message?: string;
  path?: string;
}
