import { BookOpen, Heart, Eye, ArrowRight, Clock } from "lucide-react";
import { useEffect, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { isAxiosError } from "axios";
import authorService from "../services/authorService";
import { useToast } from "./GlobalToastProvider";
import { useAuth } from "../context/AuthContext";
import postService from "../services/postService";
import { Post } from "../types/post";
import { BASE_URL } from "../api/axiosInstance";
import UserAvatar from "./UserAvatar";

interface PostCardProps {
  post: Post;
  rank?: number; // when shown in trending view
}

const formatDate = (date?: string | null) => {
  if (!date) return "Just now";
  return new Intl.DateTimeFormat("en-IN", {
    day: "numeric",
    month: "short",
    year: "numeric",
  }).format(new Date(date));
};

const gradients = [
  "from-indigo-500 via-purple-500 to-pink-500",
  "from-blue-600 via-sky-500 to-cyan-400",
  "from-emerald-500 via-teal-500 to-cyan-500",
  "from-violet-500 via-indigo-500 to-cyan-400",
  "from-rose-500 via-red-500 to-orange-500",
  "from-slate-700 via-slate-600 to-slate-500",
];

const getCoverGradient = (id: number) => gradients[id % gradients.length];

export const getLikedPostsStorageKey = (userId?: number) =>
  userId ? `inkwell-liked-posts-${userId}` : "";

export const readLikedPosts = (userId?: number) => {
  if (!userId) return new Set<number>();

  try {
    const rawValue = localStorage.getItem(getLikedPostsStorageKey(userId));
    if (!rawValue) return new Set<number>();
    const parsed = JSON.parse(rawValue) as number[];
    return new Set(Array.isArray(parsed) ? parsed : []);
  } catch {
    return new Set<number>();
  }
};

export const writeLikedPosts = (userId: number, likedPosts: Set<number>) => {
  localStorage.setItem(getLikedPostsStorageKey(userId), JSON.stringify(Array.from(likedPosts)));
};

/** Trending row — horizontal compact format with rank number */
export const TrendingCard = ({ post, rank }: PostCardProps) => (
  <Link
    to={`/blog/${post.slug}`}
    className="group flex items-center gap-6 rounded-[2rem] bg-white p-5 shadow-sm transition-all duration-500 hover:shadow-2xl hover:shadow-brand/5 hover:-translate-y-1 border border-slate-100"
  >
    {/* Rank */}
    <div className="relative shrink-0">
      <div className={`flex h-14 w-14 items-center justify-center rounded-2xl bg-gradient-to-br ${getCoverGradient(post.postId)} text-white shadow-lg`}>
        <span className="text-2xl font-black italic opacity-90">{rank}</span>
      </div>
    </div>

    {/* Content */}
    <div className="flex-1 min-w-0">
      <div className="flex items-center gap-2 mb-1">
         <span className="text-[10px] font-black uppercase tracking-[0.2em] text-brand/60">{post.categorySlug || "Story"}</span>
      </div>
      <h2 className="text-base font-black text-heading line-clamp-1 leading-tight group-hover:text-brand transition-colors">
        {post.title}
      </h2>
      <div className="mt-2 flex items-center gap-4 text-[10px] font-black uppercase tracking-widest text-subtle">
        <span className="flex items-center gap-1">
          <Eye className="h-3 w-3 text-brand" /> {post.viewCount}
        </span>
        <span className="flex items-center gap-1">
          <Clock className="h-3 w-3 text-brand" /> {post.readTimeMin}m
        </span>
        <span>{formatDate(post.publishedAt ?? post.createdAt)}</span>
      </div>
    </div>

    <div className="shrink-0 rounded-full bg-slate-50 p-2.5 opacity-0 transition-all duration-300 group-hover:translate-x-1 group-hover:opacity-100 group-hover:bg-brand group-hover:text-white">
      <ArrowRight className="h-4 w-4" />
    </div>
  </Link>
);

/** Standard card for Latest grid */
const PostCard = ({ post }: PostCardProps) => {
  const { currentUser, isAuthenticated } = useAuth();
  const { showToast } = useToast();
  const navigate = useNavigate();
  const location = useLocation();
  const [authorName, setAuthorName] = useState(post.authorName ?? `Writer #${post.authorId}`);
  const [authorAvatarUrl, setAuthorAvatarUrl] = useState(post.authorAvatarUrl ?? null);
  const [likesCount, setLikesCount] = useState(post.likesCount ?? 0);
  const [isLiking, setIsLiking] = useState(false);
  const [isLiked, setIsLiked] = useState(() => readLikedPosts(currentUser?.userId).has(post.postId));

  useEffect(() => {
    let isMounted = true;

    setAuthorName(post.authorName ?? `Writer #${post.authorId}`);
    setAuthorAvatarUrl(post.authorAvatarUrl ?? null);

    if (post.authorName && post.authorAvatarUrl !== undefined) {
      return () => {
        isMounted = false;
      };
    }

    void authorService
      .getById(post.authorId)
      .then((author) => {
        if (isMounted) {
          setAuthorName(author.fullName);
          setAuthorAvatarUrl(author.avatarUrl ?? null);
        }
      })
      .catch(() => {
        if (isMounted) {
          setAuthorName(`Writer #${post.authorId}`);
          setAuthorAvatarUrl(null);
        }
      });

    return () => {
      isMounted = false;
    };
  }, [post.authorAvatarUrl, post.authorId, post.authorName]);

  useEffect(() => {
    setLikesCount(post.likesCount ?? 0);
  }, [post.likesCount]);

  useEffect(() => {
    setIsLiked(readLikedPosts(currentUser?.userId).has(post.postId));
  }, [currentUser?.userId, post.postId]);

  const handleLike = async () => {
    if (!isAuthenticated || !currentUser) {
      showToast("Please log in to like stories.", "info");
      navigate("/login", { state: { from: location } });
      return;
    }

    if (isLiking) {
      return;
    }

    if (isLiked) {
      showToast("You already liked this story.", "info");
      return;
    }

    setIsLiking(true);

    try {
      const status = await postService.like(post.postId, currentUser.userId);
      const nextLikedPosts = readLikedPosts(currentUser.userId);
      nextLikedPosts.add(post.postId);
      writeLikedPosts(currentUser.userId, nextLikedPosts);
      setIsLiked(true);

      if (status === 201) {
        setLikesCount((current) => current + 1);
        showToast("Story liked.", "success");
      } else {
        setIsLiked(true);
        showToast("You already liked this story.", "info");
      }
    } catch (error) {
      if (isAxiosError(error)) {
        const responseData = error.response?.data as { message?: string } | string | undefined;
        const message =
          typeof responseData === "string"
            ? responseData
            : responseData?.message ?? "We couldn't update the like right now.";

        if (message.toLowerCase().includes("already liked")) {
          const nextLikedPosts = readLikedPosts(currentUser.userId);
          nextLikedPosts.add(post.postId);
          writeLikedPosts(currentUser.userId, nextLikedPosts);
          setIsLiked(true);
          showToast("You already liked this story.", "info");
          return;
        }

        setIsLiked(false);
        showToast(message, "error");
      } else {
        setIsLiked(false);
        showToast("We couldn't update the like right now.", "error");
      }
    } finally {
      setIsLiking(false);
    }
  };

  return (
    <article className="group flex flex-col bg-white rounded-[2.5rem] border border-slate-100 overflow-hidden transition-all duration-700 hover:shadow-[0_32px_64px_-12px_rgba(0,0,0,0.08)] hover:-translate-y-2 hover:border-brand/30 hover:bg-brand/[0.01]">
      {/* Cover image or gradient */}
      <div className="relative h-72 w-full overflow-hidden">
        {post.featuredImageUrl ? (
          <img
            src={post.featuredImageUrl.startsWith('http') ? post.featuredImageUrl : `${BASE_URL}${post.featuredImageUrl}`}
            alt={post.title}
            className="h-full w-full object-cover transition-transform duration-1000 group-hover:scale-110"
            onError={(e) => {
              const el = e.currentTarget as HTMLImageElement;
              const div = document.createElement("div");
              div.className = `h-full w-full bg-gradient-to-br ${getCoverGradient(post.postId)} flex items-center justify-center`;
              div.innerHTML = `<svg xmlns="http://www.w3.org/2000/svg" class="h-16 w-16 text-white/30" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" /></svg>`;
              el.parentNode?.replaceChild(div, el);
            }}
          />
        ) : (
          <div className={`h-full w-full bg-gradient-to-br ${getCoverGradient(post.postId)} flex items-center justify-center`}>
            <BookOpen className="h-24 w-24 text-white/20" />
          </div>
        )}
        
        {/* Category Badge */}
        {post.categorySlug && (
          <div className="absolute left-8 top-8">
            <span className="rounded-full bg-white/90 backdrop-blur-md px-4 py-2 text-[10px] font-black uppercase tracking-[0.2em] text-brand shadow-lg border border-white/50">
              {post.categorySlug}
            </span>
          </div>
        )}

        {/* Read Time Overlay */}
        <div className="absolute bottom-8 right-8">
          <div className="flex items-center gap-1.5 rounded-full bg-black/40 backdrop-blur-md px-3 py-1.5 text-[10px] font-bold text-white border border-white/10">
            <Clock className="h-3 w-3" /> {post.readTimeMin} min read
          </div>
        </div>
      </div>

      {/* Card body */}
      <div className="flex flex-1 flex-col p-10">
        {/* Author row */}
        <div className="mb-6 flex items-center gap-4">
          <UserAvatar
            src={authorAvatarUrl}
            alt={authorName}
            fallbackText={authorName}
            className="h-11 w-11 shrink-0 rounded-2xl border border-slate-100 bg-slate-50"
            fallbackClassName="text-sm text-heading"
            iconClassName="h-5 w-5 text-brand"
          />
          <div className="flex flex-col">
            <span className="text-[11px] font-black text-heading uppercase tracking-widest">{authorName}</span>
            <span className="text-[10px] font-bold text-subtle uppercase tracking-[0.1em]">
              {formatDate(post.publishedAt ?? post.createdAt)}
            </span>
          </div>
          <div className="ml-auto flex items-center gap-3">
             <div className="flex flex-col items-end">
                <span className="text-[10px] font-black text-brand uppercase">{post.viewCount}</span>
                <span className="text-[8px] font-bold text-subtle uppercase">Views</span>
             </div>
          </div>
        </div>

        {/* Title */}
        <h2 className="mb-4 font-sans text-2xl font-black leading-[1.3] text-heading transition-colors group-hover:text-brand line-clamp-2">
          <Link to={`/blog/${post.slug}`}>
            {post.title}
          </Link>
        </h2>

        {/* Excerpt */}
        <p className="mb-8 flex-1 text-base leading-relaxed text-slate-500 line-clamp-3">
          {post.excerpt || "No summary provided. Tap to read the full story and dive deeper into this perspective."}
        </p>

        {/* Footer */}
        <div className="flex items-center justify-between border-t border-slate-50 pt-8">
          <div className="flex items-center gap-4">
             <button
                type="button"
                onClick={handleLike}
                disabled={isLiking}
                className={`flex items-center gap-1.5 text-xs font-black uppercase tracking-widest transition ${
                  isLiked
                    ? "text-emerald-500"
                    : "text-slate-500 hover:text-emerald-500"
                } ${isLiking ? "opacity-70" : ""}`}
                aria-pressed={isLiked}
                aria-label={isLiked ? "Story already liked" : "Like story"}
             >
                <Heart className={`h-4 w-4 ${isLiked ? "fill-emerald-500 text-emerald-500" : ""}`} />
                {likesCount}
              </button>
          </div>
          
          <Link
            to={`/blog/${post.slug}`}
            className="group/btn flex items-center gap-2 text-xs font-black uppercase tracking-widest text-heading hover:text-brand transition-colors"
          >
            Read Story <ArrowRight className="h-4 w-4 transition-transform group-hover/btn:translate-x-1" />
          </Link>
        </div>
      </div>
    </article>
  );
};

export default PostCard;
