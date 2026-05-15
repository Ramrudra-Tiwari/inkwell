import { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import { User, FileText, Calendar, MessageSquare, Heart, BellPlus, BellOff } from "lucide-react";
import apiClient from "../api/axiosInstance";
import postService from "../services/postService";
import { Post } from "../types/post";
import { useAuth } from "../context/AuthContext";
import PostCard from "../components/PostCard";
import UserAvatar from "../components/UserAvatar";

const AuthorPublicProfile = () => {
  const { userId } = useParams<{ userId: string }>();
  const { currentUser } = useAuth();
  const [author, setAuthor] = useState<any>(null);
  const [posts, setPosts] = useState<Post[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");
  const [followerCount, setFollowerCount] = useState(0);
  const [isFollowing, setIsFollowing] = useState(false);

  const loadFollowData = async () => {
    if (!userId) return;
    try {
      const { data: count } = await apiClient.get<number>(`/auth/user/${userId}/follower-count`);
      setFollowerCount(count);
      
      if (currentUser && currentUser.userId !== Number(userId)) {
        const { data: following } = await apiClient.get<boolean>(`/auth/user/is-following/${userId}`, {
          params: { followerId: currentUser.userId }
        });
        setIsFollowing(following);
      }
    } catch (err) {
      console.error("Follow data error:", err);
    }
  };

  useEffect(() => {
    const loadAuthorProfile = async () => {
      if (!userId) return;
      try {
        setIsLoading(true);
        setError("");
        
        // Fetch public info of author
        const { data: authorData } = await apiClient.get<any>(`/api/v1/users/${userId}`);
        const authorPosts = await postService.getByAuthor(Number(userId));
        
        setAuthor(authorData);
        setPosts(authorPosts.filter(p => p.status === "PUBLISHED"));
        void loadFollowData();
      } catch (err) {
        console.error(err);
        setError("This writer is currently away from their desk.");
      } finally {
        setIsLoading(false);
      }
    };

    void loadAuthorProfile();
  }, [userId, currentUser?.userId]);

  const handleFollowToggle = async () => {
    if (!currentUser || !userId) return;
    try {
      if (isFollowing) {
        await apiClient.delete(`/auth/user/unfollow/${userId}`, {
          params: { followerId: currentUser.userId }
        });
      } else {
        await apiClient.post(`/auth/user/follow/${userId}`, null, {
          params: { followerId: currentUser.userId }
        });
      }
      void loadFollowData();
    } catch (err) {
      console.error("Follow toggle failed:", err);
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-canvas py-20 text-center">
        <div className="mx-auto h-12 w-12 animate-spin rounded-full border-4 border-slate-100 border-t-brand" />
        <p className="mt-4 text-xs font-black uppercase tracking-widest text-muted">Gathering stories...</p>
      </div>
    );
  }

  if (error || !author) {
    return (
      <div className="min-h-screen bg-canvas py-20">
        <div className="mx-auto max-w-xl rounded-[2.5rem] border border-red-100 bg-red-50/50 p-10 text-center">
          <User className="mx-auto h-12 w-12 text-red-200" />
          <h1 className="mt-6 text-2xl font-black text-heading">{error}</h1>
          <Link to="/" className="mt-8 inline-block text-xs font-black uppercase tracking-widest text-brand">Back to Feed</Link>
        </div>
      </div>
    );
  }

  const totalViews = posts.reduce((sum, p) => sum + p.viewCount, 0);
  const totalLikes = posts.reduce((sum, p) => sum + p.likesCount, 0);

  return (
    <div className="min-h-screen bg-canvas pb-20">
      {/* Dynamic Hero Header */}
      <div className="relative bg-slate-900 py-24 overflow-hidden">
        <div className="absolute inset-0 opacity-10">
          <div className="absolute inset-0 bg-[radial-gradient(circle_at_center,_var(--tw-gradient-stops))] from-brand via-transparent to-transparent" />
        </div>
        
        <div className="relative mx-auto max-w-5xl px-4 text-center">
          <UserAvatar
            src={author.avatarUrl}
            alt={author.fullName}
            fallbackText={author.fullName}
            className="mx-auto mb-8 h-24 w-24 rounded-[2.5rem] bg-white text-slate-900 shadow-2xl"
            fallbackClassName="text-4xl text-slate-900"
            iconClassName="h-10 w-10 text-slate-400"
          />
          <h1 className="text-5xl font-black tracking-tight text-white">{author.fullName}</h1>
          <p className="mt-4 text-[10px] font-black uppercase tracking-[0.4em] text-brand">Thought Leader & Author</p>
          
          {author.bio && (
            <p className="mx-auto mt-6 max-w-2xl text-lg text-slate-300 leading-relaxed italic">
              "{author.bio}"
            </p>
          )}

          <div className="mt-10 flex flex-wrap items-center justify-center gap-4">
            {currentUser && currentUser.userId !== author.userId && (
              <button 
                onClick={handleFollowToggle}
                className={`inline-flex items-center gap-3 rounded-2xl px-10 py-4 text-xs font-black uppercase tracking-widest transition shadow-2xl ${
                  isFollowing 
                  ? 'bg-white/10 text-white backdrop-blur-md border border-white/20 hover:bg-white/20' 
                  : 'bg-brand text-white hover:scale-[1.05] active:scale-95 shadow-brand/20'
                }`}
              >
                {isFollowing ? (
                  <>
                    <BellOff className="h-4 w-4" />
                    Unsubscribe
                  </>
                ) : (
                  <>
                    <BellPlus className="h-4 w-4" />
                    Follow Writer
                  </>
                )}
              </button>
            )}
          </div>

          <div className="mt-12 flex flex-wrap items-center justify-center gap-8">
            <div className="text-center">
              <p className="text-3xl font-black text-white">{posts.length}</p>
              <p className="text-[10px] font-black uppercase tracking-widest text-slate-500">Stories</p>
            </div>
            <div className="h-8 w-px bg-slate-800" />
            <div className="text-center">
              <p className="text-3xl font-black text-white">{followerCount.toLocaleString()}</p>
              <p className="text-[10px] font-black uppercase tracking-widest text-slate-500">Readers</p>
            </div>
            <div className="h-8 w-px bg-slate-800" />
            <div className="text-center">
              <p className="text-3xl font-black text-white">{totalViews.toLocaleString()}</p>
              <p className="text-[10px] font-black uppercase tracking-widest text-slate-500">Total Reads</p>
            </div>
            <div className="h-8 w-px bg-slate-800" />
            <div className="text-center">
              <p className="text-3xl font-black text-white">{totalLikes.toLocaleString()}</p>
              <p className="text-[10px] font-black uppercase tracking-widest text-slate-500">Appreciation</p>
            </div>
          </div>
        </div>
      </div>

      {/* Published Works */}
      <div className="mx-auto -mt-12 max-w-6xl px-4">
        <div className="grid gap-8 md:grid-cols-2 lg:grid-cols-3">
          {posts.map((post) => (
            <PostCard key={post.postId} post={post} />
          ))}
        </div>

        {posts.length === 0 && (
          <div className="py-20 text-center">
            <FileText className="mx-auto h-12 w-12 text-slate-200" />
            <p className="mt-4 text-sm font-bold text-slate-400 uppercase tracking-widest">No published stories yet.</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default AuthorPublicProfile;
