import "react-quill-new/dist/quill.snow.css";

import { isAxiosError } from "axios";
import { FormEvent, useEffect, useMemo, useRef, useState } from "react";
import ReactQuill from "react-quill-new";
import { Link, useNavigate, useParams } from "react-router-dom";
import { ArrowLeft, Save, PenLine, Image as ImageIcon, Settings } from "lucide-react";
import FeaturedImageUpload from "../components/FeaturedImageUpload";
import MediaLibrary from "../components/MediaLibrary";
import { useAuth } from "../context/AuthContext";
import mediaService from "../services/mediaService";
import postService from "../services/postService";
import { BASE_URL } from "../api/axiosInstance";
import { Media, UploadResponse } from "../types/media";
import { CreatePostPayload, PostStatus, UpdatePostPayload } from "../types/post";

const quillModules = {
  toolbar: [
    [{ header: [1, 2, 3, false] }],
    ["bold", "italic", "underline", "blockquote"],
    [{ list: "ordered" }, { list: "bullet" }],
    ["link", "image"],
    ["clean"],
  ],
};

const quillFormats = [
  "header", "bold", "italic", "underline", "blockquote",
  "list", "link", "image",
];

const statusOptions: { value: PostStatus; label: string; color: string }[] = [
  { value: "DRAFT",       label: "Draft",       color: "text-amber-600" },
  { value: "PUBLISHED",   label: "Published",   color: "text-emerald-600" },
  { value: "UNPUBLISHED", label: "Unpublished", color: "text-gray-500" },
  { value: "ARCHIVED",    label: "Archived",    color: "text-red-500" },
];

