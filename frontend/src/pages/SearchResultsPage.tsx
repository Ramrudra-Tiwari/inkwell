import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import PostCard from "../components/PostCard";
import postService from "../services/postService";
import { Post } from "../types/post";

const SearchResultsPage = () => {
  const [searchParams] = useSearchParams();
  const keyword = searchParams.get("keyword")?.trim() ?? "";
  const [results, setResults] = useState<Post[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    const loadResults = async () => {
      if (!keyword) {
        setResults([]);
        return;
      }

      try {
        setIsLoading(true);
        setError("");
        const posts = await postService.search(keyword);
        setResults(posts);
      } catch (loadError) {
        console.error(loadError);
        setError("Unable to search the platform right now.");
      } finally {
        setIsLoading(false);
      }
    };

    void loadResults();
  }, [keyword]);

  return (
    <section className="mx-auto max-w-6xl px-4 py-12">
      <div className="rounded-[2rem] border border-ink/10 bg-paper p-8 shadow-letterpress">
        <p className="text-xs uppercase tracking-[0.24em] text-brass">Gateway Search</p>
        <h1 className="mt-3 font-display text-5xl text-ink">Search results</h1>
        <p className="mt-4 text-lg text-ink/70">
          {keyword ? `Showing results for “${keyword}”.` : "Enter a keyword from the navigation bar to search posts."}
        </p>
      </div>

      {isLoading && (
        <div className="mt-8 grid gap-6 md:grid-cols-2 xl:grid-cols-3">
          {Array.from({ length: 6 }).map((_, index) => (
            <div key={index} className="h-48 animate-pulse rounded-[1.75rem] bg-ink/10" />
          ))}
        </div>
      )}

      {!isLoading && error && (
        <div className="mt-8 rounded-[1.75rem] border border-wine/20 bg-wine/10 p-6 text-wine">
          {error}
        </div>
      )}

      {!isLoading && !error && keyword && (
        <div className="mt-8">
          {results.length === 0 ? (
            <div className="rounded-[1.75rem] border border-ink/10 bg-parchment/80 p-8 text-ink/65">
              No posts matched this search.
            </div>
          ) : (
            <div className="grid gap-6 md:grid-cols-2 xl:grid-cols-3">
              {results.map((post) => (
                <PostCard key={post.postId} post={post} />
              ))}
            </div>
          )}
        </div>
      )}
    </section>
  );
};

export default SearchResultsPage;
