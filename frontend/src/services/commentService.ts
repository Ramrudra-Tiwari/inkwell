import apiClient from "../api/axiosInstance";
import { Comment, CommentRequestPayload } from "../types/comment";

const commentService = {
  add: async (payload: CommentRequestPayload) => {
    const { data } = await apiClient.post<Comment>("/api/v1/comments", payload);
    return data;
  },

  getByPost: async (postId: number) => {
    const { data } = await apiClient.get<Comment[]>(`/api/v1/comments/post/${postId}`);
    return data;
  },

  getByPostForModeration: async (postId: number) => {
    const { data } = await apiClient.get<Comment[]>(`/api/v1/comments/post/${postId}/moderation`);
    return data;
  },

  delete: async (commentId: number) => {
    await apiClient.delete(`/api/v1/comments/${commentId}`);
  },

  moderateDelete: async (commentId: number) => {
    await apiClient.post(`/api/v1/comments/${commentId}/moderate-delete`);
  },

  like: async (commentId: number, userId: number) => {
    const response = await apiClient.post(`/api/v1/comments/${commentId}/like`, null, {
      headers: { "X-User-Id": userId }
    });
    return response.status;
  },

  update: async (commentId: number, payload: Partial<Comment>) => {
    const { data } = await apiClient.put<Comment>(`/api/v1/comments/${commentId}`, payload);
    return data;
  },

  updateStatus: async (commentId: number, status: Comment["status"]) => {
    if (status === "APPROVED") {
      await apiClient.post(`/api/v1/comments/${commentId}/approve`);
      return;
    }

    if (status === "REJECTED") {
      await apiClient.post(`/api/v1/comments/${commentId}/reject`);
      return;
    }

    throw new Error(`Unsupported moderation status: ${status}`);
  },

  getByAuthor: async (authorId: number) => {
    const { data } = await apiClient.get<Comment[]>(`/api/v1/comments/author/${authorId}`);
    return data;
  }
};

export default commentService;
