import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import apiClient from "../api/axiosInstance";
import { useAuth } from "../context/AuthContext";

declare global {
  interface Window {
    Razorpay?: any;
  }
}

interface CheckoutButtonProps {
  compact?: boolean;
  authorId?: number;
  authorName?: string;
  postId?: number;
  postTitle?: string;
}

const CheckoutButton = ({
  compact = false,
  authorId,
  authorName,
  postId,
  postTitle
}: CheckoutButtonProps) => {
  const { currentUser, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);
  const [amount, setAmount] = useState("100");
  const [isAmountDialogOpen, setIsAmountDialogOpen] = useState(false);
  const isPositiveMessage = message === "Payment success";

  useEffect(() => {
    if (window.Razorpay) return;

    const script = document.createElement("script");
    script.src = "https://checkout.razorpay.com/v1/checkout.js";
    script.async = true;
    script.onerror = () => setMessage("Unable to load Razorpay checkout.");
    document.body.appendChild(script);
  }, []);

  const startSupportFlow = () => {
    if (!isAuthenticated) {
      navigate("/login", { state: { from: location } });
      return;
    }

    setMessage("");
    setIsAmountDialogOpen(true);
  };

  const handlePayment = async () => {
    const amountInRupees = Number(amount);

    if (!Number.isFinite(amountInRupees) || amountInRupees < 1) {
      setMessage("Enter an amount of at least INR 1.");
      return;
    }

    const amountInPaise = Math.round(amountInRupees * 100);

    try {
      setLoading(true);
      setMessage("");
      setIsAmountDialogOpen(false);

      const { data } = await apiClient.post("/payments/create-order", {
        amount: amountInPaise
      });

      if (!window.Razorpay) {
        setMessage("Razorpay checkout is still loading. Try again.");
        setLoading(false);
        return;
      }

      const options = {
        key: data.keyId,
        order_id: data.orderId || data.id,
        name: "InkWell",
        description: "Support author",
        prefill: {
          name: currentUser?.fullName || "InkWell Supporter",
          email: currentUser?.email || "supporter@example.com"
        },
        notes: {
          platform: "InkWell",
          purpose: "Support author"
        },
        theme: {
          color: "#10b981"
        },
        handler: async (response: any) => {
          try {
            await apiClient.post("/payments/verify", {
              razorpay_order_id: response.razorpay_order_id,
              razorpay_payment_id: response.razorpay_payment_id,
              razorpay_signature: response.razorpay_signature,
              supporterUserId: currentUser?.userId,
              supporterName: currentUser?.fullName,
              supporterEmail: currentUser?.email,
              authorId,
              authorName,
              postId,
              postTitle,
              amount: data.amount,
              currency: data.currency
            });
            setMessage("Payment success");
          } catch {
            setMessage("Payment verification failed.");
          } finally {
            setLoading(false);
          }
        },
        modal: {
          ondismiss: () => setLoading(false)
        }
      };

      const razorpay = new window.Razorpay(options);
      razorpay.on("payment.failed", (response: any) => {
        setMessage(response?.error?.description || "Payment failed.");
        setLoading(false);
      });
      razorpay.open();
    } catch (error: any) {
      setMessage(error?.response?.data?.message || error?.message || "Payment failed.");
      setLoading(false);
    }
  };

  if (compact) {
    return (
      <div className="flex flex-col items-start gap-2">
        <button
          type="button"
          onClick={startSupportFlow}
          disabled={loading}
          className="rounded-xl bg-emerald-500 px-5 py-2.5 text-xs font-black uppercase tracking-widest text-white transition hover:bg-emerald-600 disabled:cursor-not-allowed disabled:opacity-60"
        >
          {loading ? "Processing..." : isAuthenticated ? "Support Author" : "Sign In to Support"}
        </button>
        {message && (
          <p className={isPositiveMessage ? "text-xs font-bold text-emerald-400" : "text-xs font-bold text-red-400"}>
            {message}
          </p>
        )}
        {isAmountDialogOpen && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/70 px-4 backdrop-blur-sm">
            <div className="w-full max-w-sm rounded-2xl border border-slate-700 bg-slate-950 p-6 text-slate-100 shadow-2xl shadow-slate-950/40">
              <div className="mb-5">
                <p className="text-xs font-black uppercase tracking-widest text-emerald-300">Support Author</p>
                <h3 className="mt-2 text-xl font-black text-white">Enter amount</h3>
              </div>
              <label className="mb-2 block text-xs font-black uppercase tracking-widest text-slate-400">
                Amount in INR
              </label>
              <input
                type="number"
                min="1"
                value={amount}
                autoFocus
                onChange={(event) => setAmount(event.target.value)}
                className="w-full rounded-xl border border-slate-700 bg-slate-900 px-4 py-3 text-lg font-black text-white outline-none focus:border-emerald-400"
              />
              <div className="mt-6 flex justify-end gap-3">
                <button
                  type="button"
                  onClick={() => setIsAmountDialogOpen(false)}
                  className="rounded-xl border border-slate-700 px-4 py-2 text-xs font-black uppercase tracking-widest text-slate-300 transition hover:bg-white/10"
                >
                  Cancel
                </button>
                <button
                  type="button"
                  onClick={handlePayment}
                  disabled={loading}
                  className="rounded-xl bg-emerald-500 px-5 py-2 text-xs font-black uppercase tracking-widest text-white transition hover:bg-emerald-600 disabled:cursor-not-allowed disabled:opacity-60"
                >
                  Pay
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    );
  }

  return (
    <div className="mx-auto flex min-h-[60vh] max-w-md flex-col items-center justify-center gap-5 px-4 text-center">
      <h1 className="text-2xl font-black text-heading">InkWell Razorpay Test</h1>
      <label className="w-full text-left text-xs font-black uppercase tracking-widest text-slate-400">
        Amount in INR
      </label>
      <input
        type="number"
        min="1"
        value={amount}
        onChange={(event) => setAmount(event.target.value)}
        className="w-full rounded-xl border border-slate-200 px-4 py-3 text-sm font-bold outline-none focus:border-emerald-500"
      />
      <button
        type="button"
        onClick={startSupportFlow}
        disabled={loading}
        className="rounded-xl bg-emerald-500 px-6 py-3 text-sm font-black uppercase tracking-widest text-white transition hover:bg-emerald-600 disabled:cursor-not-allowed disabled:opacity-60"
      >
        {loading ? "Processing..." : isAuthenticated ? "Pay Now" : "Sign In to Support"}
      </button>
      {message && (
        <p className={isPositiveMessage ? "font-bold text-emerald-400" : "font-bold text-red-400"}>
          {message}
        </p>
      )}
    </div>
  );
};

export default CheckoutButton;
