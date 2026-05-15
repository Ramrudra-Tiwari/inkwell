import DOMPurify from "dompurify";
import { useEffect, useMemo, useState } from "react";
import { Link, useParams } from "react-router-dom";
import {
  ArrowLeft, Clock, Eye, Heart, BookOpen, User,
} from "lucide-react";
import CheckoutButton from "../components/CheckoutButton";
import CommentSection from "../components/CommentSection";
import authorService from "../services/authorService";
import postService from "../services/postService";
import { Post } from "../types/post";
import { BASE_URL } from "../api/axiosInstance";

const getCoverGradient = (id: number) => {
  const gradients = [
    "from-violet-400 to-purple-600",
    "from-sky-400 to-blue-600",
    "from-emerald-400 to-teal-600",
    "from-violet-400 to-indigo-600",
    "from-rose-400 to-pink-600",
    "from-indigo-400 to-slate-600",
  ];
  return gradients[id % gradients.length];
};

const PostDetail = () => {
  const { slug } = useParams();
  const [post, setPost] = useState<Post | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");
  const [authorName, setAuthorName] = useState("");

  useEffect(() => {
    const loadPost = async () => {
      if (!slug) { setError("Missing post slug."); setIsLoading(false); return; }
      try {
        setIsLoading(true);
        setError("");
        const foundPost = await postService.getBySlug(slug);
        setPost(foundPost);
        setAuthorName(foundPost.authorName ?? "");
        void authorService
          .getDisplayName(foundPost.authorId)
          .then(setAuthorName)
          .catch(() => {
            setAuthorName(foundPost.authorName ?? `Writer #${foundPost.authorId}`);
          });
        // Requirement 2.6: Atomic increment of view count on every full-page post load
        void postService.incrementViewCount(foundPost.postId);
      } catch (e) {
        console.error(e);
        setError("We couldn't find that article.");
      } finally {
        setIsLoading(false);
      }
    };
    void loadPost();
  }, [slug]);

  const sanitizedContent = useMemo(() => {
    if (!post?.content) return "";
    return DOMPurify.sanitize(post.content);
  }, [post?.content]);

  if (isLoading) {
    return (
      <div className="min-h-screen bg-canvas">
        <div className="mx-auto max-w-3xl px-4 py-10 space-y-4">
          <div className="h-64 animate-pulse rounded-2xl bg-gray-200" />
          <div className="h-10 w-3/4 animate-pulse rounded-xl bg-gray-200" />
          <div className="space-y-3">
            {Array.from({ length: 6 }).map((_, i) => (
              <div key={i} className="h-4 animate-pulse rounded bg-gray-100" />
            ))}
          </div>
        </div>
      </div>
    );
  }

  if (error || !post) {
    return (
      <div className="min-h-screen bg-canvas flex items-center justify-center px-4">
        <div className="max-w-md w-full rounded-2xl border border-red-200 bg-red-50 p-8 text-center">
          <p className="text-sm font-semibold uppercase tracking-widest text-red-500 mb-2">
            Article Unavailable
          </p>
          <p className="text-base text-red-700 mb-6">{error || "The requested article is unavailable."}</p>
          <Link
            to="/"
            className="inline-flex items-center gap-2 rounded-xl bg-brand px-5 py-2.5 text-sm font-bold text-white transition hover:bg-brand-hover"
          >
            <ArrowLeft className="h-4 w-4" /> Back to Feed
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-canvas">
      <div className="mx-auto max-w-3xl px-4 py-8">
        {/* Back link */}
        <Link
          to="/"
          className="inline-flex items-center gap-1.5 text-sm font-medium text-muted transition hover:text-heading mb-6"
        >
          <ArrowLeft className="h-4 w-4" />
          Back to Feed
        </Link>

        <article className="rounded-2xl border border-border bg-white shadow-card overflow-hidden">
          {/* Hero image or gradient */}
          {post.featuredImageUrl ? (
            <img
              src={`${BASE_URL}${post.featuredImageUrl}`}
              alt={post.title}
              className="w-full object-cover"
              style={{ maxHeight: "360px" }}
              onError={(e) => {
                const el = e.currentTarget as HTMLImageElement;
                el.style.display = "none";
              }}
            />
          ) : (
            <div className={`h-48 w-full bg-gradient-to-br ${getCoverGradient(post.postId)} flex items-center justify-center`}>
              <BookOpen className="h-14 w-14 text-white/30" />
            </div>
          )}

          {/* Article header */}
          <div className="px-7 pt-7 pb-5 border-b border-border">
            {/* Meta row */}
            <div className="flex flex-wrap items-center gap-3 mb-4">
              <Link 
                to={`/author/${post.authorId}`}
                className="group flex items-center gap-2 transition hover:text-brand"
              >
                <div className="flex h-8 w-8 items-center justify-center rounded-full bg-brand/10 text-brand text-sm font-bold group-hover:bg-brand group-hover:text-white transition">
                  <User className="h-4 w-4" />
                </div>
                <span className="text-sm font-black text-heading">{authorName || post.authorName || `Writer #${post.authorId}`}</span>
              </Link>
              <span className="text-subtle">·</span>
              <span className="flex items-center gap-1 text-xs text-subtle">
                <Clock className="h-3.5 w-3.5" />
                {post.readTimeMin} min read
              </span>
              <span className="text-subtle">·</span>
              <span className="flex items-center gap-1 text-xs text-subtle">
                <Eye className="h-3.5 w-3.5" />
                {post.viewCount} views
              </span>
              <span className="flex items-center gap-1 text-xs text-subtle">
                <Heart className="h-3.5 w-3.5" />
                {post.likesCount}
              </span>
            </div>

            {/* Title */}
            <h1 className="text-3xl font-extrabold tracking-tight text-heading leading-snug mb-3">
              {post.title}
            </h1>

            {/* Excerpt */}
            {post.excerpt && (
              <p className="text-base leading-relaxed text-muted mb-4">{post.excerpt}</p>
            )}

            <div className="flex flex-wrap items-center justify-between gap-4">
              <p className="text-xs text-subtle">
                Published{" "}
                {post.publishedAt
                  ? new Date(post.publishedAt).toLocaleDateString("en-IN", {
                      day: "numeric", month: "long", year: "numeric",
                    })
                  : "recently"}
              </p>
              <CheckoutButton
                compact
                authorId={post.authorId}
                authorName={authorName || post.authorName}
                postId={post.postId}
                postTitle={post.title}
              />
            </div>
          </div>

          {/* Article body */}
          <div className="px-7 py-8">
            <div
              className="article-content"
              dangerouslySetInnerHTML={{ __html: sanitizedContent }}
            />
          </div>
        </article>

        {/* Comments */}
        <div className="mt-8">
          <CommentSection postId={post.postId} />
        </div>
      </div>

    </div>
  );
};

export default PostDetail;
