import { isAxiosError } from "axios";
import { Mail, ArrowRight, Sparkles } from "lucide-react";
import { FormEvent, useEffect, useState } from "react";
import { useAuth } from "../context/AuthContext";
import messagingService from "../services/messagingService";
import taxonomyService from "../services/taxonomyService";
import { Category } from "../types/taxonomy";

const NewsletterWidget = () => {
  const { currentUser } = useAuth();
  const [email, setEmail] = useState(currentUser?.email ?? "");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [categories, setCategories] = useState<Category[]>([]);
  const [preferences, setPreferences] = useState<string[]>(["general-insights"]);
  const [message, setMessage] = useState("");
  const [messageTone, setMessageTone] = useState<"neutral" | "success" | "warning" | "error">(
    "neutral"
  );

  useEffect(() => {
    if (currentUser?.email) setEmail(currentUser.email);
  }, [currentUser?.email]);

  useEffect(() => {
    const loadCategories = async () => {
      try {
        const cats = await taxonomyService.getCategories();
        setCategories(cats.slice(0, 5)); // Show top 5 for the widget
      } catch (e) {
        console.error("Failed to load newsletter categories", e);
      }
    };
    void loadCategories();
  }, []);

  const togglePreference = (val: string) => {
    setPreferences((prev) =>
      prev.includes(val) ? prev.filter((p) => p !== val) : [...prev, val]
    );
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (!email.trim()) {
      setMessageTone("error");
      setMessage("Email is required.");
      return;
    }

    try {
      setIsSubmitting(true);
      setMessage("");

      const response = await messagingService.subscribeToNewsletter({
        email: email.trim(),
        userId: currentUser?.userId,
        preferences: preferences.join(","),
      });

      if (response.status === "PENDING") {
        setMessageTone("warning");
        setMessage("Check your email to confirm your subscription.");
        return;
      }
      if (response.status === "ACTIVE") {
        setMessageTone("success");
        setMessage("You are already subscribed!");
        return;
      }

      setMessageTone("neutral");
      setMessage(response.message);
    } catch (submitError) {
      if (isAxiosError(submitError)) {
        const data = submitError.response?.data as { message?: string } | string | undefined;
        const serverMsg =
          typeof data === "string" ? data : data?.message ?? "Unable to subscribe right now.";

        if (submitError.response?.status === 400 && /already|pending|subscribed/i.test(serverMsg)) {
          setMessageTone(/pending/i.test(serverMsg) ? "warning" : "success");
          setMessage(serverMsg);
          return;
        }

        setMessageTone("error");
        setMessage(serverMsg);
      } else {
        setMessageTone("error");
        setMessage("Unable to subscribe right now.");
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  const toneClass =
    messageTone === "success"
      ? "border-emerald-200 bg-emerald-50 text-emerald-700"
      : messageTone === "warning"
        ? "border-amber-200 bg-amber-50 text-amber-700"
        : messageTone === "error"
          ? "border-red-200 bg-red-50 text-red-600"
          : "border-border bg-canvas text-muted";

  return (
    <section className="glass-card p-6">
      <div className="flex items-center gap-2 mb-2">
        <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-brand/10">
          <Mail className="h-5 w-5 text-brand" />
        </div>
        <div>
          <h2 className="text-base font-black text-heading leading-none">The InkWell Letter</h2>
          <p className="mt-1 text-[10px] font-bold uppercase tracking-widest text-muted">Weekly Insights</p>
        </div>
      </div>
      <p className="mt-4 mb-6 text-xs leading-relaxed text-slate-500">
        Get fresh stories delivered to your inbox. Choose your areas of interest below.
      </p>

      <form onSubmit={handleSubmit} className="space-y-6">
        <input
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          placeholder="you@example.com"
          className="w-full rounded-xl border border-slate-100 bg-slate-50/50 px-4 py-3 text-sm font-medium text-heading outline-none transition focus:border-brand focus:bg-white focus:ring-4 focus:ring-brand/5"
        />

        <div className="space-y-3">
          <p className="text-[10px] font-black uppercase tracking-[0.2em] text-slate-400">Your Interests</p>
          
          <label className="flex cursor-pointer items-center gap-3 group">
            <input
              type="checkbox"
              checked={preferences.includes("general-insights")}
              onChange={() => togglePreference("general-insights")}
              className="hidden"
            />
            <div className={`flex h-5 w-5 items-center justify-center rounded-md border-2 transition-all ${
              preferences.includes("general-insights") 
                ? "border-brand bg-brand text-white" 
                : "border-slate-100 bg-slate-50 group-hover:border-brand/40"
            }`}>
              {preferences.includes("general-insights") && <Sparkles className="h-3 w-3" />}
            </div>
            <span className={`text-xs font-bold transition ${
              preferences.includes("general-insights") ? "text-heading" : "text-slate-400 group-hover:text-slate-600"
            }`}>
              General Insights
            </span>
          </label>

          {categories.map((cat) => (
            <label key={cat.categoryId} className="flex cursor-pointer items-center gap-3 group">
              <input
                type="checkbox"
                checked={preferences.includes(cat.slug)}
                onChange={() => togglePreference(cat.slug)}
                className="hidden"
              />
              <div className={`flex h-5 w-5 items-center justify-center rounded-md border-2 transition-all ${
                preferences.includes(cat.slug) 
                  ? "border-brand bg-brand text-white" 
                  : "border-slate-100 bg-slate-50 group-hover:border-brand/40"
              }`}>
                {preferences.includes(cat.slug) && (
                  <svg className="h-3 w-3 fill-current" viewBox="0 0 20 20">
                    <path d="M0 11l2-2 5 5L18 3l2 2L7 18z" />
                  </svg>
                )}
              </div>
              <span className={`text-xs font-bold transition ${
                preferences.includes(cat.slug) ? "text-heading" : "text-slate-400 group-hover:text-slate-600"
              }`}>
                {cat.name}
              </span>
            </label>
          ))}
        </div>

        <button
          type="submit"
          disabled={isSubmitting}
          className="group relative inline-flex w-full items-center justify-center gap-3 rounded-2xl bg-slate-900 py-4 text-xs font-black uppercase tracking-widest text-white transition-all hover:bg-brand hover:scale-[1.02] active:scale-95 disabled:cursor-not-allowed disabled:opacity-70"
        >
          {isSubmitting ? (
            <div className="h-4 w-4 animate-spin rounded-full border-2 border-white/30 border-t-white" />
          ) : (
            <>
              Subscribe Now
              <ArrowRight className="h-3.5 w-3.5 transition-transform group-hover:translate-x-1" />
            </>
          )}
        </button>
      </form>

      {message && (
        <p className={`mt-3 rounded-lg border px-3 py-2 text-xs ${toneClass}`}>{message}</p>
      )}
    </section>
  );
};

export default NewsletterWidget;
