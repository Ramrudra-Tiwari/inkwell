import { UploadResponse } from "../types/media";
import { BASE_URL } from "../api/axiosInstance";

interface MediaPreviewProps {
  upload: UploadResponse | null;
  altText: string;
  onAltTextChange: (value: string) => void;
}

const MediaPreview = ({ upload, altText, onAltTextChange }: MediaPreviewProps) => {
  if (!upload) {
    return null;
  }

  return (
    <div className="space-y-4 rounded-[1.5rem] border border-ink/10 bg-paper/70 p-4">
      <div className="overflow-hidden rounded-[1.25rem] border border-ink/10 bg-parchment/60">
        <img src={`${BASE_URL}${upload.url}`} alt={altText || "Featured image preview"} className="h-48 w-full object-cover" />
      </div>

      <div>
        <label className="mb-2 block text-xs uppercase tracking-[0.22em] text-ink/70">Alt Text</label>
        <input
          value={altText}
          onChange={(event) => onAltTextChange(event.target.value)}
          placeholder="Describe the image for accessibility"
          className="w-full rounded-2xl border border-ink/20 bg-white px-4 py-3 outline-none transition focus:border-brass"
        />
      </div>

      <div className="text-xs leading-6 text-ink/55">
        Media ID: {upload.mediaId} | {upload.originalName}
      </div>
    </div>
  );
};

export default MediaPreview;
