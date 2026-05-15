import { useMemo, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  PenLine, BarChart2, FileText, BookOpen, Eye,
  Layers, Image as ImageIcon, TrendingUp, Plus, Trash2,
  MessageSquare, Mail, Send, Activity, BarChart3,
  MousePointer2, ThumbsUp, X
} from "lucide-react";
import { 
  AreaChart, Area, XAxis, YAxis, CartesianGrid, 
  Tooltip, ResponsiveContainer, BarChart, Bar, Cell 
} from 'recharts';
import { useAuth } from "../context/AuthContext";
import { useToast } from "../components/GlobalToastProvider";
import mediaService from "../services/mediaService";
import postService from "../services/postService";
import messagingService from "../services/messagingService";
import { Media } from "../types/media";
import { Post } from "../types/post";
import { Comment } from "../types/comment";
import ConfirmationModal from "../components/ConfirmationModal";
import commentService from "../services/commentService";
import { BASE_URL } from "../api/axiosInstance";

const statusBadge: Record<string, string> = {
  PUBLISHED: "bg-emerald-100 text-emerald-700",
  DRAFT: "bg-slate-100 text-slate-600",
  UNPUBLISHED: "bg-gray-100 text-gray-600",
  ARCHIVED: "bg-red-100 text-red-600",
};

