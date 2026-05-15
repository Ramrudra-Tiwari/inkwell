import { AxiosProgressEvent } from "axios";
import apiClient from "../api/axiosInstance";
import { Media, UploadResponse } from "../types/media";

const mediaService = {
  upload: async ({
    file,
    uploaderId,
    altText,
    onUploadProgress
  }: {
    file: File;
    uploaderId: number;
    altText?: string;
    onUploadProgress?: (progressEvent: AxiosProgressEvent) => void;
  }) => {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("uploaderId", String(uploaderId));

    if (altText?.trim()) {
      formData.append("altText", altText.trim());
    }

    const { data } = await apiClient.post<UploadResponse>("/api/v1/media/upload", formData, {
      headers: {
        "Content-Type": "multipart/form-data"
      },
      timeout: 15000,
      onUploadProgress
    });

    return data;
  },

  getById: async (mediaId: number) => {
    const { data } = await apiClient.get<Media>(`/api/v1/media/${mediaId}`);
    return data;
  },

  getByPost: async (postId: number) => {
    const { data } = await apiClient.get<Media[]>(`/api/v1/media/post/${postId}`);
    return data;
  },

  getByUploader: async (uploaderId: number) => {
    const { data } = await apiClient.get<Media[]>(`/api/v1/media/uploader/${uploaderId}`);
    return data.filter((media) => !media.isDeleted);
  },

  linkToPost: async (mediaId: number, postId: number) => {
    await apiClient.put(`/api/v1/media/${mediaId}/link/${postId}`);
  },

  unlinkFromPost: async (mediaId: number) => {
    await apiClient.put(`/api/v1/media/${mediaId}/unlink`);
  },

  delete: async (mediaId: number) => {
    await apiClient.delete(`/api/v1/media/${mediaId}`);
  }
};

export default mediaService;
