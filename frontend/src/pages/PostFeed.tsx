import { useEffect, useState, useRef, useMemo } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { TrendingUp, Feather, Rss, Sparkles, ArrowRight, BookOpen, Search, Heart } from "lucide-react";
import { isAxiosError } from "axios";
import CategorySidebar from "../components/CategorySidebar";
import NewsletterWidget from "../components/NewsletterWidget";
import PostCard, { readLikedPosts, TrendingCard, writeLikedPosts } from "../components/PostCard";
import TagCloud from "../components/TagCloud";
import authorService from "../services/authorService";
import postService from "../services/postService";
import taxonomyService from "../services/taxonomyService";
import { Post } from "../types/post";
import { Category, Tag } from "../types/taxonomy";
import { BASE_URL } from "../api/axiosInstance";
import UserAvatar from "../components/UserAvatar";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../components/GlobalToastProvider";

const FeedSkeleton = () => (
  <div className="flex flex-col gap-8">
    <div className="h-96 animate-pulse rounded-[2.5rem] bg-gray-100" />
    <div className="grid gap-8 md:grid-cols-2">
      {Array.from({ length: 4 }).map((_, i) => (
        <div key={i} className="h-64 animate-pulse rounded-[2rem] bg-gray-100" />
      ))}
    </div>
  </div>
);

type FeedTab = "latest" | "trending";

