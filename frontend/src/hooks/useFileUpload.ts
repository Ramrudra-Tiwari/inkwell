import { useEffect, useMemo, useState } from "react";
import { isAxiosError } from "axios";
import mediaService from "../services/mediaService";
import { UploadResponse } from "../types/media";

const ACCEPTED_FILE_TYPES = ["image/jpeg", "image/png", "image/gif", "image/webp"];
const MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024;

interface UseFileUploadOptions {
  uploaderId?: number;
}

export const useFileUpload = ({ uploaderId }: UseFileUploadOptions) => {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState("");
  const [progress, setProgress] = useState(0);
  const [isUploading, setIsUploading] = useState(false);
  const [error, setError] = useState("");

  const resetSelection = () => {
    setSelectedFile(null);
    setPreviewUrl("");
    setProgress(0);
  };

  const validateFile = (file: File) => {
    if (!uploaderId) {
      return "Please sign in before uploading media.";
    }

    if (!ACCEPTED_FILE_TYPES.includes(file.type)) {
      return "Only JPEG, PNG, GIF, and WebP images are allowed.";
    }

    if (file.size > MAX_FILE_SIZE_BYTES) {
      return "Image size must be 10MB or smaller.";
    }

    return "";
  };

  const selectFile = (file: File | null) => {
    if (!file) {
      return false;
    }

    const validationMessage = validateFile(file);

    if (validationMessage) {
      setError(validationMessage);
      resetSelection();
      return false;
    }

    setError("");
    setSelectedFile(file);
    setPreviewUrl(URL.createObjectURL(file));
    setProgress(0);
    return true;
  };

  const upload = async (altText?: string): Promise<UploadResponse | null> => {
    if (!selectedFile || !uploaderId) {
      setError("Please choose an image before uploading.");
      return null;
    }

    try {
      setIsUploading(true);
      setError("");

      const uploadedMedia = await mediaService.upload({
        file: selectedFile,
        uploaderId,
        altText,
        onUploadProgress: (progressEvent) => {
          const total = progressEvent.total ?? selectedFile.size;
          const nextProgress = total > 0 ? Math.round((progressEvent.loaded * 100) / total) : 0;
          setProgress(nextProgress);
        }
      });

      setProgress(100);
      return uploadedMedia;
    } catch (uploadError) {
      if (isAxiosError(uploadError)) {
        const responseData = uploadError.response?.data as { message?: string } | string | undefined;
        setError(
          typeof responseData === "string"
            ? responseData
            : responseData?.message ?? "Upload failed."
        );
      } else {
        setError("Upload failed.");
      }

      return null;
    } finally {
      setIsUploading(false);
    }
  };

  const acceptedTypes = useMemo(() => ACCEPTED_FILE_TYPES.join(","), []);

  useEffect(() => {
    return () => {
      if (previewUrl) {
        URL.revokeObjectURL(previewUrl);
      }
    };
  }, [previewUrl]);

  return {
    acceptedTypes,
    error,
    isUploading,
    previewUrl,
    progress,
    selectedFile,
    resetSelection,
    selectFile,
    setError,
    upload
  };
};

export default useFileUpload;
