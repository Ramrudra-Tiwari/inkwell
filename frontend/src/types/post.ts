export type PostStatus = "DRAFT" | "PUBLISHED" | "UNPUBLISHED" | "ARCHIVED";

export interface Post {
  postId: number;
  authorId: number;
  authorName?: string;
  authorAvatarUrl?: string | null;
  title: string;
  slug: string;
  content: string;
  excerpt: string;
  featuredImageUrl?: string | null;
  categorySlug?: string;
  tagSlugs?: string[];
  status: PostStatus;
  readTimeMin: number;
  viewCount: number;
  likesCount: number;
  commentCount?: number;
  isFeatured?: boolean;
  createdAt: string;
  updatedAt: string;
  publishedAt?: string | null;
}

export interface CreatePostPayload {
  authorId: number;
  title: string;
  content: string;
  excerpt?: string;
  featuredImageUrl?: string;
  status?: PostStatus;
}

export interface UpdatePostPayload {
  title?: string;
  content?: string;
  excerpt?: string;
  featuredImageUrl?: string;
  status?: PostStatus;
}

export interface PostApiError {
  timestamp?: string;
  status?: number;
  error?: string;
  message?: string;
  path?: string;
}
