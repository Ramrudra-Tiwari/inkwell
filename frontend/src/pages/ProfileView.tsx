import { useEffect, useMemo, useRef, useState } from "react";
import { Link } from "react-router-dom";
import { Shield, Lock, KeyRound, User as UserIcon, PenLine, Receipt, IndianRupee, Wallet } from "lucide-react";
import { useAuth } from "../context/AuthContext";
import postService from "../services/postService";
import taxonomyService from "../services/taxonomyService";
import paymentService from "../services/paymentService";
import { Post } from "../types/post";
import { PaymentHistoryItem } from "../types/payment";
import { Category } from "../types/taxonomy";
import { useToast } from "../components/GlobalToastProvider";
import UserAvatar from "../components/UserAvatar";

const ProfileView = () => {
  const { currentUser, updateProfile, updatePassword } = useAuth();
  const { showToast } = useToast();
  
  const [posts, setPosts] = useState<Post[]>([]);
  const [payments, setPayments] = useState<PaymentHistoryItem[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");
  
  // Profile Edit State
  const [isEditing, setIsEditing] = useState(false);
  const [isUpdatingProfile, setIsUpdatingProfile] = useState(false);
  const [isUpdatingPassword, setIsUpdatingPassword] = useState(false);
  const [showPaymentHistory, setShowPaymentHistory] = useState(false);
  const [editData, setEditData] = useState({
    fullName: currentUser?.fullName ?? "",
    bio: currentUser?.bio ?? "",
    avatarUrl: currentUser?.avatarUrl ?? "",
  });
  const paymentHistoryRef = useRef<HTMLDivElement | null>(null);

  // Password Edit State
  const [passwordData, setPasswordData] = useState({
    currentPassword: "",
    newPassword: "",
  });
  const [passwordConfirm, setPasswordConfirm] = useState("");

  useEffect(() => {
    if (currentUser) {
      setEditData({
        fullName: currentUser.fullName,
        bio: currentUser.bio ?? "",
        avatarUrl: currentUser.avatarUrl ?? "",
      });
    }
  }, [currentUser]);

  useEffect(() => {
    const loadProfileData = async () => {
      if (!currentUser?.userId) {
        setIsLoading(false);
        return;
      }
      try {
        setIsLoading(true);
        setError("");
        const [authorPosts, allCategories] = await Promise.all([
          postService.getByAuthor(currentUser.userId),
          taxonomyService.getCategories(),
        ]);
        setPosts(authorPosts);
        setCategories(allCategories);
        const paymentHistory = await paymentService.getPaymentHistory(currentUser.userId);
        setPayments(paymentHistory);
      } catch (e) {
        console.error(e);
        setError("Unable to load your profile right now.");
      } finally {
        setIsLoading(false);
      }
    };
    void loadProfileData();
  }, [currentUser?.userId]);

  const handleProfileUpdate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (isUpdatingProfile) return;
    try {
      setIsUpdatingProfile(true);
      await updateProfile(editData);
      setIsEditing(false);
      showToast("Profile identity updated.", "success");
    } catch (e) {
      showToast("Failed to save changes.", "error");
    } finally {
      setIsUpdatingProfile(false);
    }
  };

  const handlePasswordUpdate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (isUpdatingPassword) return;
    if (passwordData.newPassword !== passwordConfirm) {
      showToast("New passwords do not match.", "error");
      return;
    }
    try {
      setIsUpdatingPassword(true);
      await updatePassword(passwordData.currentPassword, passwordData.newPassword);
      setPasswordData({ currentPassword: "", newPassword: "" });
      setPasswordConfirm("");
      showToast("Security credentials updated.", "success");
    } catch (e) {
      showToast("Password update failed. Check your current password.", "error");
    } finally {
      setIsUpdatingPassword(false);
    }
  };

  const topCategory = useMemo(() => {
    const counts = posts.reduce<Record<string, number>>((acc, post) => {
      if (post.categorySlug) {
        acc[post.categorySlug] = (acc[post.categorySlug] ?? 0) + 1;
      }
      return acc;
    }, {});
    const [slug] = Object.entries(counts).sort((a, b) => b[1] - a[1])[0] ?? [];
    return categories.find((c) => c.slug === slug)?.name ?? "No dominant category";
  }, [categories, posts]);

  const handleTogglePaymentHistory = () => {
    setShowPaymentHistory((current) => {
      const next = !current;
      if (!current) {
        window.setTimeout(() => {
          paymentHistoryRef.current?.scrollIntoView({ behavior: "smooth", block: "start" });
        }, 120);
      }
      return next;
    });
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-canvas py-20 text-center">
        <div className="mx-auto h-12 w-12 animate-spin rounded-full border-4 border-slate-100 border-t-brand" />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-canvas pb-20">
      {/* Header Banner */}
      <div className="bg-slate-900 pt-32 pb-24 text-center">
        <div className="mx-auto max-w-4xl px-4">
          <p className="text-[10px] font-black uppercase tracking-[0.4em] text-brand/80 mb-4">Member Account</p>
          <h1 className="text-4xl md:text-5xl font-extrabold tracking-tight text-white mb-4">
            InkWell <span className="text-transparent bg-clip-text bg-gradient-to-r from-brand to-accent">Control Center</span>
          </h1>
          <p className="text-slate-400 text-sm font-medium max-w-lg mx-auto">Manage your identity and security in your personal hub.</p>
        </div>
      </div>

      <div className="mx-auto -mt-12 max-w-6xl px-4 space-y-8">
        {/* Top Actions Card */}
        <div className="rounded-[2.5rem] border border-slate-100 bg-white p-8 shadow-sm flex flex-wrap items-center justify-between gap-6">
           <div className="flex items-center gap-4">
              <UserAvatar
                src={currentUser?.avatarUrl}
                alt={currentUser?.fullName ?? "Profile"}
                fallbackText={currentUser?.fullName}
                className="h-16 w-16 rounded-3xl border border-slate-100 bg-slate-50 shadow-sm"
                fallbackClassName="text-2xl text-heading"
                iconClassName="h-8 w-8 text-slate-300"
              />
              <div>
                <h2 className="text-2xl font-black text-heading">{currentUser?.fullName}</h2>
                <p className="text-sm font-bold text-slate-400">{currentUser?.email}</p>
              </div>
           </div>
           
           <div className="flex flex-wrap gap-3">
              <button
                type="button"
                onClick={handleTogglePaymentHistory}
                className={`inline-flex items-center gap-2 rounded-2xl border px-6 py-3 text-xs font-semibold uppercase tracking-widest transition ${
                  showPaymentHistory
                    ? "border-brand bg-brand text-white shadow-lg shadow-brand/20"
                    : "border-slate-100 bg-white text-slate-600 hover:border-brand hover:text-brand"
                }`}
              >
                <Wallet className="h-4 w-4" />
                {showPaymentHistory ? "Hide Payment History" : "Check Payment History"}
              </button>
              <Link to="/author/posts/new" className="inline-flex items-center gap-2 rounded-2xl border border-slate-100 bg-white px-6 py-3 text-xs font-semibold uppercase tracking-widest text-slate-600 transition hover:border-brand hover:text-brand">
                <PenLine className="h-4 w-4" /> New Story
              </Link>
           </div>
        </div>

        <div className="grid gap-8 lg:grid-cols-2">
          {/* Identity Section */}
          <div className="rounded-[2.5rem] border border-slate-100 bg-white p-10 shadow-sm">
            <div className="mb-8 flex items-center justify-between">
              <div>
                <h2 className="text-xl font-black text-heading">Identity</h2>
                <p className="text-[10px] font-black uppercase tracking-[0.2em] text-brand mt-1">Manage your public profile</p>
              </div>
              <button
                onClick={() => setIsEditing(!isEditing)}
                className={`rounded-xl px-5 py-2.5 text-xs font-semibold uppercase tracking-widest transition-all duration-200 active:scale-95 ${
                  isEditing ? "bg-red-500 text-white shadow-lg shadow-red-200" : "bg-brand text-white shadow-lg shadow-brand/20 hover:bg-brand-hover"
                }`}
              >
                {isEditing ? "Cancel" : "Edit Profile"}
              </button>
            </div>

            {isEditing ? (
              <form onSubmit={handleProfileUpdate} className="space-y-6">
                <div>
                  <label className="block text-xs font-semibold uppercase tracking-widest text-slate-400 mb-2">Full Name</label>
                  <input
                    type="text"
                    value={editData.fullName}
                    onChange={(e) => setEditData({ ...editData, fullName: e.target.value })}
                    className="w-full rounded-2xl border border-slate-100 bg-slate-50/30 p-4 text-sm font-medium outline-none focus:border-brand focus:ring-4 focus:ring-brand/5 transition"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold uppercase tracking-widest text-slate-400 mb-2">Avatar URL</label>
                  <input
                    type="text"
                    value={editData.avatarUrl}
                    onChange={(e) => setEditData({ ...editData, avatarUrl: e.target.value })}
                    className="w-full rounded-2xl border border-slate-100 bg-slate-50/30 p-4 text-sm font-medium outline-none focus:border-brand focus:ring-4 focus:ring-brand/5 transition"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold uppercase tracking-widest text-slate-400 mb-2">Biography</label>
                  <textarea
                    value={editData.bio}
                    onChange={(e) => setEditData({ ...editData, bio: e.target.value })}
                    className="w-full rounded-2xl border border-slate-100 bg-slate-50/30 p-4 text-sm font-medium outline-none focus:border-brand focus:ring-4 focus:ring-brand/5 transition"
                    rows={4}
                  />
                </div>
                <button
                  type="submit"
                  disabled={isUpdatingProfile}
                  className="w-full rounded-2xl bg-brand py-4 text-xs font-semibold uppercase tracking-widest text-white shadow-lg shadow-brand/20 transition hover:scale-[1.02] active:scale-95 disabled:opacity-50"
                >
                  {isUpdatingProfile ? "Saving..." : "Save Identity"}
                </button>
              </form>
            ) : (
              <div className="space-y-6">
                <div className="flex items-center gap-4 rounded-3xl bg-slate-50/50 p-6">
                  <UserAvatar
                    src={currentUser?.avatarUrl}
                    alt={currentUser?.fullName ?? "Profile"}
                    fallbackText={currentUser?.fullName}
                    className="h-16 w-16 rounded-[1.25rem] border border-slate-100 bg-white shadow-sm"
                    fallbackClassName="text-xl text-heading"
                    iconClassName="h-6 w-6 text-slate-300"
                  />
                  <div>
                    <p className="text-base font-bold text-heading">{currentUser?.fullName}</p>
                    <p className="text-[10px] font-black text-brand uppercase tracking-widest mt-0.5">Verified {currentUser?.role}</p>
                  </div>
                </div>
                {currentUser?.bio && (
                  <div className="rounded-3xl border border-slate-50 p-6">
                    <p className="text-xs font-semibold uppercase tracking-widest text-slate-400 mb-2">Bio</p>
                    <p className="text-sm font-medium text-slate-600 leading-relaxed italic">"{currentUser.bio}"</p>
                  </div>
                )}
              </div>
            )}
          </div>

          {/* Security Section */}
          <div className="rounded-[2.5rem] border border-slate-100 bg-white p-10 shadow-sm">
            <div className="mb-8">
              <h2 className="text-xl font-black text-heading">Security & Protection</h2>
              <p className="text-[10px] font-black uppercase tracking-[0.2em] text-brand mt-1">Safeguard your account credentials</p>
            </div>

            <form onSubmit={handlePasswordUpdate} className="space-y-6">
              <div>
                <label className="block text-xs font-semibold uppercase tracking-widest text-slate-400 mb-2">Current Password</label>
                <div className="relative">
                  <Lock className="absolute left-4 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-300" />
                  <input
                    type="password"
                    required
                    value={passwordData.currentPassword}
                    onChange={(e) => setPasswordData({ ...passwordData, currentPassword: e.target.value })}
                    className="w-full rounded-2xl border border-slate-100 bg-slate-50/30 pl-12 pr-4 py-4 text-sm font-medium outline-none focus:border-brand focus:ring-4 focus:ring-brand/5 transition"
                    placeholder="••••••••"
                  />
                </div>
              </div>
              <div>
                <label className="block text-xs font-semibold uppercase tracking-widest text-slate-400 mb-2">New Password</label>
                <div className="relative">
                  <Shield className="absolute left-4 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-300" />
                  <input
                    type="password"
                    required
                    value={passwordData.newPassword}
                    onChange={(e) => setPasswordData({ ...passwordData, newPassword: e.target.value })}
                    className="w-full rounded-2xl border border-slate-100 bg-slate-50/30 pl-12 pr-4 py-4 text-sm font-medium outline-none focus:border-brand focus:ring-4 focus:ring-brand/5 transition"
                    placeholder="••••••••"
                  />
                </div>
              </div>
              <div>
                <label className="block text-xs font-semibold uppercase tracking-widest text-slate-400 mb-2">Confirm New Password</label>
                <div className="relative">
                  <KeyRound className="absolute left-4 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-300" />
                  <input
                    type="password"
                    required
                    value={passwordConfirm}
                    onChange={(e) => setPasswordConfirm(e.target.value)}
                    className="w-full rounded-2xl border border-slate-100 bg-slate-50/30 pl-12 pr-4 py-4 text-sm font-medium outline-none focus:border-brand focus:ring-4 focus:ring-brand/5 transition"
                    placeholder="••••••••"
                  />
                </div>
              </div>
              <button
                type="submit"
                disabled={isUpdatingPassword}
                className="w-full rounded-2xl bg-slate-900 py-4 text-[10px] font-black uppercase tracking-widest text-white shadow-lg shadow-slate-100 transition hover:bg-brand hover:scale-[1.02] active:scale-95 disabled:opacity-50"
              >
                {isUpdatingPassword ? "Securing..." : "Update Security"}
              </button>
            </form>
          </div>
        </div>

        {/* Quick Stats Grid */}
        <div className="grid gap-6 md:grid-cols-3">
          <div className="rounded-[1.5rem] border border-slate-100 bg-white p-8 shadow-sm">
            <p className="text-[10px] font-black uppercase tracking-widest text-slate-400 mb-2">Stories Written</p>
            <p className="text-3xl font-black text-heading">{posts.length}</p>
          </div>
          <div className="rounded-[1.5rem] border border-slate-100 bg-white p-8 shadow-sm">
            <p className="text-[10px] font-black uppercase tracking-widest text-slate-400 mb-2">Dominant Category</p>
            <p className="text-xl font-black text-heading truncate">{topCategory}</p>
          </div>
          <div className="rounded-[1.5rem] border border-slate-100 bg-white p-8 shadow-sm">
            <p className="text-[10px] font-black uppercase tracking-widest text-slate-400 mb-2">Account Type</p>
            <p className="text-xl font-black text-heading">{currentUser?.role}</p>
          </div>
        </div>

        {showPaymentHistory && (
        <div ref={paymentHistoryRef} className="rounded-[2.5rem] border border-slate-200/80 bg-slate-50 p-10 shadow-sm dark:border-slate-700/80 dark:bg-slate-900/80">
          <div className="mb-8 flex flex-wrap items-center justify-between gap-4">
            <div>
              <h2 className="text-xl font-black text-heading">Payment History</h2>
              <p className="mt-1 text-[10px] font-black uppercase tracking-[0.2em] text-brand dark:text-indigo-300">Every support payment you have made</p>
            </div>
            <div className="rounded-2xl border border-slate-200 bg-white px-5 py-4 text-right shadow-sm dark:border-slate-700 dark:bg-slate-950/70">
              <p className="text-[10px] font-black uppercase tracking-widest text-slate-500 dark:text-slate-400">Total Support Given</p>
              <p className="mt-1 flex items-center justify-end gap-1 text-lg font-black text-heading">
                <IndianRupee className="h-4 w-4" />
                {(payments.reduce((sum, payment) => sum + payment.amount, 0) / 100).toFixed(2)}
              </p>
            </div>
          </div>

          {payments.length === 0 ? (
            <div className="rounded-3xl border border-dashed border-slate-300 bg-white/70 px-6 py-10 text-center dark:border-slate-700 dark:bg-slate-950/40">
              <Receipt className="mx-auto h-10 w-10 text-slate-400 dark:text-slate-500" />
              <p className="mt-4 text-sm font-bold text-heading">No payment history yet</p>
              <p className="mt-2 text-sm text-slate-600 dark:text-slate-400">Your successful author support payments will appear here.</p>
            </div>
          ) : (
            <div className="space-y-4">
              {payments.map((payment) => (
                <div
                  key={payment.paymentId}
                  className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm transition hover:-translate-y-0.5 hover:border-slate-300 hover:shadow-md dark:border-slate-700 dark:bg-slate-950/55 dark:hover:border-slate-600"
                >
                  <div className="flex flex-wrap items-start justify-between gap-4">
                    <div>
                      <p className="text-base font-black text-heading">
                        {payment.postTitle || "Support payment"}
                      </p>
                      <p className="mt-1 text-sm font-semibold text-slate-600 dark:text-slate-300">
                        Sent to {payment.authorName || `Author #${payment.authorId ?? "-"}`}
                      </p>
                    </div>
                    <div className="text-right">
                      <p className="flex items-center justify-end gap-1 text-xl font-black text-emerald-600 dark:text-emerald-300">
                        <IndianRupee className="h-4 w-4" />
                        {(payment.amount / 100).toFixed(2)}
                      </p>
                      <p className="text-[10px] font-black uppercase tracking-widest text-emerald-700 dark:text-emerald-200">
                        {payment.status}
                      </p>
                    </div>
                  </div>
                  <div className="mt-5 flex flex-wrap gap-x-6 gap-y-2 border-t border-slate-200 pt-4 text-xs font-bold text-slate-500 dark:border-slate-700 dark:text-slate-400">
                    <span>Payment ID: {payment.paymentId}</span>
                    <span>Order ID: {payment.orderId}</span>
                    <span>
                      Paid on{" "}
                      {new Date(payment.paidAt).toLocaleString("en-IN", {
                        day: "numeric",
                        month: "short",
                        year: "numeric",
                        hour: "numeric",
                        minute: "2-digit"
                      })}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
        )}
      </div>
    </div>
  );
};

export default ProfileView;