const PostEditor = () => {
  const { currentUser } = useAuth();
  const navigate = useNavigate();
  const { postId } = useParams();
  const editingPostId = postId ? Number(postId) : null;
  const isEditMode = Number.isInteger(editingPostId);

  const [title, setTitle] = useState("");
  const [excerpt, setExcerpt] = useState("");
  const [featuredImageUrl, setFeaturedImageUrl] = useState("");
  const [featuredImageAltText, setFeaturedImageAltText] = useState("");
  const [uploadedMedia, setUploadedMedia] = useState<UploadResponse | null>(null);
  const [selectedMediaId, setSelectedMediaId] = useState<number | null>(null);
  const [status, setStatus] = useState<PostStatus>("DRAFT");
  const [content, setContent] = useState("");
  const [isLoading, setIsLoading] = useState(Boolean(isEditMode));
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState("");


  useEffect(() => {
    const loadPost = async () => {
      if (!editingPostId) return;
      try {
        setIsLoading(true);
        const post = await postService.getById(editingPostId);
        setTitle(post.title);
        setExcerpt(post.excerpt ?? "");
        setFeaturedImageUrl(post.featuredImageUrl ?? "");
        setStatus(post.status);
        setContent(post.content);

        const linkedMedia = await mediaService.getByPost(editingPostId);
        const featuredMedia = linkedMedia[0];
        if (featuredMedia) {
          setUploadedMedia({
            mediaId: featuredMedia.mediaId,
            originalName: featuredMedia.originalName,
            url: featuredMedia.url,
            mimeType: featuredMedia.mimeType,
            sizeKb: featuredMedia.sizeKb,
            uploadedAt: featuredMedia.uploadedAt,
            message: "Existing media linked",
            statusCode: 200,
          });
          setSelectedMediaId(featuredMedia.mediaId);
          setFeaturedImageAltText(featuredMedia.altText ?? "");
          setFeaturedImageUrl(featuredMedia.url);
        }
      } catch (e) {
        console.error(e);
        setError("Unable to load this draft for editing.");
      } finally {
        setIsLoading(false);
      }
    };
    void loadPost();
  }, [editingPostId]);

  const editorHeading = useMemo(
    () => (isEditMode ? "Revise your article" : "Compose a new story"),
    [isEditMode]
  );

  const handleMediaSelection = (media: Media) => {
    setSelectedMediaId(media.mediaId);
    setFeaturedImageUrl(media.url);
    setFeaturedImageAltText(media.altText ?? "");
    setUploadedMedia({
      mediaId: media.mediaId,
      originalName: media.originalName,
      url: media.url,
      mimeType: media.mimeType,
      sizeKb: media.sizeKb,
      uploadedAt: media.uploadedAt,
      message: "Media selected from library",
      statusCode: 200,
    });
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!currentUser?.userId) { setError("You need an authenticated session to save posts."); return; }
    if (!title.trim() || !content.trim()) { setError("Title and content are required."); return; }

    try {
      setIsSaving(true);
      setError("");

      if (editingPostId) {
        const updatePayload: UpdatePostPayload = { title, content, excerpt, featuredImageUrl, status };
        const updatedPost = await postService.update(editingPostId, updatePayload);
        const mediaIdToLink = selectedMediaId ?? uploadedMedia?.mediaId;
        if (mediaIdToLink) await mediaService.linkToPost(mediaIdToLink, updatedPost.postId);
      } else {
        const createPayload: CreatePostPayload = { authorId: currentUser.userId, title, content, excerpt, featuredImageUrl, status };
        const createdPost = await postService.create(createPayload);
        const mediaIdToLink = selectedMediaId ?? uploadedMedia?.mediaId;
        if (mediaIdToLink) await mediaService.linkToPost(mediaIdToLink, createdPost.postId);
      }

      navigate("/author-dashboard", { replace: true });
    } catch (saveError) {
      if (isAxiosError(saveError)) {
        const responseData = saveError.response?.data as { message?: string } | string | undefined;
        setError(typeof responseData === "string" ? responseData : responseData?.message ?? "Unable to save the post.");
      } else {
        setError("Unable to save the post.");
      }
    } finally {
      setIsSaving(false);
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-canvas">
        <div className="mx-auto max-w-6xl px-4 py-8 space-y-4">
          <div className="h-16 animate-pulse rounded-xl bg-gray-100" />
          <div className="h-64 animate-pulse rounded-xl bg-gray-100" />
          <div className="h-80 animate-pulse rounded-xl bg-gray-100" />
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-canvas">
      {/* Page header */}
      <div className="border-b border-border bg-white">
        <div className="mx-auto max-w-6xl px-4 py-5">
          <div className="flex flex-wrap items-center justify-between gap-4">
            <div className="flex items-center gap-3">
              <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-brand to-pink-500 text-white">
                <PenLine className="h-5 w-5" />
              </div>
              <div>
                <p className="text-xs font-bold uppercase tracking-widest text-brand">Writer Editor</p>
                <h1 className="text-xl font-extrabold text-heading">{editorHeading}</h1>
              </div>
            </div>
            <Link
              to="/author-dashboard"
              className="inline-flex items-center gap-2 rounded-xl border border-border bg-surface px-4 py-2 text-sm font-medium text-body shadow-card transition hover:border-brand/40 hover:text-brand"
            >
              <ArrowLeft className="h-4 w-4" />
              Back to Dashboard
            </Link>
          </div>
        </div>
      </div>

      {/* Editor layout */}
      <div className="mx-auto max-w-6xl px-4 py-6">
        <form onSubmit={handleSubmit} className="grid gap-6 lg:grid-cols-[1fr_320px]">
          {/* Main writing area */}
          <div className="space-y-5">
            {/* Title */}
            <div className="rounded-xl border border-border bg-white p-5 shadow-card">
              <label className="mb-2 block text-xs font-bold uppercase tracking-widest text-muted">
                Title
              </label>
              <input
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="Write a compelling headline…"
                className="w-full rounded-xl border border-border bg-canvas px-4 py-3 text-lg font-semibold text-heading outline-none transition focus:border-brand focus:ring-2 focus:ring-brand/10"
              />
            </div>

            {/* Excerpt */}
            <div className="rounded-xl border border-border bg-white p-5 shadow-card">
              <label className="mb-2 block text-xs font-bold uppercase tracking-widest text-muted">
                Excerpt
              </label>
              <textarea
                value={excerpt}
                onChange={(e) => setExcerpt(e.target.value)}
                rows={3}
                placeholder="A short summary shown on feed cards and previews…"
                className="w-full rounded-xl border border-border bg-canvas px-4 py-3 text-sm text-body outline-none transition focus:border-brand focus:ring-2 focus:ring-brand/10 resize-none"
              />
            </div>

            {/* Rich content editor */}
            <div className="rounded-xl border border-border bg-white p-5 shadow-card">
              <label className="mb-3 block text-xs font-bold uppercase tracking-widest text-muted">
                Content
              </label>
              <div className="inkwell-editor">
                <ReactQuill
                  theme="snow"
                  value={content}
                  onChange={setContent}
                  modules={quillModules}
                  formats={quillFormats}
                  placeholder="Write your story here…"
                />
              </div>
            </div>
          </div>

          {/* Sidebar panel */}
          <aside className="space-y-5">
            {/* Publish settings */}
            <div className="rounded-xl border border-border bg-white p-5 shadow-card">
              <div className="flex items-center gap-2 mb-4">
                <Settings className="h-4 w-4 text-brand" />
                <h2 className="text-sm font-bold text-heading">Publish Settings</h2>
              </div>

              <label className="mb-1.5 block text-xs font-semibold uppercase tracking-widest text-muted">
                Status
              </label>
              <select
                value={status}
                onChange={(e) => setStatus(e.target.value as PostStatus)}
                className="w-full rounded-xl border border-border bg-canvas px-3 py-2.5 text-sm font-medium text-heading outline-none transition focus:border-brand focus:ring-2 focus:ring-brand/10"
              >
                {statusOptions.map(opt => (
                  <option key={opt.value} value={opt.value}>{opt.label}</option>
                ))}
              </select>

              {/* Status indicator */}
              <div className={`mt-3 inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-semibold
                ${status === "PUBLISHED" ? "bg-emerald-100 text-emerald-700" :
                  status === "DRAFT" ? "bg-amber-100 text-amber-700" :
                  status === "ARCHIVED" ? "bg-red-100 text-red-600" :
                  "bg-gray-100 text-gray-600"}`}>
                <span className="h-1.5 w-1.5 rounded-full bg-current" />
                {status}
              </div>

              {error && (
                <div className="mt-4 rounded-xl border border-red-200 bg-red-50 px-3 py-2.5 text-xs text-red-600">
                  {error}
                </div>
              )}

              <button
                type="submit"
                disabled={isSaving}
                className="mt-5 inline-flex w-full items-center justify-center gap-2 rounded-xl bg-brand py-3 text-sm font-bold text-white transition hover:bg-brand-hover disabled:cursor-not-allowed disabled:opacity-70"
              >
                <Save className="h-4 w-4" />
                {isSaving ? "Saving…" : isEditMode ? "Update Post" : "Create Post"}
              </button>
            </div>

            {/* Featured image URL */}
            <div className="rounded-xl border border-border bg-white p-5 shadow-card">
              <div className="flex items-center gap-2 mb-4">
                <ImageIcon className="h-4 w-4 text-brand" />
                <h2 className="text-sm font-bold text-heading">Featured Image</h2>
              </div>

              {/* Preview */}
              {featuredImageUrl && (
                <img
                  src={featuredImageUrl.startsWith("http") ? featuredImageUrl : `${BASE_URL}${featuredImageUrl}`}
                  alt={featuredImageAltText || "Featured image"}
                  className="mb-3 w-full rounded-xl object-cover"
                  style={{ maxHeight: "160px" }}
                  onError={(e) => { (e.currentTarget as HTMLImageElement).style.display = "none"; }}
                />
              )}

              <FeaturedImageUpload
                currentImageUrl={featuredImageUrl}
                currentAltText={featuredImageAltText}
                uploaderId={currentUser?.userId}
                onUploadComplete={(upload, altText) => {
                  setUploadedMedia(upload);
                  setSelectedMediaId(upload.mediaId);
                  setFeaturedImageUrl(upload.url);
                  setFeaturedImageAltText(altText);
                }}
              />

              <div className="mt-4">
                <label className="mb-1.5 block text-xs font-semibold uppercase tracking-widest text-muted">
                  Or paste image URL
                </label>
                <input
                  value={featuredImageUrl}
                  onChange={(e) => setFeaturedImageUrl(e.target.value)}
                  placeholder="https://example.com/cover.jpg"
                  className="w-full rounded-xl border border-border bg-canvas px-3 py-2 text-sm text-body outline-none transition focus:border-brand focus:ring-2 focus:ring-brand/10"
                />
              </div>
            </div>

            {/* Media library */}
            <div className="rounded-xl border border-border bg-white p-5 shadow-card">
              <MediaLibrary
                uploaderId={currentUser?.userId}
                selectedMediaId={selectedMediaId}
                onSelect={handleMediaSelection}
              />
            </div>
          </aside>
        </form>
      </div>
    </div>
  );
};

export default PostEditor;
