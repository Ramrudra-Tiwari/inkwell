import { useEffect, useState, Fragment } from "react";
import {
  Shield, Users, MessageSquare, BarChart3,
  FileText, CheckCircle, Activity, ArrowRight,
  Tag as TagIcon, Mail, Plus, Trash2, X, Bell
} from "lucide-react";
import adminService, { AdminAnalytics, AdminUser, AuditLog, MediaFile } from "../services/adminService";
import { Comment } from "../types/comment";
import { Post } from "../types/post";
import { Category, Tag } from "../types/taxonomy";
import { Subscriber } from "../types/messaging";
import taxonomyService from "../services/taxonomyService";
import commentService from "../services/commentService";
import { useToast } from "../components/GlobalToastProvider";
import { BASE_URL } from "../api/axiosInstance";

type ModerationComment = Comment & { postTitle: string };

const defaultAnalytics: AdminAnalytics = {
  totalPosts: 0,
  publishedPosts: 0,
  draftPosts: 0,
  totalViews: 0,
  totalLikes: 0,
  totalComments: 0
};

const AdminDashboard = () => {
  const { showToast } = useToast();
  const [adminMessage, setAdminMessage] = useState("");
  const [analytics, setAnalytics] = useState<AdminAnalytics>(defaultAnalytics);
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [posts, setPosts] = useState<Post[]>([]);
  const [comments, setComments] = useState<ModerationComment[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [tags, setTags] = useState<Tag[]>([]);
  const [subscribers, setSubscribers] = useState<Subscriber[]>([]);
  const [auditLogs, setAuditLogs] = useState<AuditLog[]>([]);
  const [mediaFiles, setMediaFiles] = useState<MediaFile[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

  // Modal states
  const [isCategoryModalOpen, setIsCategoryModalOpen] = useState(false);
  const [selectedImage, setSelectedImage] = useState<string | null>(null);
  const [newCat, setNewCat] = useState({ name: "", slug: "", description: "", parentCategoryId: "" });
  const [campaign, setCampaign] = useState({ subject: "", body: "" });
  const [broadcast, setBroadcast] = useState({ title: "", message: "", role: "ALL" });
  const [isDispatching, setIsDispatching] = useState(false);

  const loadAdminDashboard = async () => {
    try {
      setIsLoading(true);
      setError("");

      const [message, metrics, managedUsers, platformPosts, recentComments, allCats, allTags, allSubs, logs, library] = await Promise.all([
        adminService.getAdminMessage(),
        adminService.getAnalytics(),
        adminService.getUsers(),
        adminService.getPlatformPosts(),
        adminService.getRecentComments(),
        taxonomyService.getCategories(),
        taxonomyService.getTrending(20),
        adminService.getSubscribers(),
        adminService.getAuditLogs(),
        adminService.getMediaLibrary()
      ]);

      setAdminMessage(message);
      setAnalytics(metrics);
      setUsers(managedUsers);
      setPosts(platformPosts);
      setComments(recentComments);
      setCategories(allCats);
      setTags(allTags);
      setSubscribers(allSubs);
      setAuditLogs(logs);
      setMediaFiles(library);
    } catch (loadError) {
      console.error(loadError);
      setError("Unable to load the admin dashboard right now.");
    } finally {
      setIsLoading(false);
    }
  };

  const handleDeleteTag = async (id: number) => {
    try {
      await adminService.deleteTag(id);
      showToast("Tag purged.", "success");
      void loadAdminDashboard();
    } catch (e) {
      showToast("Purge failed.", "error");
    }
  };

  const handleCreateCategory = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const payload = {
        ...newCat,
        parentCategoryId: newCat.parentCategoryId ? parseInt(newCat.parentCategoryId) : null
      };
      await adminService.createCategory(payload);
      setNewCat({ name: "", slug: "", description: "", parentCategoryId: "" });
      setIsCategoryModalOpen(false);
      showToast("New category established.", "success");
      void loadAdminDashboard();
    } catch (e) {
      showToast("Creation failed.", "error");
    }
  };

  const handleDeleteCategory = async (id: number) => {
    try {
      await adminService.deleteCategory(id);
      showToast("Category removed.", "success");
      void loadAdminDashboard();
    } catch (e) {
      showToast("Removal failed.", "error");
    }
  };

  const CategoryItem = ({ cat, level = 0 }: { cat: Category; level?: number }) => (
    <div className="space-y-2">
      <div 
        className="flex items-center justify-between rounded-xl border border-slate-50 bg-slate-50/30 px-4 py-2 transition hover:bg-white"
        style={{ marginLeft: `${level * 20}px` }}
      >
        <div className="flex items-center gap-2">
          {level > 0 && <ArrowRight className="h-3 w-3 text-slate-300" />}
          <span className={`text-xs font-bold ${level === 0 ? 'text-heading' : 'text-slate-600'}`}>{cat.name}</span>
        </div>
        <button 
          onClick={() => handleDeleteCategory(cat.categoryId)}
          className="text-slate-300 hover:text-red-500 transition"
        >
          <Trash2 className="h-3.5 w-3.5" />
        </button>
      </div>
      {cat.subCategories && cat.subCategories.length > 0 && (
        <div className="space-y-2">
          {cat.subCategories.map(sub => (
            <CategoryItem key={sub.categoryId} cat={sub} level={level + 1} />
          ))}
        </div>
      )}
    </div>
  );

  useEffect(() => {
    void loadAdminDashboard();
  }, []);

  const handleUserAction = async (userId: number, action: 'ROLE' | 'STATUS' | 'DELETE', value?: any) => {
    try {
      if (action === 'ROLE') await adminService.updateUserRole(userId, value);
      else if (action === 'STATUS') await adminService.toggleUserStatus(userId, value);
      else if (action === 'DELETE') await adminService.deleteUser(userId);
      void loadAdminDashboard();
    } catch (e) {
      console.error(e);
    }
  };

  const handlePostAction = async (postId: number, action: 'PIN' | 'DELETE', value?: any) => {
    try {
      if (action === 'PIN') await adminService.togglePostPin(postId, value);
      else if (action === 'DELETE') await adminService.deletePost(postId);
      void loadAdminDashboard();
    } catch (e) {
      console.error(e);
    }
  };

  const handleSendCampaign = async () => {
    if (!campaign.subject || !campaign.body) {
      showToast("Both subject and body are required.", "error");
      return;
    }

    try {
      setIsDispatching(true);
      await adminService.broadcastCampaign(campaign.subject, campaign.body);
      setCampaign({ subject: "", body: "" });
      showToast("Platform campaign broadcasted successfully.", "success");
    } catch (e) {
      showToast("Failed to send campaign.", "error");
    } finally {
      setIsDispatching(false);
    }
  };

  const handleBroadcastNotification = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!broadcast.title || !broadcast.message) {
      showToast("Title and message are required.", "error");
      return;
    }
    try {
      setIsDispatching(true);
      await adminService.broadcastNotification(broadcast.title, broadcast.message, broadcast.role);
      setBroadcast({ title: "", message: "", role: "ALL" });
      showToast("Broadcast notification sent successfully.", "success");
    } catch (e) {
      showToast("Failed to send broadcast.", "error");
    } finally {
      setIsDispatching(false);
    }
  };

  const handleDeleteMedia = async (mediaId: number) => {
    if (!window.confirm("Are you sure you want to delete this media file?")) return;
    try {
      await adminService.deleteMedia(mediaId);
      showToast("Media deleted.", "success");
      void loadAdminDashboard();
    } catch (e) {
      showToast("Deletion failed.", "error");
    }
  };

  const handleApproveSubscriber = async (subscriberId: number) => {
    try {
      await adminService.approveSubscriber(subscriberId);
      setSubscribers((current) =>
        current.map((subscriber) =>
          subscriber.subscriberId === subscriberId
            ? { ...subscriber, status: "ACTIVE" }
            : subscriber
        )
      );
      showToast("Subscriber approved successfully.", "success");
    } catch (e) {
      showToast("Failed to approve subscriber.", "error");
    }
  };

  const handleModerationAction = async (commentId: number, action: "APPROVED" | "REJECTED" | "REMOVE") => {
    try {
      if (action === "REMOVE") {
        await commentService.moderateDelete(commentId);
        setComments((current) => current.filter((comment) => comment.commentId !== commentId));
        showToast("Comment removed from moderation stream.", "success");
        return;
      }

      await commentService.updateStatus(commentId, action);
      setComments((current) =>
        current.map((comment) =>
          comment.commentId === commentId ? { ...comment, status: action } : comment
        )
      );
      showToast(`Comment ${action === "APPROVED" ? "approved" : "rejected"}.`, "success");
    } catch (e) {
      showToast("Moderation action failed.", "error");
    }
  };

  const [isModerationRequired, setIsModerationRequired] = useState(false);

  const handleToggleModeration = async (required: boolean) => {
    try {
      await adminService.toggleModerationMode(required);
      setIsModerationRequired(required);
      showToast(`Global moderation is now ${required ? 'ENFORCED' : 'RELAXED'}.`, "success");
    } catch (e) {
      showToast("Failed to update moderation settings.", "error");
    }
  };

  const handlePinPost = async (postId: number, isFeatured: boolean) => {
    try {
      await adminService.togglePostPin(postId, isFeatured);
      showToast(isFeatured ? "Story pinned to top." : "Story unpinned.", "success");
      void loadAdminDashboard();
    } catch (e) {
      showToast("Failed to update story status.", "error");
    }
  };

  if (isLoading) {
    return (
      <div className="flex min-h-[40vh] items-center justify-center p-20">
        <div className="h-12 w-12 animate-spin rounded-full border-4 border-slate-100 border-t-brand" />
      </div>
    );
  }

  if (error) {
    return (
      <div className="mx-auto max-w-lg p-20 text-center">
        <div className="mx-auto mb-6 flex h-16 w-16 items-center justify-center rounded-full bg-red-50 text-red-500">
          <Shield className="h-8 w-8" />
        </div>
        <h2 className="text-xl font-black text-heading">Access Restricted</h2>
        <p className="mt-2 text-sm font-medium text-muted">{error}</p>
        <button 
          onClick={() => window.location.reload()}
          className="mt-8 rounded-xl bg-slate-900 px-6 py-3 text-[10px] font-black uppercase tracking-widest text-white transition hover:bg-brand"
        >
          Retry Connection
        </button>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[#f8fafc]">
      {/* Page Header - Premium Glassmorphism */}
      <div className="sticky top-0 z-[40] border-b border-white/20 bg-white/60 backdrop-blur-xl">
        <div className="mx-auto max-w-7xl px-4 py-6">
          <div className="flex flex-wrap items-center justify-between gap-6">
            <div className="flex items-center gap-5">
              <div className="relative group">
                <div className="absolute inset-0 bg-brand blur-xl opacity-20 group-hover:opacity-40 transition-opacity" />
                <div className="relative flex h-14 w-14 items-center justify-center rounded-[1.5rem] bg-slate-900 text-white shadow-2xl transition-transform hover:scale-105">
                  <Shield className="h-7 w-7" />
                </div>
              </div>
              <div>
                <p className="text-[10px] font-black uppercase tracking-[0.3em] text-brand mb-1">Nexus Core</p>
                <h1 className="text-3xl font-black tracking-tighter text-heading">System <span className="bg-gradient-brand bg-clip-text text-transparent">Admin</span></h1>
              </div>
            </div>
            
            <div className="flex items-center gap-4">
              <div className="flex items-center gap-3 bg-white/80 px-5 py-2.5 rounded-2xl border border-white shadow-sm">
                <div className="flex flex-col">
                  <span className="text-[9px] font-black uppercase tracking-widest text-slate-400 leading-none mb-1">Auto Moderation</span>
                  <span className={`text-[10px] font-bold ${isModerationRequired ? 'text-brand' : 'text-slate-400'}`}>
                    {isModerationRequired ? 'ACTIVE' : 'INACTIVE'}
                  </span>
                </div>
                <button 
                  onClick={() => handleToggleModeration(!isModerationRequired)}
                  className={`h-6 w-11 rounded-full transition-all relative ${isModerationRequired ? 'bg-brand shadow-lg shadow-brand/30' : 'bg-slate-200'}`}
                >
                  <div className={`absolute top-1 h-4 w-4 rounded-full bg-white shadow-sm transition-all ${isModerationRequired ? 'left-6' : 'left-1'}`} />
                </button>
              </div>

              {adminMessage && (
                <div className="hidden md:flex rounded-2xl bg-emerald-500/10 px-5 py-2.5 text-[10px] font-black uppercase tracking-widest text-emerald-600 border border-emerald-500/20 items-center gap-2.5 animate-pulse">
                  <div className="h-2 w-2 rounded-full bg-emerald-500 shadow-[0_0_8px_rgba(16,185,129,0.6)]" />
                  System Healthy
                </div>
              )}
              
              <button 
                onClick={loadAdminDashboard} 
                className="group rounded-2xl bg-white p-3 text-slate-400 hover:bg-brand hover:text-white transition-all shadow-sm hover:shadow-lg active:scale-90"
              >
                <Activity className={`h-5 w-5 transition-transform ${isLoading ? 'animate-spin' : 'group-hover:rotate-180'}`} />
              </button>
            </div>
          </div>
        </div>
      </div>

      <div className="mx-auto max-w-6xl px-4 py-8 space-y-8">
        {/* Analytics Cards */}
        <div className="grid gap-6 md:grid-cols-2 xl:grid-cols-4">
          {[
            { label: "Total Artifacts", value: analytics.totalPosts, icon: <FileText className="h-6 w-6" />, grad: "from-indigo-600 to-brand" },
            { label: "Public Stories", value: analytics.publishedPosts, icon: <CheckCircle className="h-6 w-6" />, grad: "from-emerald-500 to-teal-600" },
            { label: "Discussions", value: analytics.totalComments, icon: <MessageSquare className="h-6 w-6" />, grad: "from-brand to-accent" },
            { label: "Global Reach", value: analytics.totalViews, icon: <BarChart3 className="h-6 w-6" />, grad: "from-slate-800 to-slate-900" }
          ].map((card) => (
            <div
              key={card.label}
              className="group relative overflow-hidden rounded-[2rem] border border-white bg-white p-8 shadow-[0_8px_30px_rgb(0,0,0,0.04)] transition-all hover:shadow-xl hover:shadow-brand/5 hover:-translate-y-1"
            >
              <div className={`absolute right-0 top-0 h-32 w-32 translate-x-12 -translate-y-12 rounded-full bg-gradient-to-br ${card.grad} opacity-[0.03] group-hover:opacity-[0.06] transition-opacity`} />
              <div className={`mb-6 flex h-14 w-14 items-center justify-center rounded-2xl bg-gradient-to-br ${card.grad} text-white shadow-xl group-hover:scale-110 transition-transform`}>
                {card.icon}
              </div>
              <p className="text-[10px] font-black uppercase tracking-[0.2em] text-slate-400">{card.label}</p>
              <p className="mt-2 text-4xl font-black tracking-tight text-heading">{card.value.toLocaleString()}</p>
            </div>
          ))}
        </div>

        {/* Main Sections */}
        <div className="grid gap-8 lg:grid-cols-[0.45fr_0.55fr]">
          {/* User Directory */}
          <section className="rounded-[2.5rem] border border-white bg-white/80 shadow-[0_32px_64px_-12px_rgba(0,0,0,0.06)] backdrop-blur-xl overflow-hidden flex flex-col">
            <div className="flex items-center justify-between border-b border-slate-100/50 px-8 py-6">
              <div className="flex items-center gap-3">
                <div className="flex h-8 w-8 items-center justify-center rounded-xl bg-brand/10 text-brand">
                  <Users className="h-4 w-4" />
                </div>
                <h2 className="text-sm font-black uppercase tracking-[0.2em] text-heading">Identity Registry</h2>
              </div>
              <span className="rounded-full bg-slate-100 px-4 py-1.5 text-[9px] font-black text-slate-500 uppercase tracking-widest">
                {users.length} Entities
              </span>
            </div>

            <div className="p-8 space-y-5 flex-1 overflow-y-auto max-h-[600px] scrollbar-thin">
              {users.map((user) => (
                <div key={user.userId} className="group relative rounded-3xl border border-transparent bg-slate-50/50 p-5 transition-all hover:bg-white hover:border-brand/10 hover:shadow-xl hover:shadow-brand/5">
                  <div className="flex items-center justify-between mb-5">
                    <div className="flex items-center gap-4 min-w-0">
                      <div className="h-10 w-10 rounded-2xl bg-gradient-to-br from-brand/10 to-accent/10 flex items-center justify-center text-brand font-black text-xs shadow-inner">
                        {user.fullName.charAt(0)}
                      </div>
                      <div className="min-w-0">
                        <p className="truncate text-sm font-black text-heading group-hover:text-brand transition-colors">{user.fullName}</p>
                        <p className="truncate text-[10px] font-bold text-slate-400 uppercase tracking-widest">{user.email}</p>
                      </div>
                    </div>
                    <div className="flex flex-col items-end gap-1">
                       <div className={`h-1.5 w-1.5 rounded-full ${user.isActive ? 'bg-emerald-500 shadow-[0_0_8px_rgba(16,185,129,0.4)]' : 'bg-red-400'}`} />
                       <span className="text-[8px] font-black uppercase tracking-widest text-slate-300">{user.isActive ? 'Active' : 'Suspended'}</span>
                    </div>
                  </div>
                  
                  <div className="flex flex-wrap items-center gap-2">
                    <div className="relative">
                      <select 
                        value={user.role}
                        onChange={(e) => handleUserAction(user.userId, 'ROLE', e.target.value)}
                        className="appearance-none rounded-xl bg-white border border-slate-200 pl-3 pr-8 py-2 text-[9px] font-black uppercase tracking-widest text-slate-600 outline-none hover:border-brand transition-all cursor-pointer"
                      >
                        <option value="READER">Reader</option>
                        <option value="AUTHOR">Author</option>
                        <option value="ADMIN">Admin</option>
                      </select>
                      <div className="pointer-events-none absolute right-2 top-1/2 -translate-y-1/2 text-slate-400">
                        <Activity className="h-3 w-3" />
                      </div>
                    </div>

                    <div className="h-4 w-px bg-slate-200 mx-1" />

                    <button 
                      onClick={() => handleUserAction(user.userId, 'STATUS', !user.isActive)}
                      className={`rounded-xl px-4 py-2 text-[9px] font-black uppercase tracking-widest transition-all ${
                        user.isActive 
                          ? 'bg-amber-50 text-amber-600 hover:bg-amber-600 hover:text-white hover:shadow-lg hover:shadow-amber-200' 
                          : 'bg-emerald-50 text-emerald-600 hover:bg-emerald-600 hover:text-white hover:shadow-lg hover:shadow-emerald-200'
                      }`}
                    >
                      {user.isActive ? 'Suspend' : 'Activate'}
                    </button>
                    <button 
                      onClick={() => handleUserAction(user.userId, 'DELETE')}
                      className="rounded-xl bg-slate-100 px-4 py-2 text-[9px] font-black uppercase tracking-widest text-slate-400 hover:bg-red-500 hover:text-white hover:shadow-lg hover:shadow-red-200 transition-all"
                    >
                      Expel
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </section>

          {/* Activity Logs / Moderation */}
          <div className="space-y-8">
            {/* Recent Comments */}
            <section className="rounded-[2.5rem] border border-white bg-white/80 shadow-[0_32px_64px_-12px_rgba(0,0,0,0.06)] backdrop-blur-xl overflow-hidden">
              <div className="flex items-center justify-between border-b border-slate-100/50 px-8 py-6">
                <div className="flex items-center gap-3">
                  <div className="flex h-8 w-8 items-center justify-center rounded-xl bg-brand/10 text-brand">
                    <MessageSquare className="h-4 w-4" />
                  </div>
                  <h2 className="text-sm font-black uppercase tracking-[0.2em] text-heading">Moderation Stream</h2>
                </div>
              </div>

              <div className="p-8 space-y-4">
                {comments.length === 0 ? (
                  <div className="py-12 text-center">
                    <CheckCircle className="mx-auto h-12 w-12 text-slate-100 mb-4" />
                    <p className="text-xs font-bold text-slate-400 uppercase tracking-widest">Queue is Clear</p>
                  </div>
                ) : (
                  comments.slice(0, 4).map((comment) => (
                    <div key={comment.commentId} className="group relative rounded-3xl border border-slate-100 bg-white p-6 transition-all hover:shadow-xl hover:shadow-brand/5 hover:border-brand/10">
                      <div className="flex items-center gap-2 mb-3">
                        <FileText className="h-3 w-3 text-brand" />
                        <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest truncate">Ref: {comment.postTitle}</p>
                      </div>
                      <p className="text-sm font-medium text-heading leading-relaxed mb-6 italic">"{comment.content}"</p>
                      <div className="flex items-center justify-between">
                        <div className="flex gap-2">
                          {comment.status !== "APPROVED" && (
                            <button
                              onClick={() => void handleModerationAction(comment.commentId, "APPROVED")}
                              className="rounded-xl bg-emerald-500 px-5 py-2 text-[9px] font-black uppercase tracking-widest text-white shadow-lg shadow-emerald-200 hover:scale-105 transition-transform active:scale-95"
                            >
                              Approve
                            </button>
                          )}
                          {comment.status !== "REJECTED" && (
                            <button
                              onClick={() => void handleModerationAction(comment.commentId, "REJECTED")}
                              className="rounded-xl bg-violet-50 px-5 py-2 text-[9px] font-black uppercase tracking-widest text-violet-600 hover:bg-violet-600 hover:text-white hover:shadow-lg transition-all active:scale-95"
                            >
                              Reject
                            </button>
                          )}
                          <button
                            onClick={() => void handleModerationAction(comment.commentId, "REMOVE")}
                            className="rounded-xl bg-slate-100 px-5 py-2 text-[9px] font-black uppercase tracking-widest text-slate-500 hover:bg-red-500 hover:text-white hover:shadow-lg transition-all active:scale-95"
                          >
                            Remove
                          </button>
                        </div>
                        <span className="text-[8px] font-bold text-slate-300 uppercase tracking-widest">{comment.status}</span>
                      </div>
                    </div>
                  ))
                )}
              </div>
            </section>

            {/* Platform Analytics / Latest Posts */}
            <section className="rounded-[2.5rem] border border-white bg-white/80 shadow-[0_32px_64px_-12px_rgba(0,0,0,0.06)] backdrop-blur-xl overflow-hidden">
              <div className="flex items-center justify-between border-b border-slate-100/50 px-8 py-6">
                <div className="flex items-center gap-3">
                  <div className="flex h-8 w-8 items-center justify-center rounded-xl bg-slate-900 text-white shadow-lg">
                    <BarChart3 className="h-4 w-4" />
                  </div>
                  <h2 className="text-sm font-black uppercase tracking-[0.2em] text-heading">High Velocity Content</h2>
                </div>
              </div>

              <div className="p-8 grid gap-4 md:grid-cols-2">
                {posts.slice(0, 4).map((post) => (
                  <div key={post.postId} className="group flex flex-col justify-between rounded-3xl border border-slate-100 bg-slate-50/30 p-5 transition-all hover:bg-white hover:shadow-xl hover:shadow-brand/5">
                    <div>
                      <h3 className="text-xs font-black text-heading line-clamp-2 uppercase tracking-wide leading-tight group-hover:text-brand transition-colors">{post.title}</h3>
                      <div className="mt-4 flex items-center gap-4 text-[9px] font-black text-slate-400 uppercase tracking-widest">
                        <div className="flex items-center gap-1.5">
                          <Activity className="h-3 w-3 text-brand" />
                          <span>{post.viewCount} Views</span>
                        </div>
                        <div className="flex items-center gap-1.5">
                          <CheckCircle className="h-3 w-3 text-emerald-500" />
                          <span>{post.likesCount} Likes</span>
                        </div>
                      </div>
                    </div>
                    <div className="mt-6 flex items-center justify-between pt-4 border-t border-slate-100/50">
                      <button 
                        onClick={() => handlePostAction(post.postId, 'PIN', !post.isFeatured)}
                        className={`rounded-xl px-4 py-2 text-[8px] font-black uppercase tracking-widest transition-all ${
                          post.isFeatured 
                            ? 'bg-brand text-white shadow-lg shadow-brand/30' 
                            : 'bg-white border border-slate-200 text-slate-400 hover:border-brand hover:text-brand shadow-sm'
                        }`}
                      >
                        {post.isFeatured ? 'Featured Spotlight' : 'Spotlight Story'}
                      </button>
                      <button 
                        onClick={() => handlePostAction(post.postId, 'DELETE')}
                        className="h-8 w-8 flex items-center justify-center rounded-xl bg-slate-100 text-slate-300 hover:bg-red-50 hover:text-red-500 transition-all"
                      >
                        <Trash2 className="h-3.5 w-3.5" />
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </section>
          </div>
        </div>


        <div className="grid gap-8 lg:grid-cols-[0.6fr_0.4fr]">
           {/* Taxonomy Management */}
           <section className="rounded-[2.5rem] border border-white bg-white/80 shadow-[0_32px_64px_-12px_rgba(0,0,0,0.06)] backdrop-blur-xl overflow-hidden">
            <div className="flex items-center justify-between border-b border-slate-100/50 px-8 py-6">
              <div className="flex items-center gap-3">
                <div className="flex h-8 w-8 items-center justify-center rounded-xl bg-accent/10 text-accent">
                  <TagIcon className="h-4 w-4" />
                </div>
                <h2 className="text-sm font-black uppercase tracking-[0.2em] text-heading">Taxonomy & Mapping</h2>
              </div>
              <button 
                onClick={() => setIsCategoryModalOpen(true)}
                className="group inline-flex items-center gap-2 rounded-2xl bg-brand px-5 py-2.5 text-[9px] font-black uppercase tracking-widest text-white shadow-lg shadow-brand/30 hover:scale-105 transition-all"
              >
                <Plus className="h-3.5 w-3.5 transition-transform group-hover:rotate-90" /> Initialize Category
              </button>
            </div>
            
            <div className="p-8 grid gap-10 md:grid-cols-2">
               {/* Categories */}
               <div className="bg-slate-50/30 rounded-[2rem] p-6 border border-slate-100/50">
                  <p className="mb-6 text-[10px] font-black uppercase tracking-[0.2em] text-slate-400">Hierarchical Map</p>
                  <div className="space-y-3">
                    {categories.length === 0 ? (
                      <div className="py-8 text-center border-2 border-dashed border-slate-100 rounded-2xl">
                        <p className="text-[10px] font-bold text-slate-300 uppercase">Architecture Empty</p>
                      </div>
                    ) : (
                      categories.filter(c => !c.parentCategoryId).map(cat => (
                        <CategoryItem key={cat.categoryId} cat={cat} />
                      ))
                    )}
                  </div>
               </div>
               {/* Tags */}
               <div className="bg-slate-50/30 rounded-[2rem] p-6 border border-slate-100/50">
                  <p className="mb-6 text-[10px] font-black uppercase tracking-[0.2em] text-slate-400">Active Tags</p>
                  <div className="flex flex-wrap gap-2.5">
                    {tags.length === 0 ? (
                      <p className="text-[10px] font-bold text-slate-300 uppercase">Zero Tags Detected</p>
                    ) : (
                      tags.map(tag => (
                        <div key={tag.tagId} className="group flex items-center gap-3 rounded-xl bg-white px-4 py-2.5 border border-slate-100 shadow-sm hover:border-brand/30 hover:shadow-md transition-all">
                          <span className="text-[10px] font-black text-slate-600 uppercase tracking-tight">#{tag.name}</span>
                          <button 
                            onClick={() => handleDeleteTag(tag.tagId)}
                            className="text-slate-200 hover:text-red-500 transition-colors"
                          >
                            <Trash2 className="h-3.5 w-3.5" />
                          </button>
                        </div>
                      ))
                    )}
                  </div>
               </div>
            </div>
           </section>

           {/* Newsletter Dispatch */}
           <div className="space-y-8">
             <section className="rounded-[2.5rem] border border-white bg-white/80 shadow-[0_32px_64px_-12px_rgba(0,0,0,0.06)] backdrop-blur-xl overflow-hidden">
               <div className="flex items-center justify-between border-b border-slate-100/50 px-8 py-6">
                  <div className="flex items-center gap-3">
                    <div className="flex h-8 w-8 items-center justify-center rounded-xl bg-brand/10 text-brand">
                      <Mail className="h-4 w-4" />
                    </div>
                    <h2 className="text-sm font-black uppercase tracking-[0.2em] text-heading">Campaign Nexus</h2>
                  </div>
               </div>
               <div className="p-8 space-y-6">
                  <div>
                    <label className="mb-3 block text-[10px] font-black uppercase tracking-[0.2em] text-slate-400">Dispatch Subject</label>
                    <input 
                      value={campaign.subject}
                      onChange={e => setCampaign({...campaign, subject: e.target.value})}
                      placeholder="The Weekly Insight"
                      className="w-full rounded-2xl border border-slate-200 bg-white/50 p-4 text-sm font-bold text-heading outline-none focus:border-brand focus:ring-4 focus:ring-brand/5 shadow-inner transition-all"
                    />
                  </div>
                  <div>
                    <label className="mb-3 block text-[10px] font-black uppercase tracking-[0.2em] text-slate-400">Content Manifest</label>
                    <textarea 
                      value={campaign.body}
                      onChange={e => setCampaign({...campaign, body: e.target.value})}
                      className="w-full rounded-2xl border border-slate-200 bg-white/50 p-4 text-sm font-medium text-heading outline-none focus:border-brand focus:ring-4 focus:ring-brand/5 shadow-inner transition-all"
                      rows={4}
                      placeholder="Drafting platform-wide broadcast..."
                    />
                  </div>
                  <button 
                    onClick={handleSendCampaign}
                    disabled={isDispatching}
                    className="relative group w-full overflow-hidden rounded-2xl bg-slate-900 py-5 text-[10px] font-black uppercase tracking-[0.3em] text-white shadow-2xl hover:shadow-brand/20 disabled:opacity-50 transition-all"
                  >
                    <div className="absolute inset-0 bg-gradient-to-r from-brand to-accent opacity-0 group-hover:opacity-100 transition-opacity duration-300" />
                    <span className="relative z-10">{isDispatching ? "TRANSMITTING..." : "AUTHORIZE BROADCAST"}</span>
                  </button>
               </div>
             </section>

             <section className="rounded-[2.5rem] border border-white bg-white/80 shadow-[0_32px_64px_-12px_rgba(0,0,0,0.06)] backdrop-blur-xl overflow-hidden">
               <div className="flex items-center justify-between border-b border-slate-100/50 px-8 py-6">
                  <div className="flex items-center gap-3">
                    <div className="flex h-8 w-8 items-center justify-center rounded-xl bg-slate-900 text-white shadow-lg">
                      <Bell className="h-4 w-4" />
                    </div>
                    <h2 className="text-sm font-black uppercase tracking-[0.2em] text-heading">Live Alerts</h2>
                  </div>
               </div>
               <form onSubmit={handleBroadcastNotification} className="p-8 space-y-6">
                  <div>
                    <label className="mb-3 block text-[10px] font-black uppercase tracking-[0.2em] text-slate-400">Broadcast Title</label>
                    <input 
                      value={broadcast.title}
                      onChange={e => setBroadcast({...broadcast, title: e.target.value})}
                      placeholder="Maintenance Update"
                      className="w-full rounded-2xl border border-slate-200 bg-white/50 p-4 text-sm font-bold text-heading outline-none focus:border-brand focus:ring-4 focus:ring-brand/5 shadow-inner transition-all"
                    />
                  </div>
                  <div>
                    <label className="mb-3 block text-[10px] font-black uppercase tracking-[0.2em] text-slate-400">Alert Message</label>
                    <textarea
                      value={broadcast.message}
                      onChange={e => setBroadcast({ ...broadcast, message: e.target.value })}
                      placeholder="Share the exact alert you want every selected user to receive."
                      rows={5}
                      className="w-full resize-none rounded-2xl border border-slate-200 bg-white/50 p-4 text-sm font-medium text-heading outline-none focus:border-brand focus:ring-4 focus:ring-brand/5 shadow-inner transition-all"
                    />
                  </div>
                  <div>
                    <label className="mb-3 block text-[10px] font-black uppercase tracking-[0.2em] text-slate-400">Target Segment</label>
                    <div className="relative">
                      <select 
                        value={broadcast.role}
                        onChange={e => setBroadcast({...broadcast, role: e.target.value})}
                        className="w-full appearance-none rounded-2xl border border-slate-200 bg-white/50 p-4 text-sm font-bold text-heading outline-none focus:border-brand focus:ring-4 focus:ring-brand/5 shadow-inner transition-all cursor-pointer"
                      >
                        <option value="ALL">Entire Population</option>
                        <option value="READER">Readers Syndicate</option>
                        <option value="AUTHOR">Authors Collective</option>
                      </select>
                      <div className="pointer-events-none absolute right-4 top-1/2 -translate-y-1/2 text-slate-400">
                        <Users className="h-4 w-4" />
                      </div>
                    </div>
                  </div>
                  <button 
                    disabled={isDispatching}
                    className="group w-full rounded-2xl bg-brand py-5 text-[10px] font-black uppercase tracking-[0.3em] text-white shadow-xl shadow-brand/20 hover:scale-[1.02] active:scale-95 transition-all"
                  >
                    {isDispatching ? "BROADCASTING..." : "DISPATCH SYSTEM ALERT"}
                  </button>
               </form>
             </section>
           </div>
        </div>

        {/* Newsletter Subscribers Table */}
        <section className="rounded-[2.5rem] border border-white bg-white shadow-[0_32px_64px_-12px_rgba(0,0,0,0.06)] overflow-hidden">
          <div className="flex items-center justify-between border-b border-slate-100/50 px-8 py-6 bg-slate-50/50">
            <div className="flex items-center gap-3">
              <div className="flex h-8 w-8 items-center justify-center rounded-xl bg-indigo-500/10 text-indigo-600">
                <Users className="h-4 w-4" />
              </div>
              <h2 className="text-sm font-black uppercase tracking-[0.2em] text-heading">Syndicate Subscribers</h2>
            </div>
            <span className="rounded-full bg-indigo-50 px-4 py-1.5 text-[9px] font-black text-indigo-600 uppercase tracking-widest">
              {subscribers.length} Global
            </span>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-slate-50/30">
                  <th className="px-8 py-5 text-[9px] font-black uppercase tracking-[0.2em] text-slate-400 border-b border-slate-100">Identity / Email</th>
                  <th className="px-8 py-5 text-[9px] font-black uppercase tracking-[0.2em] text-slate-400 border-b border-slate-100">Protocol Status</th>
                  <th className="px-8 py-5 text-[9px] font-black uppercase tracking-[0.2em] text-slate-400 border-b border-slate-100">Registry Date</th>
                  <th className="px-8 py-5 text-[9px] font-black uppercase tracking-[0.2em] text-slate-400 border-b border-slate-100">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100/50">
                {subscribers.map((sub) => (
                  <tr key={sub.subscriberId} className="group hover:bg-slate-50/30 transition-colors">
                    <td className="px-8 py-5 text-sm font-black text-heading">{sub.email}</td>
                    <td className="px-8 py-5">
                      <div className={`inline-flex items-center gap-1.5 rounded-full px-3 py-1 text-[8px] font-black uppercase tracking-widest ${
                        sub.status === 'ACTIVE' ? 'bg-emerald-50 text-emerald-600 border border-emerald-100' : 
                        sub.status === 'PENDING' ? 'bg-amber-50 text-amber-600 border border-amber-100' : 'bg-red-50 text-red-600 border border-red-100'
                      }`}>
                        <div className={`h-1 w-1 rounded-full ${sub.status === 'ACTIVE' ? 'bg-emerald-500' : sub.status === 'PENDING' ? 'bg-amber-500' : 'bg-red-500'}`} />
                        {sub.status}
                      </div>
                    </td>
                    <td className="px-8 py-5 text-[10px] font-bold text-muted uppercase tracking-widest">
                      {new Date(sub.subscribedAt).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}
                    </td>
                    <td className="px-8 py-5">
                      {sub.status === 'PENDING' ? (
                        <button
                          onClick={() => void handleApproveSubscriber(sub.subscriberId)}
                          className="rounded-xl bg-emerald-50 px-4 py-2 text-[9px] font-black uppercase tracking-widest text-emerald-600 transition hover:bg-emerald-600 hover:text-white"
                        >
                          Approve
                        </button>
                      ) : (
                        <span className="text-[9px] font-black uppercase tracking-widest text-slate-300">
                          No action
                        </span>
                      )}
                    </td>
                  </tr>
                ))}
                {subscribers.length === 0 && (
                  <tr>
                    <td colSpan={4} className="p-20 text-center">
                       <Mail className="mx-auto h-10 w-10 text-slate-100 mb-4" />
                       <p className="text-[10px] font-black text-slate-300 uppercase tracking-widest italic">Registry Is Empty</p>
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </section>

        {/* Media Library */}
        <section className="rounded-[2.5rem] border border-white bg-white shadow-[0_32px_64px_-12px_rgba(0,0,0,0.06)] overflow-hidden">
          <div className="flex items-center justify-between border-b border-slate-100/50 px-8 py-6">
            <div className="flex items-center gap-3">
              <div className="flex h-8 w-8 items-center justify-center rounded-xl bg-violet-500/10 text-violet-600">
                <Activity className="h-4 w-4" />
              </div>
              <h2 className="text-sm font-black uppercase tracking-[0.2em] text-heading">Media Assets</h2>
            </div>
            <span className="rounded-full bg-slate-100 px-4 py-1.5 text-[9px] font-black text-slate-500 uppercase tracking-widest">
              {mediaFiles.length} Blobs
            </span>
          </div>
          <div className="p-8 grid gap-4 grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 xl:grid-cols-8">
            {mediaFiles.map((file) => (
              <div key={file.mediaId} className="group relative aspect-square rounded-[1.5rem] border border-slate-100 overflow-hidden bg-slate-50 transition-all hover:shadow-xl hover:shadow-brand/10 hover:-translate-y-1">
                {file.mimeType.startsWith('image/') ? (
                  <button type="button" onClick={() => setSelectedImage(`${BASE_URL}${file.url}`)} className="block h-full w-full outline-none">
                    <img src={`${BASE_URL}${file.url}`} alt={file.altText} className="h-full w-full object-cover transition duration-500 group-hover:scale-110" />
                  </button>
                ) : (
                  <button type="button" onClick={() => window.open(`${BASE_URL}${file.url}`, '_blank')} className="flex h-full w-full items-center justify-center outline-none">
                    <FileText className="h-8 w-8 text-slate-300 group-hover:text-brand transition-colors" />
                  </button>
                )}
                <div className="absolute inset-0 pointer-events-none bg-slate-900/80 backdrop-blur-[2px] opacity-0 group-hover:opacity-100 transition-all duration-300 flex flex-col items-center justify-center gap-3 p-4 text-center">
                  <p className="text-[8px] font-black text-white uppercase tracking-widest line-clamp-2 leading-tight">{file.originalName}</p>
                  <div className="flex gap-2 pointer-events-auto">
                    <button 
                      onClick={() => setSelectedImage(`${BASE_URL}${file.url}`)}
                      className="rounded-lg bg-white/20 p-2.5 text-white hover:bg-white hover:text-slate-900 transition-all active:scale-90"
                      title="Inspect Asset"
                    >
                      <Activity className="h-3.5 w-3.5" />
                    </button>
                    <button 
                      onClick={() => handleDeleteMedia(file.mediaId)}
                      className="rounded-lg bg-red-500/80 p-2.5 text-white hover:bg-red-500 transition-all active:scale-90 shadow-lg shadow-red-500/20"
                    >
                      <Trash2 className="h-3.5 w-3.5" />
                    </button>
                  </div>
                </div>
              </div>
            ))}
            {mediaFiles.length === 0 && (
              <div className="col-span-full py-20 text-center border-2 border-dashed border-slate-100 rounded-3xl">
                <p className="text-[10px] font-black text-slate-300 uppercase tracking-[0.3em]">Vault is Empty</p>
              </div>
            )}
          </div>
        </section>

        {/* System Audit Logs */}
        <section className="mb-20 overflow-hidden rounded-[2.5rem] border border-slate-200 bg-slate-50 shadow-[0_32px_64px_-12px_rgba(15,23,42,0.10)] dark:border-slate-700 dark:bg-slate-900">
          <div className="flex items-center justify-between border-b border-slate-200 px-8 py-6 bg-slate-950 text-white dark:border-slate-700 dark:bg-slate-950">
            <div className="flex items-center gap-3">
              <div className="flex h-8 w-8 items-center justify-center rounded-xl bg-white/10 text-white">
                <Shield className="h-4 w-4" />
              </div>
              <h2 className="text-sm font-black uppercase tracking-[0.2em]">Platform Manifest</h2>
            </div>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-slate-200/80 dark:bg-slate-800/80">
                  <th className="border-b border-slate-300 px-8 py-5 text-[9px] font-black uppercase tracking-[0.2em] text-slate-600 dark:border-slate-700 dark:text-slate-300">Operation</th>
                  <th className="border-b border-slate-300 px-8 py-5 text-[9px] font-black uppercase tracking-[0.2em] text-slate-600 dark:border-slate-700 dark:text-slate-300">Subject</th>
                  <th className="border-b border-slate-300 px-8 py-5 text-[9px] font-black uppercase tracking-[0.2em] text-slate-600 dark:border-slate-700 dark:text-slate-300">Performer</th>
                  <th className="border-b border-slate-300 px-8 py-5 text-[9px] font-black uppercase tracking-[0.2em] text-slate-600 dark:border-slate-700 dark:text-slate-300">Chronology</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-200 dark:divide-slate-700">
                {auditLogs.slice(0, 20).map((log) => (
                  <tr key={log.id} className="group bg-white transition-colors hover:bg-blue-50/70 dark:bg-slate-900 dark:hover:bg-slate-800/80">
                    <td className="px-8 py-5">
                      <span className="rounded-lg bg-slate-950 px-3 py-1.5 text-[8px] font-black uppercase tracking-widest text-slate-100 shadow-lg shadow-slate-900/10 dark:bg-slate-800 dark:text-slate-100">
                        {log.action}
                      </span>
                    </td>
                    <td className="px-8 py-5 text-sm font-black text-slate-800 dark:text-slate-100">{log.target}</td>
                    <td className="px-8 py-5 text-[9px] font-black uppercase tracking-widest text-slate-600 dark:text-sky-200">{log.performedBy}</td>
                    <td className="px-8 py-5 text-[10px] font-bold uppercase text-slate-600 dark:text-slate-300">
                      {new Date(log.timestamp).toLocaleString('en-US', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })}
                    </td>
                  </tr>
                ))}
                {auditLogs.length === 0 && (
                  <tr>
                    <td colSpan={4} className="p-20 text-center">
                       <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest">No Records In Ledger</p>
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </section>
      </div>


      {isCategoryModalOpen && (
        <div className="fixed inset-0 z-[100] flex items-center justify-center p-4">
          <div className="absolute inset-0 bg-slate-900/60 backdrop-blur-sm" onClick={() => setIsCategoryModalOpen(false)} />
          <div className="relative w-full max-w-lg rounded-[3rem] bg-white p-12 shadow-[0_50px_100px_-20px_rgba(0,0,0,0.25)] border border-slate-100 animate-in zoom-in-95 duration-300">
            <button onClick={() => setIsCategoryModalOpen(false)} className="absolute right-8 top-8 rounded-2xl p-3 text-slate-300 hover:bg-slate-50 hover:text-slate-600 transition-all">
              <X className="h-6 w-6" />
            </button>
            
            <div className="mb-10">
              <p className="text-[10px] font-black uppercase tracking-[0.3em] text-brand mb-2">Structure Architect</p>
              <h2 className="text-3xl font-black tracking-tight text-heading">New Taxonomy Node</h2>
            </div>
            
            <form onSubmit={handleCreateCategory} className="space-y-6">
              <div>
                <label className="mb-3 block text-[10px] font-black uppercase tracking-[0.2em] text-slate-400">Node Designation</label>
                <input 
                  required
                  value={newCat.name}
                  onChange={e => setNewCat({...newCat, name: e.target.value})}
                  placeholder="e.g. Quantum Computing"
                  className="w-full rounded-2xl border border-slate-200 bg-slate-50/30 p-4 text-sm font-bold text-heading outline-none focus:border-brand focus:ring-4 focus:ring-brand/5 transition-all"
                />
              </div>
              <div>
                <label className="mb-3 block text-[10px] font-black uppercase tracking-[0.2em] text-slate-400">Slug Identifier</label>
                <input 
                  required
                  value={newCat.slug}
                  onChange={e => setNewCat({...newCat, slug: e.target.value})}
                  placeholder="quantum-computing"
                  className="w-full rounded-2xl border border-slate-200 bg-slate-50/30 p-4 text-sm font-bold text-heading outline-none focus:border-brand focus:ring-4 focus:ring-brand/5 transition-all"
                />
              </div>
              <div>
                <label className="mb-3 block text-[10px] font-black uppercase tracking-[0.2em] text-slate-400">Parent Anchor</label>
                <div className="relative">
                  <select 
                    value={newCat.parentCategoryId}
                    onChange={e => setNewCat({...newCat, parentCategoryId: e.target.value})}
                    className="w-full appearance-none rounded-2xl border border-slate-200 bg-slate-50/30 p-4 text-sm font-bold text-heading outline-none focus:border-brand focus:ring-4 focus:ring-brand/5 transition-all cursor-pointer"
                  >
                    <option value="">Root Hierarchy</option>
                    {categories.map(c => (
                      <option key={c.categoryId} value={c.categoryId}>{c.name}</option>
                    ))}
                  </select>
                  <div className="pointer-events-none absolute right-4 top-1/2 -translate-y-1/2 text-slate-400">
                    <Activity className="h-4 w-4" />
                  </div>
                </div>
              </div>
              
              <button className="relative group mt-4 w-full overflow-hidden rounded-2xl bg-slate-900 py-5 text-[10px] font-black uppercase tracking-[0.3em] text-white shadow-xl hover:shadow-brand/20 transition-all">
                <div className="absolute inset-0 bg-gradient-to-r from-brand to-accent opacity-0 group-hover:opacity-100 transition-opacity" />
                <span className="relative z-10">INITIALIZE NODE</span>
              </button>
            </form>
          </div>
        </div>
      )}

      {/* Full Size Image Modal */}
      {selectedImage && (
        <div 
          className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/90 p-4 backdrop-blur-sm animate-in fade-in duration-200"
          onClick={() => setSelectedImage(null)}
        >
          <button 
            className="absolute top-6 right-6 rounded-full bg-white/10 p-2 text-white transition hover:bg-white/20"
            onClick={() => setSelectedImage(null)}
            title="Close"
          >
            <X className="h-6 w-6" />
          </button>
          <img 
            src={selectedImage} 
            alt="Full size media" 
            className="max-h-[90vh] max-w-[90vw] rounded-lg object-contain shadow-2xl animate-in zoom-in-95 duration-200" 
            onClick={(e) => e.stopPropagation()}
          />
        </div>
      )}
    </div>
  );
};

export default AdminDashboard;
