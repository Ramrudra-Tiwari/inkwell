import { isAxiosError } from "axios";
import { XCircle, MailWarning, Loader2, ArrowLeft, HeartOff } from "lucide-react";
import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import messagingService from "../services/messagingService";

const Unsubscribe = () => {
  const [searchParams] = useSearchParams();
  const [isProcessing, setIsProcessing] = useState(true);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    const token = searchParams.get("token");
    const email = searchParams.get("email");

    const handleUnsubscribe = async () => {
      if (!token || !email) {
        setError("The unsubscribe link appears to be invalid or expired.");
        setIsProcessing(false);
        return;
      }

      try {
        setIsProcessing(true);
        await messagingService.unsubscribeFromNewsletter(email, token);
        setSuccess(true);
      } catch (err) {
        if (isAxiosError(err)) {
          const data = err.response?.data as { message?: string } | string;
          setError(typeof data === "string" ? data : data?.message ?? "We encountered a problem processing your request.");
        } else {
          setError("We couldn't process your request. Please try again later.");
        }
      } finally {
        setIsProcessing(false);
      }
    };

    void handleUnsubscribe();
  }, [searchParams]);

  return (
    <div className="min-h-screen bg-canvas flex items-center justify-center p-4">
      <div className="w-full max-w-xl rounded-[2.5rem] border border-slate-100 bg-white p-12 shadow-2xl text-center">
        {isProcessing ? (
          <div className="space-y-6">
            <div className="mx-auto flex h-20 w-20 items-center justify-center rounded-3xl bg-slate-50 border border-slate-100 shadow-sm">
              <Loader2 className="h-10 w-10 text-brand animate-spin" />
            </div>
            <div>
              <p className="text-[10px] font-black uppercase tracking-[0.4em] text-brand">Newsletter Updates</p>
              <h1 className="mt-4 text-3xl font-black text-heading tracking-tight">Processing Request...</h1>
              <p className="mt-3 text-sm font-medium text-slate-400 leading-relaxed">We're updating your preferences in our system.</p>
            </div>
          </div>
        ) : success ? (
          <div className="space-y-8 animate-in fade-in zoom-in-95 duration-500">
            <div className="mx-auto flex h-24 w-24 items-center justify-center rounded-[2rem] bg-slate-50 text-slate-400 border border-slate-100 shadow-lg shadow-slate-100">
              <HeartOff className="h-12 w-12" />
            </div>
            <div>
              <p className="text-[10px] font-black uppercase tracking-[0.4em] text-slate-400">Subscription Ended</p>
              <h1 className="mt-4 text-4xl font-black text-heading tracking-tight">Successfully Removed</h1>
              <p className="mt-4 text-base font-medium text-slate-500 leading-relaxed">
                We're sorry to see you go. You have been successfully unsubscribed and will no longer receive newsletter updates.
              </p>
            </div>
            <div className="pt-4">
              <Link
                to="/"
                className="inline-flex items-center gap-2 rounded-2xl bg-slate-900 px-10 py-4 text-[10px] font-black uppercase tracking-widest text-white shadow-lg shadow-slate-100 hover:bg-brand hover:scale-[1.02] active:scale-95 transition"
              >
                <ArrowLeft className="h-4 w-4" /> Back to Stories
              </Link>
            </div>
          </div>
        ) : (
          <div className="space-y-8 animate-in fade-in duration-300">
            <div className="mx-auto flex h-20 w-20 items-center justify-center rounded-3xl bg-rose-50 text-rose-500 border border-rose-100 shadow-lg shadow-rose-500/10">
              <XCircle className="h-10 w-10" />
            </div>
            <div>
              <p className="text-[10px] font-black uppercase tracking-[0.4em] text-rose-500">Unsubscribe Error</p>
              <h1 className="mt-4 text-3xl font-black text-heading tracking-tight">Request Failed</h1>
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

export default Unsubscribe;