const AuthorDashboard = () => {
  const { currentUser } = useAuth();
  const { showToast } = useToast();
  const [posts, setPosts] = useState<Post[]>([]);
  const [media, setMedia] = useState<Media[]>([]);
  const [comments, setComments] = useState<Comment[]>([]);
  const [activeTab, setActiveTab] = useState<"posts" | "analytics" | "moderation" | "media" | "newsletter">("posts");
  const [isLoading, setIsLoading] = useState(true);
  const [isSending, setIsSending] = useState(false);
  const [error, setError] = useState("");
  const [postToDelete, setPostToDelete] = useState<Post | null>(null);
  const [selectedImage, setSelectedImage] = useState<string | null>(null);

  // Newsletter state
  const [campaign, setCampaign] = useState({ subject: "", body: "" });

  // Mock trend data for visualization
  const trendData = useMemo(() => [
    { name: 'Mon', views: 400, likes: 240 },
    { name: 'Tue', views: 300, likes: 139 },
    { name: 'Wed', views: 200, likes: 980 },
    { name: 'Thu', views: 278, likes: 390 },
    { name: 'Fri', views: 189, likes: 480 },
    { name: 'Sat', views: 239, likes: 380 },
    { name: 'Sun', views: 349, likes: 430 },
  ], []);

  const topPostsData = useMemo(() => {
    return posts
      .sort((a, b) => b.viewCount - a.viewCount)
      .slice(0, 5)
      .map(p => ({
        name: p.title.length > 20 ? p.title.substring(0, 20) + "..." : p.title,
        views: p.viewCount,
        likes: p.likesCount
      }));
  }, [posts]);

  useEffect(() => {
    const loadDashboard = async () => {
      if (!currentUser?.userId) { setIsLoading(false); return; }
      try {
        setIsLoading(true);
        setError("");
        const [authorPosts, authorMedia, authorComments] = await Promise.all([
          postService.getByAuthor(currentUser.userId),
          mediaService.getByUploader(currentUser.userId),
          commentService.getByAuthor(currentUser.userId)
        ]);
        setPosts(authorPosts);
        setMedia(authorMedia);
        setComments(authorComments);
      } catch (e) {
        console.error(e);
        setError("Unable to load your author dashboard right now.");
      } finally {
        setIsLoading(false);
      }
    };
    void loadDashboard();
  }, [currentUser?.userId]);

  const handleSendNewsletter = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!campaign.subject || !campaign.body) {
      showToast("Both subject and body are required.", "error");
      return;
    }
    try {
      setIsSending(true);
      await messagingService.sendNewsletterCampaign(campaign.subject, campaign.body);
      setCampaign({ subject: "", body: "" });
      showToast("Campaign broadcasted successfully.", "success");
    } catch (e) {
      showToast("Failed to send campaign.", "error");
    } finally {
      setIsSending(false);
    }
  };

  const handleUpdateCommentStatus = async (commentId: number, status: Comment["status"]) => {
    try {
      await commentService.updateStatus(commentId, status);
      setComments(current => current.map(c => c.commentId === commentId ? { ...c, status } : c));
      showToast(`Comment marked as ${status.toLowerCase()}.`, "success");
    } catch (e) {
      showToast("Moderation action failed.", "error");
    }
  };

  const handleDeleteComment = async (commentId: number) => {
    try {
      await commentService.moderateDelete(commentId);
      setComments(current => current.filter(c => c.commentId !== commentId));
      showToast("Comment removed from moderation queue.", "success");
    } catch (e) {
      showToast("Deletion failed.", "error");
    }
  };

  const handleDeletePost = async () => {
    if (!postToDelete) return;
    try {
      await postService.delete(postToDelete.postId);
      setPosts(current => current.filter(p => p.postId !== postToDelete.postId));
      showToast(`"${postToDelete.title}" has been permanently deleted.`, "success");
      setPostToDelete(null);
    } catch (e) {
      console.error("Deletion error:", e);
      showToast("Failed to delete the post. You might not have permission or it's already gone.", "error");
      setPostToDelete(null);
    }
  };

  const stats = useMemo(() => ({
    totalPosts: posts.length,
    publishedPosts: posts.filter(p => p.status === "PUBLISHED").length,
    pendingComments: comments.filter(c => c.status === "PENDING").length,
    totalViews: posts.reduce((s, p) => s + p.viewCount, 0),
  }), [posts, comments]);

  const statCards = [
    { label: "Total Posts", value: stats.totalPosts, icon: <FileText className="h-5 w-5" />, grad: "from-blue-500 to-indigo-600" },
    { label: "Published", value: stats.publishedPosts, icon: <BookOpen className="h-5 w-5" />, grad: "from-emerald-500 to-teal-600" },
    { label: "Pending Talk", value: stats.pendingComments, icon: <MessageSquare className="h-5 w-5" />, grad: "from-violet-500 to-indigo-600" },
    { label: "Total Views", value: stats.totalViews, icon: <Eye className="h-5 w-5" />, grad: "from-brand to-pink-500" },
  ];

  return (
    <div className="min-h-screen bg-canvas">
      {/* Page header */}
      <div className="border-b border-border bg-white">
        <div className="mx-auto max-w-6xl px-4 py-8">
          <div className="flex flex-wrap items-center justify-between gap-4">
            <div className="flex items-center gap-4">
              <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-brand to-pink-500 text-white shadow-lg">
                <PenLine className="h-6 w-6" />
              </div>
              <div>
                <p className="text-xs font-black uppercase tracking-widest text-brand">Writer Workspace</p>
                <h1 className="text-2xl font-extrabold tracking-tight text-heading">
                  {currentUser?.fullName}'s Dashboard
                </h1>
              </div>
            </div>
            <Link
              to="/author/posts/new"
              className="inline-flex items-center gap-2 rounded-xl bg-slate-900 px-6 py-3 text-xs font-black uppercase tracking-widest text-white shadow-lg shadow-slate-200 transition hover:bg-brand hover:scale-105"
            >
              <Plus className="h-4 w-4" />
              New Story
            </Link>
          </div>
        </div>
      </div>

      <div className="mx-auto max-w-6xl px-4 py-8 space-y-8">
        {/* Error */}
        {error && (
          <div className="rounded-xl border border-red-200 bg-red-50 p-5 text-sm text-red-600 font-bold uppercase tracking-widest">{error}</div>
        )}

        {/* Stats grid */}
        {isLoading ? (
          <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
            {Array.from({ length: 4 }).map((_, i) => (
              <div key={i} className="h-28 animate-pulse rounded-xl bg-slate-50" />
            ))}
          </div>
        ) : (
          <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
            {statCards.map(card => (
              <div
                key={card.label}
                className="group relative overflow-hidden rounded-[1.5rem] border border-slate-100 bg-white p-6 shadow-sm transition-all duration-300 hover:-translate-y-1 hover:shadow-xl hover:shadow-brand/5 hover:border-brand/20"
              >
                <div className={`absolute right-4 top-4 flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br ${card.grad} text-white shadow-lg transition-transform duration-300 group-hover:scale-110 group-hover:rotate-6`}>
                  {card.icon}
                </div>
                <p className="text-[10px] font-black uppercase tracking-widest text-slate-400 group-hover:text-brand transition-colors">{card.label}</p>
                <p className="mt-2 text-4xl font-black text-heading group-hover:scale-[1.02] transition-transform origin-left">{card.value}</p>
              </div>
            ))}
          </div>
        )}

        {/* Tabs Switcher */}
        <div className="flex gap-2 border-b border-slate-100 pb-px">
          {[
            { id: "posts", label: "Stories", icon: <FileText className="h-4 w-4" /> },
            { id: "analytics", label: "Analytics", icon: <BarChart2 className="h-4 w-4" /> },
            { id: "moderation", label: "Moderation", icon: <MessageSquare className="h-4 w-4" /> },
            { id: "media", label: "Library", icon: <ImageIcon className="h-4 w-4" /> },
            { id: "newsletter", label: "Newsletter", icon: <Mail className="h-4 w-4" /> },
          ].map(tab => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id as any)}
              className={`flex items-center gap-2 px-6 py-4 text-xs font-black uppercase tracking-widest transition-all duration-200 ${
                activeTab === tab.id 
                  ? "border-b-2 border-brand text-brand bg-brand/5" 
                  : "text-slate-400 hover:text-brand hover:bg-slate-50"
              }`}
            >
              {tab.icon}
              {tab.label}
            </button>
          ))}
        </div>

        {/* Dynamic Content */}
        {!isLoading && !error && (
          <div className="animate-in fade-in slide-in-from-bottom-4 duration-500">
            {activeTab === "posts" && (
              <div className="rounded-[1.5rem] border border-slate-100 bg-white shadow-sm overflow-hidden">
                <div className="overflow-x-auto">
                  <table className="min-w-full text-sm">
                    <thead className="bg-slate-50/50">
                      <tr>
                        {["Story", "Status", "Reads", "Likes", "Talk", "Updated", "Actions"].map(col => (
                          <th key={col} className="px-6 py-4 text-left text-[10px] font-black uppercase tracking-widest text-slate-400">{col}</th>
                        ))}
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-50">
                      {posts.map(post => (
                        <tr key={post.postId} className="transition hover:bg-slate-50/50">
                          <td className="px-6 py-4">
                            <Link to={`/author/posts/${post.postId}/edit`} className="font-bold text-heading hover:text-brand transition line-clamp-1">
                              {post.title}
                            </Link>
                          </td>
                          <td className="px-6 py-4">
                            <span className={`inline-flex rounded-lg px-3 py-1 text-[9px] font-black uppercase tracking-widest ${statusBadge[post.status] ?? statusBadge.DRAFT}`}>
                              {post.status}
                            </span>
                          </td>
                          <td className="px-6 py-4 font-bold text-slate-500">{post.viewCount}</td>
                          <td className="px-6 py-4 font-bold text-slate-500">{post.likesCount}</td>
                          <td className="px-6 py-4 font-bold text-slate-500">{post.commentCount ?? 0}</td>
                          <td className="px-6 py-4 text-xs font-medium text-slate-400">
                            {new Date(post.updatedAt).toLocaleDateString("en-IN", { month: "short", day: "numeric" })}
                          </td>
                          <td className="px-6 py-4">
                            <button onClick={() => setPostToDelete(post)} className="rounded-lg p-2 text-slate-400 hover:bg-red-50 hover:text-red-500 transition">
                              <Trash2 className="h-4 w-4" />
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                  {posts.length === 0 && <div className="p-20 text-center text-slate-400 font-bold uppercase tracking-widest">No stories written yet.</div>}
                </div>
              </div>
            )}

            {activeTab === "analytics" && (
              <div className="space-y-8 animate-in fade-in zoom-in-95 duration-500">
                {/* Visual Trends */}
                <div className="grid gap-8 lg:grid-cols-2">
                  {/* Readership Growth */}
                  <div className="rounded-[1.5rem] border border-slate-100 bg-white p-8 shadow-sm">
                    <div className="mb-6 flex items-center justify-between">
                      <div>
                        <h3 className="text-lg font-black text-heading">Readership Trends</h3>
                        <p className="text-[10px] font-black uppercase tracking-widest text-brand">Weekly view volume</p>
                      </div>
                      <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-slate-50 text-slate-400">
                        <TrendingUp className="h-5 w-5" />
                      </div>
                    </div>
                    <div className="w-full">
                      <ResponsiveContainer width="100%" height={300}>
                        <AreaChart data={trendData}>
                          <defs>
                            <linearGradient id="colorViews" x1="0" y1="0" x2="0" y2="1">
                              <stop offset="5%" stopColor="#f43f5e" stopOpacity={0.1}/>
                              <stop offset="95%" stopColor="#f43f5e" stopOpacity={0}/>
                            </linearGradient>
                          </defs>
                          <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                          <XAxis 
                            dataKey="name" 
                            axisLine={false} 
                            tickLine={false} 
                            tick={{fontSize: 10, fontWeight: 900, fill: '#94a3b8'}}
                            dy={10}
                          />
                          <YAxis hide />
                          <Tooltip 
                            contentStyle={{ borderRadius: '16px', border: 'none', boxShadow: '0 20px 25px -5px rgb(0 0 0 / 0.1)', padding: '12px' }}
                            itemStyle={{ fontSize: '10px', fontWeight: 900, textTransform: 'uppercase' }}
                          />
                          <Area type="monotone" dataKey="views" stroke="#f43f5e" strokeWidth={4} fillOpacity={1} fill="url(#colorViews)" />
                        </AreaChart>
                      </ResponsiveContainer>
                    </div>
                  </div>

                  {/* Top Stories Performance */}
                  <div className="rounded-[1.5rem] border border-slate-100 bg-white p-8 shadow-sm">
                    <div className="mb-6 flex items-center justify-between">
                      <div>
                        <h3 className="text-lg font-black text-heading">Top Performing Stories</h3>
                        <p className="text-[10px] font-black uppercase tracking-widest text-brand">Highest reach content</p>
                      </div>
                      <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-slate-50 text-slate-400">
                        <BarChart3 className="h-5 w-5" />
                      </div>
                    </div>
                    <div className="w-full">
                      <ResponsiveContainer width="100%" height={300}>
                        <BarChart data={topPostsData} layout="vertical">
                          <XAxis type="number" hide />
                          <YAxis 
                            dataKey="name" 
                            type="category" 
                            axisLine={false} 
                            tickLine={false} 
                            tick={{fontSize: 9, fontWeight: 900, fill: '#64748b'}}
                            width={100}
                          />
                          <Tooltip 
                            cursor={{fill: 'transparent'}}
                            contentStyle={{ borderRadius: '16px', border: 'none', boxShadow: '0 20px 25px -5px rgb(0 0 0 / 0.1)' }}
                          />
                          <Bar dataKey="views" radius={[0, 8, 8, 0]} barSize={20}>
                            {topPostsData.map((entry, index) => (
                              <Cell key={`cell-${index}`} fill={index === 0 ? '#f43f5e' : '#e2e8f0'} />
                            ))}
                          </Bar>
                        </BarChart>
                      </ResponsiveContainer>
                    </div>
                  </div>
                </div>

                {/* Efficiency Cards */}
                <div className="grid gap-4 md:grid-cols-3">
                   {[
                     { label: "Content Velocity", value: "85%", icon: <Activity className="h-5 w-5" />, trend: "+12%" },
                     { label: "Click-thru Rate", value: "4.2%", icon: <MousePointer2 className="h-5 w-5" />, trend: "+0.8%" },
                     { label: "Appreciation Ratio", value: "1:24", icon: <ThumbsUp className="h-5 w-5" />, trend: "+4%" },
                   ].map((item) => (
                     <div key={item.label} className="rounded-[1.5rem] border border-slate-100 bg-white p-6 shadow-sm flex items-center gap-4">
                        <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-slate-50 text-slate-400">
                          {item.icon}
                        </div>
                        <div>
                           <p className="text-[10px] font-black uppercase tracking-widest text-slate-400">{item.label}</p>
                           <div className="flex items-center gap-3">
                             <p className="text-xl font-black text-heading">{item.value}</p>
                             <span className="text-[10px] font-bold text-emerald-500 bg-emerald-50 px-2 py-0.5 rounded-full">{item.trend}</span>
                           </div>
                        </div>
                     </div>
                   ))}
                </div>
              </div>
            )}

            {activeTab === "moderation" && (
              <div className="space-y-4">
                {comments.length === 0 ? (
                  <div className="rounded-[2.5rem] border-2 border-dashed border-slate-100 p-20 text-center">
                    <MessageSquare className="mx-auto h-12 w-12 text-slate-100" />
                    <p className="mt-4 font-bold text-slate-400 uppercase tracking-widest">No conversation to moderate.</p>
                  </div>
                ) : (
                  <div className="grid gap-4">
                    {comments.map(comment => (
                      <div key={comment.commentId} className="rounded-[1.5rem] border border-slate-100 bg-white p-6 shadow-sm flex flex-wrap items-center justify-between gap-4">
                        <div className="min-w-0 flex-1">
                           <div className="flex items-center gap-2 mb-2">
                             <span className={`rounded-lg px-2 py-0.5 text-[8px] font-black uppercase tracking-tighter ${
                               comment.status === 'APPROVED' ? 'bg-emerald-100 text-emerald-600' : 
                               comment.status === 'PENDING' ? 'bg-violet-100 text-violet-600' : 'bg-red-100 text-red-600'
                             }`}>
                               {comment.status}
                             </span>
                             <span className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Post ID: {comment.postId}</span>
                           </div>
                           <p className="text-sm font-medium text-slate-600 leading-relaxed italic">"{comment.content}"</p>
                        </div>
                        <div className="flex items-center gap-2">
                          {comment.status !== 'APPROVED' && (
                             <button
                               onClick={() => handleUpdateCommentStatus(comment.commentId, 'APPROVED')}
                               className="rounded-xl bg-emerald-50 px-4 py-2 text-[9px] font-black uppercase tracking-widest text-emerald-600 hover:bg-emerald-600 hover:text-white transition"
                             >
                               Approve
                             </button>
                          )}
                          {comment.status !== 'REJECTED' && (
                             <button
                               onClick={() => handleUpdateCommentStatus(comment.commentId, 'REJECTED')}
                               className="rounded-xl bg-violet-50 px-4 py-2 text-[9px] font-black uppercase tracking-widest text-violet-600 hover:bg-violet-600 hover:text-white transition"
                             >
                               Reject
                             </button>
                          )}
                          <button
                            onClick={() => handleDeleteComment(comment.commentId)}
                            className="rounded-xl bg-red-50 px-4 py-2 text-[9px] font-black uppercase tracking-widest text-red-500 hover:bg-red-500 hover:text-white transition"
                          >
                            Delete
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}

            {activeTab === "newsletter" && (
              <div className="rounded-[1.5rem] border border-slate-100 bg-white p-10 shadow-sm">
                <div className="mb-8">
                  <h2 className="text-xl font-black text-heading">Newsletter Campaign</h2>
                  <p className="text-[10px] font-black uppercase tracking-widest text-brand mt-1">Broadcast your thoughts to all subscribers</p>
                </div>
                
                <form onSubmit={handleSendNewsletter} className="space-y-6 max-w-2xl">
                  <div>
                    <label className="mb-2 block text-[10px] font-black uppercase tracking-widest text-slate-400">Campaign Subject</label>
                    <input 
                      required
                      value={campaign.subject}
                      onChange={e => setCampaign({...campaign, subject: e.target.value})}
                      placeholder="e.g. Exclusive Update: New Stories and Insights"
                      className="w-full rounded-2xl border border-slate-100 bg-slate-50/30 p-4 text-sm font-bold text-heading outline-none focus:border-brand focus:ring-4 focus:ring-brand/5 transition"
                    />
                  </div>
                  <div>
                    <label className="mb-2 block text-[10px] font-black uppercase tracking-widest text-slate-400">Newsletter Body</label>
                    <textarea 
                      required
                      value={campaign.body}
                      onChange={e => setCampaign({...campaign, body: e.target.value})}
                      rows={8}
                      placeholder="Share your message with your readers..."
                      className="w-full rounded-2xl border border-slate-100 bg-slate-50/30 p-4 text-sm font-medium text-heading outline-none focus:border-brand focus:ring-4 focus:ring-brand/5 transition resize-none"
                    />
                  </div>
                  
                  <button 
                    disabled={isSending}
                    className="flex items-center gap-2 rounded-2xl bg-slate-900 px-8 py-4 text-[10px] font-black uppercase tracking-widest text-white shadow-lg shadow-slate-100 hover:bg-brand hover:scale-[1.02] active:scale-95 transition disabled:opacity-50"
                  >
                    <Send className="h-4 w-4" />
                    {isSending ? "Broadcasting..." : "Send Campaign"}
                  </button>
                </form>
              </div>
            )}
            {activeTab === "media" && (
              <div className="rounded-[1.5rem] border border-slate-100 bg-white shadow-sm overflow-hidden p-8">
                <div className="mb-6 flex items-center justify-between">
                  <div>
                    <h2 className="text-xl font-black text-heading">Personal Media Library</h2>
                    <p className="text-[10px] font-black uppercase tracking-widest text-brand mt-1">Manage your uploaded assets</p>
                  </div>
                </div>
                <div className="grid gap-6 grid-cols-2 md:grid-cols-4 lg:grid-cols-6">
                  {media.map((file) => (
                    <div key={file.mediaId} className="group relative aspect-square rounded-2xl border border-slate-100 overflow-hidden bg-slate-50 transition hover:border-brand/20">
                      {file.mimeType.startsWith('image/') ? (
                        <button type="button" onClick={() => setSelectedImage(`${BASE_URL}${file.url}`)} className="block h-full w-full outline-none">
                          <img src={`${BASE_URL}${file.url}`} alt={file.altText ?? file.originalName} className="h-full w-full object-cover transition group-hover:scale-110" />
                        </button>
                      ) : (
                        <button type="button" onClick={() => window.open(`${BASE_URL}${file.url}`, '_blank')} className="flex h-full w-full items-center justify-center outline-none">
                          <FileText className="h-8 w-8 text-slate-300" />
                        </button>
                      )}
                      <div className="absolute inset-0 pointer-events-none bg-slate-900/60 opacity-0 group-hover:opacity-100 transition flex flex-col items-center justify-center gap-2 p-4 text-center">
                        <p className="text-[9px] font-black text-white uppercase line-clamp-1">{file.originalName}</p>
                        <div className="flex gap-2 pointer-events-auto">
                          <button 
                            onClick={() => setSelectedImage(`${BASE_URL}${file.url}`)}
                            className="rounded-lg bg-white/20 p-2 text-white hover:bg-white/40 transition"
                            title="View full size"
                          >
                            <Eye className="h-3.5 w-3.5" />
                          </button>
                        </div>
                      </div>
                    </div>
                  ))}
                  {media.length === 0 && (
                    <div className="col-span-full p-20 text-center">
                      <ImageIcon className="mx-auto h-12 w-12 text-slate-100" />
                      <p className="mt-4 font-bold text-slate-400 uppercase tracking-widest">Your library is empty.</p>
                    </div>
                  )}
                </div>
              </div>
            )}
          </div>
        )}
      </div>

      <ConfirmationModal
        isOpen={!!postToDelete}
        title="Delete Post?"
        message={`Are you sure you want to delete "${postToDelete?.title}"? This action cannot be undone and the story will be removed from your profile and search results.`}
        confirmLabel="Delete"
        onConfirm={handleDeletePost}
        onCancel={() => setPostToDelete(null)}
      />

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

export default AuthorDashboard;
