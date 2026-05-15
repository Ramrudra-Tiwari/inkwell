import { useEffect } from "react";
import { X, ImageIcon } from "lucide-react";

interface AltTextModalProps {
  altText: string;
  isOpen: boolean;
  onAltTextChange: (value: string) => void;
  onClose: () => void;
  onConfirm: () => void;
}

const AltTextModal = ({
  altText,
  isOpen,
  onAltTextChange,
  onClose,
  onConfirm,
}: AltTextModalProps) => {
  // Lock body scroll
  useEffect(() => {
    if (isOpen) document.body.style.overflow = "hidden";
    else document.body.style.overflow = "";
    return () => { document.body.style.overflow = ""; };
  }, [isOpen]);

  if (!isOpen) return null;

  return (
    <div
      className="fixed inset-0 z-[9999] flex items-center justify-center bg-black/50 px-4 backdrop-blur-sm"
      onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}
    >
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby="alt-text-modal-title"
        className="w-full max-w-md rounded-2xl border border-border bg-white p-6 shadow-modal"
      >
        <div className="flex items-start justify-between gap-3 mb-4">
          <div className="flex items-center gap-3">
            <div className="flex h-9 w-9 items-center justify-center rounded-full bg-accent/10">
              <ImageIcon className="h-4 w-4 text-accent" />
            </div>
            <div>
              <p className="text-xs font-bold uppercase tracking-widest text-accent">Accessibility</p>
              <h3 id="alt-text-modal-title" className="text-base font-bold text-heading">
                Add alt text
              </h3>
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="rounded-full p-1.5 text-muted transition hover:bg-canvas hover:text-heading"
            aria-label="Close"
          >
            <X className="h-4 w-4" />
          </button>
        </div>

        <p className="text-sm text-muted mb-4">
          Describe the image clearly so screen readers can explain it to readers.
        </p>

        <textarea
          value={altText}
          onChange={(e) => onAltTextChange(e.target.value)}
          rows={4}
          placeholder="Example: A fountain pen resting on a stack of handwritten notes"
          className="w-full rounded-xl border border-border bg-canvas px-4 py-3 text-sm text-body outline-none transition focus:border-brand focus:ring-2 focus:ring-brand/10 resize-none"
        />

        <div className="mt-4 flex flex-wrap justify-end gap-3">
          <button
            type="button"
            onClick={onClose}
            className="rounded-xl border border-border px-4 py-2 text-sm font-semibold text-body transition hover:bg-canvas hover:text-heading"
          >
            Cancel
          </button>
          <button
            type="button"
            onClick={onConfirm}
            className="rounded-xl bg-brand px-4 py-2 text-sm font-semibold text-white transition hover:bg-brand-hover"
          >
            Save Alt Text
          </button>
        </div>
      </div>
    </div>
  );
};

export default AltTextModal;
