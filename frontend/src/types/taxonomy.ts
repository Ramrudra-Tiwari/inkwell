export interface Category {
  categoryId: number;
  name: string;
  slug: string;
  description?: string | null;
  parentCategoryId?: number | null;
  subCategories?: Category[];
  postCount?: number;
}

export interface Tag {
  tagId: number;
  name: string;
  slug: string;
  postCount: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface CategoryTreeNode extends Category {
  children: CategoryTreeNode[];
}

export interface BreadcrumbItem {
  label: string;
  to?: string;
}

export type TaxonomyKind = "category" | "tag";
