export interface UploadResponse {
  mediaId: number;
  originalName: string;
  url: string;
  mimeType: string;
  sizeKb: number;
  uploadedAt: string;
  message?: string;
  statusCode?: number;
}

export interface Media {
  mediaId: number;
  uploaderId: number;
  filename: string;
  originalName: string;
  url: string;
  mimeType: string;
  sizeKb: number;
  altText?: string | null;
  linkedPostId?: number | null;
  uploadedAt: string;
  isDeleted: boolean;
}
