import { memo, useMemo, useState } from "react";
import { NavLink } from "react-router-dom";
import { ChevronRight, LayoutGrid } from "lucide-react";
import { Category, CategoryTreeNode } from "../types/taxonomy";

interface CategorySidebarProps {
  categories: Category[];
  isLoading?: boolean;
}

const buildCategoryTree = (categories: Category[]): CategoryTreeNode[] => {
  const nodeMap = new Map<number, CategoryTreeNode>();

  categories.forEach((category) => {
    nodeMap.set(category.categoryId, {
      ...category,
      parentCategoryId: category.parentCategoryId ?? null,
      children: [],
    });
  });

  const roots: CategoryTreeNode[] = [];

  nodeMap.forEach((node) => {
    if (node.parentCategoryId && nodeMap.has(node.parentCategoryId)) {
      nodeMap.get(node.parentCategoryId)?.children.push(node);
      return;
    }
    roots.push(node);
  });

  const sortNodes = (nodes: CategoryTreeNode[]) => {
    nodes.sort((a, b) => a.name.localeCompare(b.name));
    nodes.forEach((n) => sortNodes(n.children));
  };

  sortNodes(roots);
  return roots;
};

const CategoryBranch = memo(function CategoryBranch({
  node,
  depth = 0,
}: {
  node: CategoryTreeNode;
  depth?: number;
}) {
  return (
    <li role="treeitem" aria-expanded={node.children.length > 0 ? true : undefined}>
      <NavLink
        to={`/category/${node.slug}`}
        style={{ paddingLeft: `${depth * 12 + 8}px` }}
        aria-label={`${node.name}, ${node.postCount} posts`}
        className={({ isActive }) =>
          `flex items-center justify-between rounded-lg py-1.5 pr-2 text-sm transition ${
            isActive
              ? "bg-brand/10 text-brand font-medium"
              : "text-body hover:bg-canvas hover:text-heading"
          }`
        }
      >
        <span className="flex items-center gap-1.5">
          {depth > 0 && <ChevronRight className="h-3 w-3 text-subtle shrink-0" />}
          {node.name}
        </span>
        <span className="rounded-full bg-canvas px-2 py-0.5 text-[11px] font-medium text-subtle">
          {node.postCount}
        </span>
      </NavLink>

      {node.children.length > 0 && (
        <ul className="mt-0.5 space-y-0.5" role="group">
          {node.children.map((child) => (
            <CategoryBranch key={child.categoryId} node={child} depth={depth + 1} />
          ))}
        </ul>
      )}
    </li>
  );
});

const CategorySidebar = ({ categories, isLoading = false }: CategorySidebarProps) => {
  const [isOpen, setIsOpen] = useState(false);
  const tree = useMemo(() => buildCategoryTree(categories), [categories]);

  return (
    <aside className="glass-card p-5">
      <div className="flex items-center justify-between mb-3">
        <div className="flex items-center gap-2">
          <LayoutGrid className="h-4 w-4 text-brand" />
          <h2 className="text-sm font-bold text-heading">Topics</h2>
        </div>
        <button
          type="button"
          onClick={() => setIsOpen((v) => !v)}
          className="rounded-md px-2.5 py-1 text-xs font-medium text-muted transition hover:bg-canvas lg:hidden"
        >
          {isOpen ? "Hide" : "Show"}
        </button>
      </div>

      <div className={`${isOpen ? "block" : "hidden"} lg:block`}>
        {isLoading && (
          <div className="space-y-2">
            {Array.from({ length: 6 }).map((_, i) => (
              <div key={i} className="h-8 animate-pulse rounded-lg bg-gray-100" />
            ))}
          </div>
        )}

        {!isLoading && tree.length === 0 && (
          <p className="text-sm text-muted">No topics yet.</p>
        )}

        {!isLoading && tree.length > 0 && (
          <ul className="space-y-0.5" role="tree" aria-label="Category navigation">
            {tree.map((node) => (
              <CategoryBranch key={node.categoryId} node={node} />
            ))}
          </ul>
        )}
      </div>
    </aside>
  );
};

export default memo(CategorySidebar);
