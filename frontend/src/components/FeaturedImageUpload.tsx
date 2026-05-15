import { ChangeEvent, useEffect, useRef, useState } from "react";
import { CloudUpload, Image as ImageIcon } from "lucide-react";
import useFileUpload from "../hooks/useFileUpload";
import { UploadResponse } from "../types/media";
import { BASE_URL } from "../api/axiosInstance";
import AltTextModal from "./AltTextModal";
import UploadProgress from "./UploadProgress";

interface FeaturedImageUploadProps {
  currentImageUrl?: string;
  currentAltText?: string;
  uploaderId?: number;
  onUploadComplete: (upload: UploadResponse, altText: string) => void;
}

const FeaturedImageUpload = ({
  currentImageUrl,
  currentAltText = "",
  uploaderId,
  onUploadComplete
}: FeaturedImageUploadProps) => {
  const inputRef = useRef<HTMLInputElement | null>(null);
  const [altText, setAltText] = useState(currentAltText);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const { acceptedTypes, error, isUploading, previewUrl, progress, resetSelection, selectFile, upload } =
    useFileUpload({
      uploaderId
    });

  useEffect(() => {
    setAltText(currentAltText);
  }, [currentAltText]);

  const activePreview = previewUrl || currentImageUrl || "";

  const handleFileChange = (event: ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0] ?? null;

    if (selectFile(file)) {
      setIsModalOpen(true);
    }
  };

  const handleUpload = async () => {
    const uploadedMedia = await upload(altText);

    if (!uploadedMedia) {
      return;
    }

    onUploadComplete(uploadedMedia, altText.trim());
    setIsModalOpen(false);
    resetSelection();

    if (inputRef.current) {
      inputRef.current.value = "";
    }
  };

  return (
    <div className="space-y-4">
      <div>
        <label className="mb-2 block text-xs uppercase tracking-[0.22em] text-ink/70">
          Featured Image
        </label>

        <label className="flex cursor-pointer flex-col items-center justify-center rounded-[1.5rem] border border-dashed border-ink/25 bg-white/80 p-6 text-center transition hover:border-brass hover:bg-parchment/60">
          <CloudUpload className="h-9 w-9 text-brass" />
          <span className="mt-4 text-sm font-semibold uppercase tracking-[0.18em] text-ink">
            Upload featured image
          </span>
          <span className="mt-2 text-xs leading-6 text-ink/55">
            JPEG, PNG, GIF, or WebP only. Maximum size 10MB.
          </span>
          <input
            ref={inputRef}
            type="file"
            accept={acceptedTypes}
            onChange={handleFileChange}
            className="sr-only"
          />
        </label>
      </div>

      {activePreview && (
        <div className="overflow-hidden rounded-[1.5rem] border border-ink/10 bg-paper/70">
          <div className="flex items-center gap-2 border-b border-ink/10 px-4 py-3 text-xs uppercase tracking-[0.18em] text-ink/60">
            <ImageIcon className="h-4 w-4" />
            Live Preview
          </div>
          <img
            src={activePreview.startsWith("blob:") || activePreview.startsWith("http") ? activePreview : `${BASE_URL}${activePreview}`}
            alt={altText || "Featured image preview"}
            className="h-52 w-full object-cover"
          />
          <div className="px-4 py-3 text-sm text-ink/65">
            {altText ? `Alt text: ${altText}` : "No alt text added yet."}
          </div>
        </div>
      )}

      {isUploading && <UploadProgress progress={progress} />}

      {error && <p className="rounded-2xl bg-wine/10 px-4 py-3 text-sm text-wine">{error}</p>}

      <AltTextModal
        altText={altText}
        isOpen={isModalOpen}
        onAltTextChange={setAltText}
        onClose={() => {
          setIsModalOpen(false);
          resetSelection();
          if (inputRef.current) {
            inputRef.current.value = "";
          }
        }}
        onConfirm={() => {
          void handleUpload();
        }}
      />
    </div>
  );
};

export default FeaturedImageUpload;
