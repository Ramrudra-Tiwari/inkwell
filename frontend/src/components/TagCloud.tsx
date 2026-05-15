import { memo } from "react";
import { NavLink } from "react-router-dom";
import { Hash } from "lucide-react";
import { Tag } from "../types/taxonomy";

interface TagCloudProps {
  tags: Tag[];
  isLoading?: boolean;
}

const TagCloud = ({ tags, isLoading = false }: TagCloudProps) => {
  return (
    <section
      className="glass-card p-5"
      aria-labelledby="trending-tags-heading"
    >
      <div className="flex items-center gap-2 mb-3">
        <Hash className="h-4 w-4 text-brand" />
        <h2 id="trending-tags-heading" className="text-sm font-bold text-heading">
          Trending Tags
        </h2>
      </div>

      <div className="flex flex-wrap gap-2">
        {isLoading &&
          Array.from({ length: 8 }).map((_, i) => (
            <div key={i} className="h-7 w-20 animate-pulse rounded-full bg-gray-100" />
          ))}

        {!isLoading &&
          tags.map((tag) => (
            <NavLink
              key={tag.tagId}
              to={`/tag/${tag.slug}`}
              aria-label={`${tag.name}, ${tag.postCount} posts`}
              className={({ isActive }) =>
                `inline-flex items-center gap-1 rounded-full border px-3 py-1 text-xs font-medium transition ${
                  isActive
                    ? "border-brand bg-brand text-white"
                    : "border-border bg-canvas text-body hover:border-brand/40 hover:text-brand"
                }`
              }
            >
              <span>#{tag.name}</span>
              <span className="opacity-60">({tag.postCount})</span>
            </NavLink>
          ))}

        {!isLoading && tags.length === 0 && (
          <p className="text-sm text-muted">No trending tags yet.</p>
        )}
      </div>
    </section>
  );
};

export default memo(TagCloud);
