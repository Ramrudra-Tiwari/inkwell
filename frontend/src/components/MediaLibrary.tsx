import { Trash2 } from "lucide-react";
import { useEffect, useState } from "react";
import mediaService from "../services/mediaService";
import { Media } from "../types/media";
import { BASE_URL } from "../api/axiosInstance";

interface MediaLibraryProps {
  selectedMediaId?: number | null;
  uploaderId?: number;
  onSelect: (media: Media) => void;
}

const formatDate = (dateString: string) =>
  new Intl.DateTimeFormat("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric"
  }).format(new Date(dateString));

const MediaLibrary = ({ selectedMediaId, uploaderId, onSelect }: MediaLibraryProps) => {
  const [items, setItems] = useState<Media[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");

  const loadMedia = async () => {
    if (!uploaderId) {
      setItems([]);
      return;
    }

    try {
      setIsLoading(true);
      setError("");
      const media = await mediaService.getByUploader(uploaderId);
      setItems(media);
    } catch (loadError) {
      console.error(loadError);
      setError("Unable to load your media library.");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    void loadMedia();
  }, [uploaderId]);

  const handleDelete = async (mediaId: number) => {
    try {
      await mediaService.delete(mediaId);
      setItems((currentItems) => currentItems.filter((item) => item.mediaId !== mediaId));
    } catch (deleteError) {
      console.error(deleteError);
      setError("Unable to delete that media item right now.");
    }
  };

  return (
    <section className="space-y-4 rounded-[1.75rem] border border-ink/10 bg-paper p-5 shadow-letterpress">
      <div className="flex items-center justify-between gap-3">
        <div>
          <p className="text-xs uppercase tracking-[0.22em] text-brass">Media Library</p>
          <h3 className="mt-2 font-display text-3xl text-ink">Previously uploaded</h3>
        </div>
        <button
          type="button"
          onClick={() => {
            void loadMedia();
          }}
          className="rounded-full border border-ink px-4 py-2 text-xs font-semibold uppercase tracking-[0.18em] text-ink transition hover:bg-ink hover:text-parchment"
        >
          Refresh
        </button>
      </div>

      {isLoading && (
        <div className="grid gap-4 sm:grid-cols-2">
          {Array.from({ length: 4 }).map((_, index) => (
            <div key={index} className="h-40 animate-pulse rounded-[1.25rem] bg-ink/10" />
          ))}
        </div>
      )}

      {!isLoading && error && (
        <div className="rounded-2xl border border-wine/20 bg-wine/10 px-4 py-3 text-sm text-wine">
          {error}
        </div>
      )}

      {!isLoading && !error && items.length === 0 && (
        <div className="rounded-2xl border border-ink/10 bg-parchment/70 px-4 py-5 text-sm text-ink/65">
          Your uploaded images will appear here after the first successful upload.
        </div>
      )}

      {!isLoading && !error && items.length > 0 && (
        <div className="grid gap-4 sm:grid-cols-2">
          {items.map((item) => {
            const isSelected = selectedMediaId === item.mediaId;

            return (
              <article
                key={item.mediaId}
                className={`overflow-hidden rounded-[1.25rem] border transition ${
                  isSelected
                    ? "border-brass bg-sepia/45 shadow-letterpress"
                    : "border-ink/10 bg-parchment/70 hover:border-ink/25"
                }`}
              >
                <button
                  type="button"
                  onClick={() => onSelect(item)}
                  className="block w-full text-left"
                >
                  <img src={`${BASE_URL}${item.url}`} alt={item.altText || item.originalName} className="h-36 w-full object-cover" />
                  <div className="space-y-2 px-4 py-4">
                    <p className="truncate text-sm font-semibold text-ink">{item.originalName}</p>
                    <p className="text-xs uppercase tracking-[0.16em] text-ink/55">
                      Uploaded {formatDate(item.uploadedAt)}
                    </p>
                  </div>
                </button>

                <div className="flex items-center justify-between border-t border-ink/10 px-4 py-3">
                  <span className="text-xs uppercase tracking-[0.16em] text-ink/60">
                    {isSelected ? "Selected" : "Choose image"}
                  </span>
                  <button
                    type="button"
                    onClick={() => {
                      void handleDelete(item.mediaId);
                    }}
                    className="rounded-full p-2 text-wine transition hover:bg-wine/10"
                    aria-label={`Delete ${item.originalName}`}
                  >
                    <Trash2 className="h-4 w-4" />
                  </button>
                </div>
              </article>
            );
          })}
        </div>
      )}
    </section>
  );
};

export default MediaLibrary;
