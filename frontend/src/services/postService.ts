import apiClient from "../api/axiosInstance";
import { CreatePostPayload, Post, PostStatus, UpdatePostPayload } from "../types/post";

const postService = {
  create: async (payload: CreatePostPayload) => {
    const { data } = await apiClient.post<Post>("/api/v1/posts", payload);
    return data;
  },

  getAll: async () => {
    const { data } = await apiClient.get<Post[]>("/api/v1/posts");
    return data;
  },

  getPublished: async () => {
    const { data } = await apiClient.get<Post[]>("/api/v1/posts/published");
    return data;
  },

  getBySlug: async (slug: string) => {
    const { data } = await apiClient.get<Post>(`/api/v1/posts/slug/${slug}`);
    return data;
  },

  getById: async (postId: number) => {
    const { data } = await apiClient.get<Post>(`/api/v1/posts/${postId}`);
    return data;
  },

  getByAuthor: async (authorId: number) => {
    const { data } = await apiClient.get<Post[]>(`/api/v1/posts/author/${authorId}`);
    return data;
  },

  getByAuthorAndStatus: async (authorId: number, status: PostStatus) => {
    const { data } = await apiClient.get<Post[]>(`/api/v1/posts/author/${authorId}/status/${status}`);
    return data;
  },

  update: async (postId: number, payload: UpdatePostPayload) => {
    const { data } = await apiClient.put<Post>(`/api/v1/posts/${postId}`, payload);
    return data;
  },

  search: async (keyword: string) => {
    try {
      const { data } = await apiClient.get<Post[] | { results?: Post[] }>("/api/v1/posts/search", {
        params: { keyword }
      });

      if (Array.isArray(data)) {
        return data;
      }

      if (Array.isArray(data.results)) {
        return data.results;
      }
    } catch {
      // fallback below
    }

    const { data } = await apiClient.get<Post[]>("/api/v1/posts/search", {
      params: { keyword }
    });
    return data;
  },
  
  delete: async (postId: number) => {
    const { data } = await apiClient.delete(`/api/v1/posts/${postId}`);
    return data;
  },

  incrementViewCount: async (postId: number) => {
    const { data } = await apiClient.post(`/api/v1/posts/${postId}/view`);
    return data;
  },

  like: async (postId: number, userId: number) => {
    const response = await apiClient.post(`/api/v1/posts/${postId}/like`, null, {
      headers: { "X-User-Id": String(userId) }
    });
    return response.status;
  },

  unlike: async (postId: number, userId: number) => {
    const { data } = await apiClient.post(`/api/v1/posts/${postId}/unlike`, null, {
      headers: { "X-User-Id": String(userId) }
    });
    return data;
  }
};

export default postService;
