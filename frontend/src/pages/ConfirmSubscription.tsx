import { isAxiosError } from "axios";
import { CheckCircle2, AlertCircle, Loader2, ArrowLeft, MailCheck } from "lucide-react";
import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import messagingService from "../services/messagingService";
import { SubscriptionResponse } from "../types/messaging";

const ConfirmSubscription = () => {
  const [searchParams] = useSearchParams();
  const [result, setResult] = useState<SubscriptionResponse | null>(null);
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const token = searchParams.get("token");

    const confirm = async () => {
      if (!token) {
        setError("Your confirmation link appears to be incomplete or expired.");
        setIsLoading(false);
        return;
      }

      try {
        setIsLoading(true);
        const response = await messagingService.confirmSubscription(token);
        setResult(response);
      } catch (confirmError) {
        if (isAxiosError(confirmError)) {
          const responseData = confirmError.response?.data as { message?: string } | string | undefined;
          setError(
            typeof responseData === "string"
              ? responseData
              : responseData?.message ?? "We encountered an issue confirming your subscription."
          );
        } else {
          setError("We encountered an unexpected issue. Please try again later.");
        }
      } finally {
        setIsLoading(false);
      }
    };

    void confirm();
  }, [searchParams]);

  return (
    <div className="min-h-screen bg-canvas flex items-center justify-center p-4">
      <div className="w-full max-w-xl rounded-[2.5rem] border border-slate-100 bg-white p-12 shadow-2xl text-center">
        {isLoading ? (
          <div className="space-y-6">
            <div className="mx-auto flex h-20 w-20 items-center justify-center rounded-3xl bg-slate-50 border border-slate-100 shadow-sm animate-pulse">
              <Loader2 className="h-10 w-10 text-brand animate-spin" />
            </div>
            <div>
              <p className="text-[10px] font-black uppercase tracking-[0.4em] text-brand">Newsletter Verification</p>
              <h1 className="mt-4 text-3xl font-black text-heading tracking-tight">Authenticating...</h1>
              <p className="mt-3 text-sm font-medium text-slate-400 leading-relaxed">Securing your spot in the InkWell community.</p>
            </div>
          </div>
        ) : result ? (
          <div className="space-y-8 animate-in fade-in zoom-in-95 duration-500">
            <div className="mx-auto flex h-24 w-24 items-center justify-center rounded-[2rem] bg-emerald-50 text-emerald-500 border border-emerald-100 shadow-lg shadow-emerald-500/10">
              <MailCheck className="h-12 w-12" />
            </div>
            <div>
              <p className="text-[10px] font-black uppercase tracking-[0.4em] text-emerald-500">Welcome Aboard</p>
              <h1 className="mt-4 text-4xl font-black text-heading tracking-tight">You're Subscribed!</h1>
              <p className="mt-4 text-base font-medium text-slate-500 leading-relaxed">
                {result.message || "Your subscription is now active. You'll be the first to know when our authors publish new stories."}
              </p>
            </div>
            <div className="pt-4">
              <Link
                to="/"
                className="inline-flex items-center gap-2 rounded-2xl bg-slate-900 px-10 py-4 text-[10px] font-black uppercase tracking-widest text-white shadow-lg shadow-slate-100 hover:bg-brand hover:scale-[1.02] active:scale-95 transition"
              >
                <ArrowLeft className="h-4 w-4" /> Start Reading
              </Link>
            </div>
          </div>
        ) : (
          <div className="space-y-8 animate-in fade-in duration-300">
            <div className="mx-auto flex h-20 w-20 items-center justify-center rounded-3xl bg-rose-50 text-rose-500 border border-rose-100 shadow-lg shadow-rose-500/10">
              <AlertCircle className="h-10 w-10" />
            </div>
            <div>
              <p className="text-[10px] font-black uppercase tracking-[0.4em] text-rose-500">Verification Error</p>
              <h1 className="mt-4 text-3xl font-black text-heading tracking-tight">Link Invalid</h1>
              <p className="mt-4 text-sm font-medium text-slate-400 leading-relaxed bg-rose-50/50 p-4 rounded-2xl italic">
                {error}
              </p>
            </div>
            <div className="pt-4">
              <Link
                to="/"
                className="inline-flex items-center gap-2 rounded-2xl border border-slate-100 bg-white px-10 py-4 text-[10px] font-black uppercase tracking-widest text-slate-600 hover:border-brand hover:text-brand transition"
              >
                Return Home
              </Link>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default ConfirmSubscription;
