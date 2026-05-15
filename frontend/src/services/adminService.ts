import apiClient from "../api/axiosInstance";
import commentService from "./commentService";
import postService from "./postService";
import { Comment } from "../types/comment";
import { Post } from "../types/post";
import { Subscriber } from "../types/messaging";

export interface AdminAnalytics {
  totalPosts: number;
  publishedPosts: number;
  draftPosts: number;
  totalViews: number;
  totalLikes: number;
  totalComments: number;
}

export interface AdminUser {
  userId: number;
  fullName: string;
  email: string;
  role: "READER" | "AUTHOR" | "ADMIN";
  isActive: boolean;
}

export interface AuditLog {
  id: number;
  action: string;
  target: string;
  performedBy: string;
  details: string;
  timestamp: string;
}

export interface MediaFile {
  mediaId: number;
  filename: string;
  originalName: string;
  url: string;
  mimeType: string;
  sizeKb: number;
  uploadedAt: string;
  uploaderId: number;
  altText: string;
}

const adminService = {
  getAdminMessage: async () => {
    const { data } = await apiClient.get<string>("/api/v1/users/admin/dashboard");
    return data;
  },

  getUsers: async () => {
    try {
      const { data } = await apiClient.get<AdminUser[]>("/api/v1/users");
      return data;
    } catch {
      return [] as AdminUser[];
    }
  },

  getPlatformPosts: async () => {
    return postService.getAll();
  },

  getRecentComments: async () => {
    const posts = await postService.getAll();
    const commentResults = await Promise.allSettled(
      posts.slice(0, 8).map(async (post) => ({
        post,
        comments: await commentService.getByPostForModeration(post.postId)
      }))
    );

    return commentResults
      .filter((result): result is PromiseFulfilledResult<{ post: Post; comments: Comment[] }> => result.status === "fulfilled")
      .flatMap((result) =>
        result.value.comments.map((comment) => ({
          ...comment,
          postTitle: result.value.post.title
        }))
      )
      .sort((first, second) => new Date(second.createdAt).getTime() - new Date(first.createdAt).getTime())
      .slice(0, 12);
  },

  getAnalytics: async (): Promise<AdminAnalytics> => {
    const posts = await postService.getAll();
    const commentsByPost = await Promise.allSettled(posts.slice(0, 8).map((post) => commentService.getByPost(post.postId)));

    return {
      totalPosts: posts.length,
      publishedPosts: posts.filter((post) => post.status === "PUBLISHED").length,
      draftPosts: posts.filter((post) => post.status === "DRAFT").length,
      totalViews: posts.reduce((sum, post) => sum + post.viewCount, 0),
      totalLikes: posts.reduce((sum, post) => sum + post.likesCount, 0),
      totalComments: commentsByPost.reduce((sum, result) => {
        if (result.status !== "fulfilled") {
          return sum;
        }

        return sum + result.value.length;
      }, 0)
    };
  },

  updateUserRole: async (userId: number, role: string) => {
    await apiClient.put(`/api/v1/users/${userId}/role`, { role });
  },

  toggleUserStatus: async (userId: number, isActive: boolean) => {
    await apiClient.put(`/api/v1/users/${userId}/status`, { isActive });
  },

  deleteUser: async (userId: number) => {
    await apiClient.delete(`/api/v1/users/${userId}`);
  },

  togglePostPin: async (postId: number, isFeatured: boolean) => {
    await apiClient.post(`/api/v1/posts/${postId}/feature`, null, {
      params: { isFeatured }
    });
  },

  deletePost: async (postId: number) => {
    await postService.delete(postId);
  },

  createCategory: async (category: any) => {
    const { data } = await apiClient.post("/api/v1/taxonomy/categories", category);
    return data;
  },

  deleteCategory: async (id: number) => {
    await apiClient.delete(`/api/v1/taxonomy/categories/${id}`);
  },

  broadcastCampaign: async (subject: string, body: string, targetStatus?: string, targetPreferences?: string) => {
    await apiClient.post("/api/v1/newsletter/campaign", { 
      subject, 
      body,
      targetStatus,
      targetPreferences
    });
  },

  deleteTag: async (id: number) => {
    await apiClient.delete(`/api/v1/taxonomy/tags/${id}`);
  },
  
  getSubscribers: async (): Promise<Subscriber[]> => {
    const { data } = await apiClient.get<Subscriber[]>("/api/v1/newsletter/subscribers/all");
    return data;
  },

  approveSubscriber: async (subscriberId: number) => {
    try {
      const { data } = await apiClient.post(`/api/v1/newsletter/subscribers/${subscriberId}/approve`);
      return data;
    } catch (error: any) {
      if (error?.response?.status !== 404) {
        throw error;
      }

      const { data } = await apiClient.post(`/api/v1/newsletter/subscriber/${subscriberId}/approve`);
      return data;
    }
  },

  broadcastNotification: async (title: string, message: string, role: string = "ALL") => {
    await apiClient.post("/api/v1/notifications/broadcast", null, {
      params: { title, message, role }
    });
  },

  getAuditLogs: async (): Promise<AuditLog[]> => {
    try {
      const { data } = await apiClient.get<AuditLog[]>("/api/v1/users/audit-logs");
      return data;
    } catch {
      return [];
    }
  },

  getMediaLibrary: async (): Promise<MediaFile[]> => {
    const { data } = await apiClient.get<MediaFile[]>("/api/v1/media/all");
    return data;
  },

  deleteMedia: async (mediaId: number) => {
    await apiClient.delete(`/api/v1/media/${mediaId}`);
  },

  toggleModerationMode: async (required: boolean) => {
    await apiClient.post("/api/v1/comments/settings/moderation", null, {
      params: { required }
    });
  },

  getModerationMode: async (): Promise<boolean> => {
    const { data } = await apiClient.get<boolean>("/api/v1/comments/settings/moderation");
    return data;
  }
};

export default adminService;
