import { isAxiosError } from "axios";
import { ChangeEvent, useRef, useState } from "react";
import mediaService from "../services/mediaService";
import { UploadResponse } from "../types/media";

interface ImageUploaderProps {
  uploaderId?: number;
  altText: string;
  onUploadComplete: (upload: UploadResponse) => void;
}

const acceptedTypes = ["image/jpeg", "image/png", "image/gif", "image/webp"];
const maxFileSizeBytes = 10 * 1024 * 1024;

const ImageUploader = ({ uploaderId, altText, onUploadComplete }: ImageUploaderProps) => {
  const inputRef = useRef<HTMLInputElement | null>(null);
  const [isUploading, setIsUploading] = useState(false);
  const [error, setError] = useState("");

  const handleFileSelection = async (event: ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];

    if (!file) {
      return;
    }

    if (!uploaderId) {
      setError("Please sign in before uploading media.");
      return;
    }

    if (!acceptedTypes.includes(file.type)) {
      setError("Only JPEG, PNG, GIF, and WebP images are allowed.");
      return;
    }

    if (file.size > maxFileSizeBytes) {
      setError("Image size must be 10MB or smaller.");
      return;
    }

    try {
      setIsUploading(true);
      setError("");
      const upload = await mediaService.upload({
        file,
        uploaderId,
        altText
      });
      onUploadComplete(upload);
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
    } finally {
      setIsUploading(false);
      if (inputRef.current) {
        inputRef.current.value = "";
      }
    }
  };

  return (
    <div className="space-y-3">
      <label className="mb-2 block text-xs uppercase tracking-[0.22em] text-ink/70">
        Featured Image
      </label>

      <div className="rounded-2xl border border-dashed border-ink/25 bg-white/80 p-4">
        <input
          ref={inputRef}
          type="file"
          accept="image/jpeg,image/png,image/gif,image/webp"
          onChange={handleFileSelection}
          className="block w-full text-sm text-ink file:mr-4 file:rounded-full file:border-0 file:bg-ink file:px-4 file:py-2 file:text-xs file:font-semibold file:uppercase file:tracking-[0.18em] file:text-parchment hover:file:bg-forest"
        />
        <p className="mt-3 text-xs leading-6 text-ink/55">
          Upload JPEG, PNG, GIF, or WebP. Maximum size: 10MB.
        </p>
      </div>

      {isUploading && (
        <p className="rounded-2xl border border-brass/20 bg-brass/10 px-4 py-3 text-sm text-ink/75">
          Uploading image...
        </p>
      )}

      {error && <p className="rounded-2xl bg-wine/10 px-4 py-3 text-sm text-wine">{error}</p>}
    </div>
  );
};

export default ImageUploader;