const PostFeed = () => {
  const { currentUser, isAuthenticated } = useAuth();
  const { showToast } = useToast();
  const navigate = useNavigate();
  const location = useLocation();
  const [posts, setPosts] = useState<Post[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [tags, setTags] = useState<Tag[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isTaxonomyLoading, setIsTaxonomyLoading] = useState(true);
  const [error, setError] = useState("");
  const [activeTab, setActiveTab] = useState<FeedTab>("latest");
  const [searchKeyword, setSearchKeyword] = useState("");
  const [likingPostId, setLikingPostId] = useState<number | null>(null);
  
  const feedRef = useRef<HTMLElement>(null);
  const featuredPostRef = useRef<HTMLDivElement>(null);

  const scrollToFeed = () => {
    featuredPostRef.current?.scrollIntoView({ behavior: "smooth", block: "start" });
  };

  useEffect(() => {
    const loadPosts = async () => {
      try {
        setIsLoading(true);
        setError("");
        const publishedPosts = await postService.getPublished();
        const visiblePosts = publishedPosts.filter((p) => p.status === "PUBLISHED");
        const enrichedPosts = await Promise.all(
          visiblePosts.map(async (post) => {
            try {
              const author = await authorService.getById(post.authorId);
              return {
                ...post,
                authorName: author.fullName,
                authorAvatarUrl: author.avatarUrl ?? null,
              };
            } catch {
              return post;
            }
          })
        );
        setPosts(enrichedPosts);
      } catch (e) {
        console.error(e);
        setError("Unable to load the public feed right now.");
      } finally {
        setIsLoading(false);
      }
    };
    void loadPosts();
  }, []);

  useEffect(() => {
    const loadTaxonomy = async () => {
      try {
        setIsTaxonomyLoading(true);
        const [cats, fetchedTags] = await Promise.all([
          taxonomyService.getCategories(),
          taxonomyService.getTrending(10),
        ]);
        setCategories(cats);
        setTags(fetchedTags);
      } catch (e) {
        console.error(e);
      } finally {
        setIsTaxonomyLoading(false);
      }
    };
    void loadTaxonomy();
  }, []);

  const sortedPosts = useMemo(() => {
    let filtered = posts;
    if (searchKeyword.trim()) {
      const kw = searchKeyword.toLowerCase();
      filtered = posts.filter(p => 
        p.title.toLowerCase().includes(kw) || 
        (p.excerpt && p.excerpt.toLowerCase().includes(kw)) ||
        p.content.toLowerCase().includes(kw)
      );
    }

    return activeTab === "trending"
      ? [...filtered].sort((a, b) => (b.viewCount ?? 0) - (a.viewCount ?? 0))
      : [...filtered].sort(
          (a, b) =>
            new Date(b.publishedAt ?? b.createdAt ?? 0).getTime() -
            new Date(a.publishedAt ?? a.createdAt ?? 0).getTime()
        );
  }, [posts, activeTab, searchKeyword]);

  const featuredPost = sortedPosts[0];
  const remainingPosts = sortedPosts.slice(1);
  const isFeaturedPostLiked = featuredPost
    ? readLikedPosts(currentUser?.userId).has(featuredPost.postId)
    : false;

  const handleFeaturedLike = async () => {
    if (!featuredPost) return;

    if (!isAuthenticated || !currentUser) {
      showToast("Please log in to like stories.", "info");
      navigate("/login", { state: { from: location } });
      return;
    }

    if (likingPostId === featuredPost.postId) return;

    if (isFeaturedPostLiked) {
      showToast("You already liked this story.", "info");
      return;
    }

    setLikingPostId(featuredPost.postId);

    try {
      const status = await postService.like(featuredPost.postId, currentUser.userId);
      const nextLikedPosts = readLikedPosts(currentUser.userId);
      nextLikedPosts.add(featuredPost.postId);
      writeLikedPosts(currentUser.userId, nextLikedPosts);

      if (status === 201) {
        setPosts((currentPosts) =>
          currentPosts.map((post) =>
            post.postId === featuredPost.postId
              ? { ...post, likesCount: (post.likesCount ?? 0) + 1 }
              : post
          )
        );
        showToast("Story liked.", "success");
      } else {
        setPosts((currentPosts) => [...currentPosts]);
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
          nextLikedPosts.add(featuredPost.postId);
          writeLikedPosts(currentUser.userId, nextLikedPosts);
          setPosts((currentPosts) => [...currentPosts]);
          showToast("You already liked this story.", "info");
          return;
        }

        showToast(message, "error");
      } else {
        showToast("We couldn't update the like right now.", "error");
      }
    } finally {
      setLikingPostId(null);
    }
  };

  return (
    <div className="min-h-screen bg-[#f8fafc] selection:bg-brand/20">
      {/* Immersive Hero Section */}
      <section className="relative overflow-hidden bg-white py-20 md:py-32">
        {/* Animated Background Mesh */}
        <div className="absolute inset-0 z-0">
          <div className="absolute -left-[10%] -top-[10%] h-[50%] w-[50%] rounded-full bg-brand/5 blur-[120px] animate-pulse" />
          <div className="absolute -right-[5%] top-[20%] h-[40%] w-[40%] rounded-full bg-accent/5 blur-[100px]" />
          <div className="absolute bottom-[10%] left-[20%] h-[30%] w-[30%] rounded-full bg-brand/5 blur-[80px]" />
        </div>
        
        <div className="container mx-auto max-w-7xl px-4">
          <div className="flex flex-col items-center gap-12 lg:flex-row">
            {/* Hero Content */}
            <div className="relative z-10 flex-1 space-y-8 text-center lg:text-left">
              <div className="inline-flex items-center gap-2 rounded-full bg-brand/5 px-4 py-2 text-xs font-black uppercase tracking-[0.2em] text-brand">
                <Sparkles className="h-3.5 w-3.5" />
                Next Generation Publishing
              </div>
              
              <h1 className="font-sans text-5xl font-[900] leading-[1] text-heading md:text-8xl tracking-tight">
                Where great <br />
                <span className="text-transparent bg-clip-text bg-gradient-to-r from-brand via-accent to-brand bg-[length:200%_auto] animate-gradient">stories</span> live.
              </h1>
              
              <p className="mx-auto max-w-xl text-lg leading-relaxed text-muted lg:mx-0">
                InkWell is the world's most premium platform for independent writers. 
                Discover deep insights, expert analysis, and beautiful storytelling 
                from minds that matter.
              </p>

              <div className="flex flex-wrap items-center justify-center gap-4 lg:justify-start">
                <button 
                  onClick={scrollToFeed}
                  className="group flex items-center gap-2 rounded-2xl bg-brand px-8 py-4 text-sm font-black uppercase tracking-widest text-white shadow-xl shadow-brand/20 transition-all hover:bg-brand-hover hover:scale-105 active:scale-95"
                >
                  Start Reading <ArrowRight className="h-4 w-4 transition-transform group-hover:translate-x-1" />
                </button>
                <div className="flex -space-x-3 overflow-hidden">
                  {[1, 2, 3, 4].map((i) => (
                    <div key={i} className="inline-block h-10 w-10 rounded-full border-2 border-white bg-gray-100 shadow-sm">
                      <img src={`https://i.pravatar.cc/100?u=writer${i}`} alt="Author" className="h-full w-full rounded-full object-cover" />
                    </div>
                  ))}
                  <div className="flex h-10 w-10 items-center justify-center rounded-full border-2 border-white bg-brand/10 text-[10px] font-black text-brand">
                    +2k
                  </div>
                </div>
              </div>
            </div>

            {/* Hero Image Container */}
            <div className="relative flex-1">
              <div className="relative mx-auto max-w-[500px]">
                {/* Decorative blobs */}
                <div className="absolute -left-12 -top-12 h-64 w-64 rounded-full bg-accent/20 blur-3xl animate-pulse" />
                <div className="absolute -bottom-12 -right-12 h-64 w-64 rounded-full bg-brand/20 blur-3xl" />
                
                {/* Hero visual */}
                <div className="relative overflow-hidden rounded-[3rem] border border-white/80 bg-white/40 p-5 shadow-[0_32px_64px_-12px_rgba(0,0,0,0.12)] backdrop-blur-xl">
                  <div className="relative overflow-hidden rounded-[2.5rem]">
                    {featuredPost?.featuredImageUrl ? (
                      <img
                        src={featuredPost.featuredImageUrl.startsWith("http") ? featuredPost.featuredImageUrl : `${BASE_URL}${featuredPost.featuredImageUrl}`}
                        alt={featuredPost.title}
                        className="h-[520px] w-full object-cover shadow-inner transition-transform duration-1000 group-hover:scale-110"
                      />
                    ) : (
                      <div className="flex h-[520px] w-full items-center justify-center bg-gradient-to-br from-slate-900 via-brand/80 to-cyan-500 shadow-inner">
                        <BookOpen className="h-24 w-24 text-white/25" />
                      </div>
                    )}
                    <div className="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-transparent opacity-60" />
                  </div>
                  <button
                    type="button"
                    onClick={scrollToFeed}
                    className="absolute bottom-8 left-8 right-8 rounded-[2rem] bg-white/80 p-8 text-left shadow-[0_20px_50px_rgba(0,0,0,0.1)] backdrop-blur-xl border border-white/40 transform transition-all duration-500 hover:-translate-y-2 hover:shadow-brand/20"
                  >
                    <div className="flex items-center gap-2 mb-3">
                      <div className="h-1.5 w-1.5 rounded-full bg-brand animate-pulse" />
                      <p className="text-[10px] font-black uppercase tracking-[0.4em] text-brand">Weekly Spotlight</p>
                    </div>
                    <p className="text-xl font-black text-heading leading-tight tracking-tight">
                      {featuredPost?.title ?? "Mastering the Art of Narrative Resonance"}
                    </p>
                    <p className="mt-2 text-xs font-medium text-slate-500 line-clamp-2">
                      {featuredPost?.excerpt ?? "How elite authors build emotional connection through structural harmony and rhythmic prose."}
                    </p>
                    <div className="mt-6 flex items-center justify-between">
                       <div className="flex items-center gap-2">
                          <div className="h-px w-12 bg-brand/30" />
                          <span className="text-[10px] font-black text-brand uppercase tracking-widest">Read Analysis</span>
                       </div>
                       <ArrowRight className="h-4 w-4 text-brand" />
                    </div>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Main Feed Content */}
      <main ref={feedRef} className="container mx-auto max-w-7xl px-4 py-16 scroll-mt-10">
        <div className="flex flex-col gap-12 lg:flex-row">
          {/* Feed Column */}
          <div className="flex-1 space-y-12">
            {/* Feed Navigation */}
            <div className="flex flex-col gap-6 sm:flex-row sm:items-center sm:justify-between border-b border-slate-200 pb-6">
              <div className="flex items-center gap-3 rounded-full border border-slate-200/80 bg-white/80 p-1 shadow-sm dark:border-slate-700 dark:bg-slate-900/80">
                {(["latest", "trending"] as FeedTab[]).map((tab) => (
                  <button
                    key={tab}
                    onClick={() => setActiveTab(tab)}
                    className={`rounded-full px-4 py-2 text-sm font-black uppercase tracking-widest transition-all ${
                      activeTab === tab
                        ? "bg-brand/10 text-brand shadow-sm shadow-brand/10"
                        : "text-slate-500 hover:bg-slate-100/80 hover:text-heading dark:text-slate-300 dark:hover:bg-slate-800"
                    }`}
                  >
                    {tab}
                  </button>
                ))}
              </div>

              <div className="relative flex-1 max-w-sm">
                <Search className="absolute left-4 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-300" />
                <input 
                  type="text"
                  placeholder="Search stories..."
                  value={searchKeyword}
                  onChange={(e) => setSearchKeyword(e.target.value)}
                  className="w-full rounded-2xl border border-slate-100 bg-white px-12 py-3 text-sm font-medium text-heading outline-none transition focus:border-brand focus:ring-4 focus:ring-brand/5"
                />
              </div>
            </div>

            {/* Posts Grid */}
            {isLoading ? (
              <FeedSkeleton />
            ) : error ? (
              <div className="rounded-[2.5rem] border-2 border-dashed border-red-200 bg-red-50 p-12 text-center">
                <p className="text-red-600 font-bold">{error}</p>
              </div>
            ) : sortedPosts.length === 0 ? (
              <div className="rounded-[2.5rem] bg-white p-20 text-center shadow-sm">
                <Feather className="mx-auto h-12 w-12 text-brand/20" />
                <h3 className="mt-4 text-xl font-black text-heading">No stories yet.</h3>
                <p className="mt-2 text-muted">Check back later or explore different categories.</p>
              </div>
            ) : (
              <div className="space-y-16">
                {/* Featured Spotlight (only on latest) */}
                {activeTab === "latest" && featuredPost && (
                  <div ref={featuredPostRef} className="group relative overflow-hidden rounded-[2.5rem] bg-white shadow-2xl transition-all duration-500 hover:shadow-brand/10 scroll-mt-28">
                    <div className="flex flex-col lg:flex-row">
                      <div className="h-64 w-full overflow-hidden lg:h-auto lg:w-1/2">
                        {featuredPost.featuredImageUrl ? (
                          <img 
                            src={featuredPost.featuredImageUrl.startsWith('http') ? featuredPost.featuredImageUrl : `${BASE_URL}${featuredPost.featuredImageUrl}`}
                            className="h-full w-full object-cover transition-transform duration-700 group-hover:scale-105" 
                            alt={featuredPost.title}
                          />
                        ) : (
                          <div className="h-full w-full bg-gradient-brand flex items-center justify-center">
                            <BookOpen className="h-20 w-20 text-white/20" />
                          </div>
                        )}
                      </div>
                      <div className="flex flex-1 flex-col justify-center p-8 lg:p-12">
                        <div className="mb-4 flex items-center gap-2">
                          <span className="rounded-full bg-brand/5 px-3 py-1 text-[10px] font-black uppercase tracking-widest text-brand">
                            {featuredPost.categorySlug || "Featured Story"}
                          </span>
                        </div>
                        <h2 className="text-3xl font-black text-heading md:text-4xl">
                          <Link to={`/blog/${featuredPost.slug}`} className="hover:text-brand transition-colors">
                            {featuredPost.title}
                          </Link>
                        </h2>
                        <p className="mt-6 text-lg text-muted line-clamp-3">
                          {featuredPost.excerpt}
                        </p>
                        <div className="mt-8 flex items-center justify-between">
                          <div className="flex items-center gap-3">
                          <UserAvatar
                            src={featuredPost.authorAvatarUrl}
                            alt={featuredPost.authorName || `Writer #${featuredPost.authorId}`}
                            fallbackText={featuredPost.authorName || `Writer #${featuredPost.authorId}`}
                            className="h-10 w-10 rounded-full border border-slate-200 bg-slate-100"
                            fallbackClassName="text-sm text-heading"
                            iconClassName="h-4 w-4 text-slate-400"
                          />
                          <div>
                              <p className="text-[10px] font-black uppercase tracking-widest text-heading">{featuredPost.authorName || `Writer #${featuredPost.authorId}`}</p>
                              <p className="text-[10px] font-bold text-subtle uppercase tracking-widest">{featuredPost.readTimeMin} min read</p>
                            </div>
                          </div>
                          <div className="flex items-center gap-3">
                            <button
                              type="button"
                              onClick={handleFeaturedLike}
                              disabled={likingPostId === featuredPost.postId}
                              className={`flex h-12 items-center gap-2 rounded-full px-4 text-xs font-black uppercase tracking-widest transition-all ${
                                isFeaturedPostLiked
                                  ? "bg-emerald-500 text-white"
                                  : "bg-slate-100 text-slate-600 hover:bg-emerald-500 hover:text-white"
                              } ${likingPostId === featuredPost.postId ? "opacity-70" : ""}`}
                              aria-pressed={isFeaturedPostLiked}
                              aria-label={isFeaturedPostLiked ? "Story already liked" : "Like story"}
                            >
                              <Heart className={`h-4 w-4 ${isFeaturedPostLiked ? "fill-white" : ""}`} />
                              {featuredPost.likesCount ?? 0}
                            </button>
                            <Link 
                              to={`/blog/${featuredPost.slug}`}
                              className="flex h-12 w-12 items-center justify-center rounded-full bg-slate-900 text-white transition-all hover:bg-brand hover:scale-110"
                            >
                              <ArrowRight className="h-5 w-5" />
                            </Link>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                )}

                {/* Grid for other posts */}
                <div className="grid gap-8 md:grid-cols-2">
                  {(activeTab === "latest" ? remainingPosts : sortedPosts).map((post) => (
                    activeTab === "trending" ? (
                      <TrendingCard key={post.postId} post={post} />
                    ) : (
                      <PostCard key={post.postId} post={post} />
                    )
                  ))}
                </div>
              </div>
            )}
          </div>

          {/* Right Sidebar */}
          <aside className="w-full space-y-8 lg:w-80">
            <div className="sticky top-24 space-y-8">
              <CategorySidebar categories={categories} isLoading={isTaxonomyLoading} />
              <TagCloud tags={tags} isLoading={isTaxonomyLoading} />
              <NewsletterWidget />
            </div>
          </aside>
        </div>
      </main>
    </div>
  );
};

export default PostFeed;

