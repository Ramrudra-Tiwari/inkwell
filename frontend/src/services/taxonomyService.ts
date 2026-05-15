import apiClient from "../api/axiosInstance";
import { Category, Tag, TaxonomyKind } from "../types/taxonomy";

const taxonomyService = {
  getCategories: async () => {
    const { data } = await apiClient.get<Category[]>("/api/v1/taxonomy/categories");
    return data;
  },

  getTags: async () => {
    const { data } = await apiClient.get<Tag[]>("/api/v1/taxonomy/tags");
    return data;
  },

  getTrending: async (limit = 10) => {
    const { data } = await apiClient.get<Tag[]>("/api/v1/posts/analytics/trending-tags", {
      params: { limit }
    });
    return data;
  },

  getBySlug: async (kind: TaxonomyKind, slug: string) => {
    if (kind === "category") {
      const { data } = await apiClient.get<Category>(`/api/v1/taxonomy/categories/slug/${slug}`);
      return data;
    }

    const { data } = await apiClient.get<Tag>(`/api/v1/taxonomy/tags/slug/${slug}`);
    return data;
  }
};

export const getAllCategories = taxonomyService.getCategories;
export const getTrendingTags = taxonomyService.getTrending;

export default taxonomyService;
