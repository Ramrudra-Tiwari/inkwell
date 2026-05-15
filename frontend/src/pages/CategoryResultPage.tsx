import { useEffect, useMemo, useState } from "react";
import { Link, useLocation, useParams } from "react-router-dom";
import PostCard from "../components/PostCard";
import postService from "../services/postService";
import taxonomyService from "../services/taxonomyService";
import { Post } from "../types/post";
import { BreadcrumbItem, Category, Tag, TaxonomyKind } from "../types/taxonomy";

type TaxonomyEntity = Category | Tag;

const isCategory = (entity: TaxonomyEntity, kind: TaxonomyKind): entity is Category => kind === "category";

const inferTaxonomyMatches = (posts: Post[], slug: string, kind: TaxonomyKind) => {
  return posts.filter((post) => {
    const candidate = post as Post & {
      categorySlug?: string;
      category?: { slug?: string };
      categories?: Array<{ slug?: string }>;
      tagSlugs?: string[];
      tags?: Array<{ slug?: string }>;
    };

    if (kind === "category") {
      return (
        candidate.categorySlug === slug ||
        candidate.category?.slug === slug ||
        candidate.categories?.some((category) => category.slug === slug) === true
      );
    }

    return (
      candidate.tagSlugs?.includes(slug) === true ||
      candidate.tags?.some((tag) => tag.slug === slug) === true
    );
  });
};

const buildBreadcrumbs = (
  entity: TaxonomyEntity,
  kind: TaxonomyKind,
  allCategories: Category[]
): BreadcrumbItem[] => {
  const items: BreadcrumbItem[] = [{ label: "Home", to: "/" }];

  if (kind === "tag") {
    return [...items, { label: "Tags", to: "/tag/" + entity.slug }, { label: entity.name }];
  }

  const path: Category[] = [];
  let currentCategory: Category | undefined = entity as Category;

  while (currentCategory) {
    path.unshift(currentCategory);

    if (!currentCategory.parentCategoryId) {
      break;
    }

    currentCategory = allCategories.find(
      (category) => category.categoryId === currentCategory?.parentCategoryId
    );
  }

  path.forEach((category, index) => {
    const isLast = index === path.length - 1;
    items.push({
      label: category.name,
      to: isLast ? undefined : `/category/${category.slug}`
    });
  });

  return items;
};

const CategoryResultPage = () => {
  const { slug = "" } = useParams();
  const location = useLocation();
  const taxonomyKind: TaxonomyKind = location.pathname.startsWith("/tag/") ? "tag" : "category";

  const [entity, setEntity] = useState<TaxonomyEntity | null>(null);
  const [allCategories, setAllCategories] = useState<Category[]>([]);
  const [posts, setPosts] = useState<Post[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");
  const [hasFilterSupport, setHasFilterSupport] = useState(true);

  useEffect(() => {
    const loadTaxonomyData = async () => {
      try {
        setIsLoading(true);
        setError("");

        const requests: Array<Promise<unknown>> = [
          taxonomyService.getBySlug(taxonomyKind, slug),
          postService.getPublished()
        ];

        if (taxonomyKind === "category") {
          requests.push(taxonomyService.getCategories());
        }

        const [entityResponse, postsResponse, categoriesResponse] = await Promise.all(requests);
        const filteredPosts = inferTaxonomyMatches(postsResponse as Post[], slug, taxonomyKind);

        setEntity(entityResponse as TaxonomyEntity);
        setPosts(filteredPosts);
        setAllCategories((categoriesResponse as Category[] | undefined) ?? []);
        setHasFilterSupport(filteredPosts.length > 0 || (postsResponse as Post[]).length === 0);
      } catch (loadError) {
        console.error(loadError);
        setError("Unable to load this discovery page right now.");
      } finally {
        setIsLoading(false);
      }
    };

    void loadTaxonomyData();
  }, [slug, taxonomyKind]);

  const breadcrumbs = useMemo(() => {
    if (!entity) {
      return [{ label: "Home", to: "/" }];
    }

    return buildBreadcrumbs(entity, taxonomyKind, allCategories);
  }, [allCategories, entity, taxonomyKind]);

  if (isLoading) {
    return (
      <section className="mx-auto max-w-6xl px-4 py-12">
        <div className="animate-pulse rounded-[2rem] border border-ink/10 bg-paper p-8 shadow-letterpress">
          <div className="h-4 w-48 rounded bg-ink/10" />
          <div className="mt-5 h-12 w-2/3 rounded-2xl bg-ink/10" />
          <div className="mt-10 grid gap-6 md:grid-cols-2 xl:grid-cols-3">
            {Array.from({ length: 3 }).map((_, index) => (
              <div key={index} className="h-56 rounded-[1.5rem] bg-ink/10" />
            ))}
          </div>
        </div>
      </section>
    );
  }

  if (error || !entity) {
    return (
      <section className="mx-auto max-w-6xl px-4 py-12">
        <div className="rounded-[2rem] border border-wine/20 bg-wine/10 p-8 text-wine shadow-letterpress">
          <p className="text-sm uppercase tracking-[0.22em]">Discovery unavailable</p>
          <p className="mt-3 text-lg">{error || "The requested taxonomy page is unavailable."}</p>
        </div>
      </section>
    );
  }

  return (
    <section className="mx-auto max-w-6xl px-4 py-12">
      <div className="rounded-[2rem] border border-ink/10 bg-paper p-8 shadow-letterpress">
        <nav aria-label="Breadcrumb" className="flex flex-wrap items-center gap-2 text-sm text-ink/55">
          {breadcrumbs.map((item, index) => (
            <span key={`${item.label}-${index}`} className="flex items-center gap-2">
              {item.to ? (
                <Link to={item.to} className="rounded-sm transition hover:text-ink focus:outline-none focus:ring-2 focus:ring-brass">
                  {item.label}
                </Link>
              ) : (
                <span className="text-ink">{item.label}</span>
              )}
              {index < breadcrumbs.length - 1 && <span aria-hidden="true">&gt;</span>}
            </span>
          ))}
        </nav>

        <p className="mt-5 text-xs uppercase tracking-[0.28em] text-brass">
          {taxonomyKind === "category" ? "Category Archive" : "Tag Archive"}
        </p>
        <h1 className="mt-3 font-display text-5xl text-ink">{entity.name}</h1>
        <p className="mt-4 max-w-3xl text-lg leading-8 text-ink/70">
          {isCategory(entity, taxonomyKind)
            ? entity.description || "Browse writing gathered under this category."
            : "Explore stories gathered around this trending tag."}
        </p>
        <p className="mt-4 text-sm text-ink/55">Post count: {entity.postCount}</p>
      </div>

      {!hasFilterSupport && (
        <div className="mt-6 rounded-[1.5rem] border border-brass/20 bg-brass/10 p-5 text-sm leading-7 text-ink/75">
          This page is wired for category and tag discovery, but your current post responses do not yet expose
          category or tag metadata consistently. Once those slugs are included in published post payloads, the
          filtered results will appear automatically.
        </div>
      )}

      <div className="mt-8">
        {posts.length === 0 ? (
          <div className="rounded-[1.5rem] border border-ink/10 bg-parchment/80 p-8 text-ink/65">
            No published posts found for this {taxonomyKind}.
          </div>
        ) : (
          <div className="grid gap-6 md:grid-cols-2 xl:grid-cols-3">
            {posts.map((post) => (
              <PostCard key={post.postId} post={post} />
            ))}
          </div>
        )}
      </div>
    </section>
  );
};

export default CategoryResultPage;
