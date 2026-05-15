import { FormEvent, useState } from "react";
import { Send, X } from "lucide-react";

interface CommentFormProps {
  isSubmitting?: boolean;
  initialContent?: string;
  placeholder?: string;
  submitLabel?: string;
  onSubmit: (content: string) => Promise<boolean | void>;
  onCancel?: () => void;
}

const CommentForm = ({
  isSubmitting = false,
  initialContent = "",
  placeholder = "Add your thoughts to the story...",
  submitLabel = "Post Comment",
  onSubmit,
  onCancel
}: CommentFormProps) => {
  const [content, setContent] = useState(initialContent);
  const [localError, setLocalError] = useState("");

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (!content.trim()) {
      setLocalError("Your thought needs a bit more substance.");
      return;
    }

    setLocalError("");
    const didSucceed = await onSubmit(content.trim());

    if (didSucceed !== false && !initialContent) {
      setContent("");
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div className="relative group">
        <textarea
          value={content}
          onChange={(event) => setContent(event.target.value)}
          rows={4}
          placeholder={placeholder}
          className="w-full rounded-[1.5rem] border border-slate-200 bg-white px-6 py-5 text-base leading-relaxed text-slate-600 outline-none transition-all duration-300 focus:border-brand focus:ring-4 focus:ring-brand/5 placeholder:text-slate-300"
        />
        <div className="absolute bottom-4 right-6 text-[10px] font-bold text-slate-300 uppercase tracking-widest pointer-events-none group-focus-within:text-brand/40">
          Markdown supported
        </div>
      </div>

      {localError && <p className="text-[10px] font-black uppercase tracking-widest text-red-500">{localError}</p>}

      <div className="flex flex-wrap items-center gap-3">
        <button
          type="submit"
          disabled={isSubmitting}
          className="group flex items-center gap-2 rounded-2xl bg-brand px-6 py-3 text-xs font-black uppercase tracking-[0.2em] text-white shadow-lg shadow-brand/20 transition-all hover:bg-brand-hover hover:scale-105 active:scale-95 disabled:cursor-not-allowed disabled:opacity-70"
        >
          {isSubmitting ? (
            <span className="flex items-center gap-2">
               <div className="h-3 w-3 animate-spin rounded-full border-2 border-white/30 border-t-white" />
               Processing...
            </span>
          ) : (
            <>
              {submitLabel}
              <Send className="h-3.5 w-3.5 transition-transform group-hover:translate-x-1 group-hover:-translate-y-1" />
            </>
          )}
        </button>

        {onCancel && (
          <button
            type="button"
            onClick={onCancel}
            className="flex items-center gap-2 rounded-2xl border border-slate-200 bg-white px-6 py-3 text-xs font-black uppercase tracking-[0.2em] text-slate-500 transition-all hover:bg-slate-50 hover:text-heading"
          >
            <X className="h-3.5 w-3.5" />
            Dismiss
          </button>
        )}
      </div>
    </form>
  );
};

export default CommentForm;

