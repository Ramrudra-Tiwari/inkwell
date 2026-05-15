import { isAxiosError } from "axios";
import { useEffect, useState } from "react";
import { ThumbsUp, Reply, Trash2, User, Clock } from "lucide-react";
import { useAuth } from "../context/AuthContext";
import commentService from "../services/commentService";
import { Comment, CommentApiError } from "../types/comment";
import CommentForm from "./CommentForm";

interface CommentItemProps {
  comment: Comment;
  depth?: 0 | 1;
  onReplyCreated: (parentCommentId: number, reply: Comment) => void;
  onCommentDeleted: (commentId: number) => void;
  onCommentUpdated: (commentId: number, updatedComment: Comment) => void;
  onCommentLiked: (commentId: number) => void;
  showToast: (message: string, tone?: "success" | "error" | "info") => void;
}

const formatTimestamp = (timestamp: string) =>
  new Intl.DateTimeFormat("en-IN", {
    day: "numeric",
    month: "short",
    year: "numeric",
    hour: "numeric",
    minute: "2-digit"
  }).format(new Date(timestamp));

const avatarGradients = [
  "from-violet-500 to-purple-600",
  "from-sky-500 to-blue-600",
  "from-emerald-500 to-teal-600",
  "from-amber-500 to-orange-600",
  "from-rose-500 to-pink-600",
];

const getAvatarGradient = (id: number) => avatarGradients[id % avatarGradients.length];

const getLikedCommentsStorageKey = (userId?: number) =>
  userId ? `inkwell-liked-comments-${userId}` : "";

const readLikedComments = (userId?: number) => {
  if (!userId) return new Set<number>();

  try {
    const rawValue = localStorage.getItem(getLikedCommentsStorageKey(userId));
    if (!rawValue) return new Set<number>();
    const parsed = JSON.parse(rawValue) as number[];
    return new Set(Array.isArray(parsed) ? parsed : []);
  } catch {
    return new Set<number>();
  }
};

const writeLikedComments = (userId: number, likedComments: Set<number>) => {
  localStorage.setItem(getLikedCommentsStorageKey(userId), JSON.stringify(Array.from(likedComments)));
};

const CommentItem = ({
  comment,
  depth = 0,
  onReplyCreated,
  onCommentDeleted,
  onCommentUpdated,
  onCommentLiked,
  showToast
}: CommentItemProps) => {
  const { currentUser, isAuthenticated } = useAuth();
  const [isReplying, setIsReplying] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [actionError, setActionError] = useState("");
  const [isLiked, setIsLiked] = useState(() => readLikedComments(currentUser?.userId).has(comment.commentId));

  const isDeleted = comment.status === "DELETED";
  
  const isWithinWindow = () => {
    const createdTime = new Date(comment.createdAt).getTime();
    const now = new Date().getTime();
    const diffMins = (now - createdTime) / (1000 * 60);
    return diffMins <= 15;
  };

  const isOwner = currentUser?.userId === comment.authorId;
  const isAdmin = currentUser?.role === "ADMIN";
  
  const canEdit = isOwner && !isDeleted && isWithinWindow();
  const canDelete = (isOwner && isWithinWindow()) || isAdmin;
  const canReply = depth === 0 && !isDeleted;

  useEffect(() => {
    setIsLiked(readLikedComments(currentUser?.userId).has(comment.commentId));
  }, [comment.commentId, currentUser?.userId]);

  const handleUpdate = async (content: string) => {
    try {
      setIsSaving(true);
      setActionError("");
      const updated = await commentService.update(comment.commentId, { content });
      onCommentUpdated(comment.commentId, updated);
      setIsEditing(false);
      showToast("Thought refined successfully.", "success");
      return true;
    } catch (error) {
      setActionError("Unable to update your thought.");
      return false;
    } finally {
      setIsSaving(false);
    }
  };

  const handleReplySubmit = async (content: string) => {
    if (!currentUser) {
      setActionError("Please log in to reply.");
      return false;
    }

    try {
      setIsSaving(true);
      setActionError("");

      const createdReply = await commentService.add({
        postId: comment.postId,
        authorId: currentUser.userId,
        parentCommentId: comment.commentId,
        content
      });

      onReplyCreated(comment.commentId, createdReply);
      setIsReplying(false);
      showToast("Reply posted successfully.", "success");
      return true;
    } catch (error) {
      if (isAxiosError(error)) {
        const responseData = error.response?.data as CommentApiError | string | undefined;
        setActionError(
          typeof responseData === "string"
            ? responseData
            : responseData?.message ?? "Unable to post your reply."
        );
      } else {
        setActionError("Unable to post your reply.");
      }
      return false;
    } finally {
      setIsSaving(false);
    }
  };

  const handleLike = async () => {
    if (!isAuthenticated) {
      showToast("Please log in to like comments.", "info");
      return;
    }

    try {
      setActionError("");

      if (isLiked && currentUser?.userId) {
        showToast("You already liked this comment.", "info");
        return;
      }

      const status = await commentService.like(comment.commentId, currentUser!.userId);
      const nextLikedComments = readLikedComments(currentUser!.userId);
      nextLikedComments.add(comment.commentId);
      writeLikedComments(currentUser!.userId, nextLikedComments);
      setIsLiked(true);

      if (status === 201) {
        onCommentLiked(comment.commentId);
        showToast("Comment liked.", "success");
      } else {
        showToast("You already liked this comment.", "info");
      }
    } catch (error) {
      if (isAxiosError(error)) {
        const responseData = error.response?.data as CommentApiError | string | undefined;
        setActionError(
          typeof responseData === "string"
            ? responseData
            : responseData?.message ?? "Unable to like this comment."
        );
      } else {
        setActionError("Unable to like this comment.");
      }
    }
  };

  const handleDelete = async () => {
    if (!canDelete) return;

    try {
      setActionError("");
      await commentService.delete(comment.commentId);
      onCommentDeleted(comment.commentId);
      showToast("Comment removed.", "success");
    } catch (error) {
      if (isAxiosError(error)) {
        const responseData = error.response?.data as CommentApiError | string | undefined;
        setActionError(
          typeof responseData === "string"
            ? responseData
            : responseData?.message ?? "Unable to remove this comment."
        );
      } else {
        setActionError("Unable to remove this comment.");
      }
    }
  };

  return (
    <div
      className={`relative rounded-[1.5rem] border border-slate-100 bg-white p-6 shadow-sm transition-all duration-300 hover:shadow-md ${
        depth === 1 ? "ml-4 md:ml-12 mt-4" : "mt-6"
      }`}
    >
      {/* Thread Line for replies */}
      {depth === 1 && (
        <div className="absolute -left-6 top-0 h-full w-px bg-slate-200 md:-left-8" />
      )}

      <div className="flex items-start gap-4">
        {/* Avatar */}
        <div className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-gradient-to-br ${getAvatarGradient(comment.authorId)} text-white shadow-sm`}>
          {isDeleted ? <Trash2 className="h-4 w-4" /> : <User className="h-5 w-5" />}
        </div>

        <div className="flex-1 min-w-0">
          <div className="flex flex-wrap items-center justify-between gap-2">
            <div>
              <p className="text-sm font-black text-heading">
                {isDeleted ? "Removed User" : `Reader #${comment.authorId}`}
              </p>
              <div className="flex items-center gap-2 text-[10px] font-bold uppercase tracking-widest text-subtle">
                <Clock className="h-3 w-3" />
                {formatTimestamp(comment.createdAt)}
                {comment.updatedAt !== comment.createdAt && !isDeleted && (
                  <span className="italic opacity-60">(Edited)</span>
                )}
              </div>
            </div>

            <div className={`flex items-center gap-1.5 rounded-full px-3 py-1 text-[10px] font-black ${
              isLiked ? "bg-emerald-500/10 text-emerald-500" : "bg-slate-50 text-brand"
            }`}>
              <ThumbsUp className={`h-3 w-3 ${isLiked ? "fill-emerald-500 text-emerald-500" : ""}`} />
              {comment.likesCount}
            </div>
          </div>

          {/* Content or Edit Form */}
          {isEditing ? (
            <div className="mt-4">
               <CommentForm
                initialContent={comment.content}
                isSubmitting={isSaving}
                submitLabel="Save Changes"
                onSubmit={handleUpdate}
                onCancel={() => setIsEditing(false)}
              />
            </div>
          ) : (
            <div className={`mt-4 text-sm leading-relaxed ${isDeleted ? "italic text-muted/60" : "text-slate-600"}`}>
              {isDeleted ? (
                "This thought has been retracted by the author or moderator."
              ) : (
                comment.content.split(/(@\w+)/g).map((part, i) => 
                  part.startsWith('@') ? (
                    <span key={i} className="font-black text-brand bg-brand/5 px-1 rounded cursor-pointer hover:bg-brand/10 transition">
                      {part}
                    </span>
                  ) : part
                )
              )}
            </div>
          )}

          {/* Actions */}
          {!isDeleted && !isEditing && (
            <div className="mt-5 flex flex-wrap items-center gap-2">
              <button
                onClick={handleLike}
                className={`flex items-center gap-1.5 rounded-xl px-4 py-2 text-[10px] font-black uppercase tracking-widest transition-all ${
                  isLiked
                    ? "bg-emerald-500 text-white shadow-lg shadow-emerald-500/20"
                    : isAuthenticated 
                    ? "bg-brand/5 text-brand hover:bg-emerald-500 hover:text-white" 
                    : "bg-slate-50 text-slate-400 cursor-not-allowed"
                }`}
                aria-pressed={isLiked}
              >
                <ThumbsUp className={`h-3.5 w-3.5 ${isLiked ? "fill-white" : ""}`} />
                Like
              </button>

              {canReply && (
                <button
                  onClick={() => {
                    if (!isAuthenticated) {
                      showToast("Please log in to reply.", "info");
                      return;
                    }
                    setIsReplying(!isReplying);
                  }}
                  className={`flex items-center gap-1.5 rounded-xl px-4 py-2 text-[10px] font-black uppercase tracking-widest transition-all ${
                    isReplying ? "bg-slate-900 text-white" : "bg-slate-100 text-slate-600 hover:bg-slate-200"
                  }`}
                >
                  <Reply className="h-3.5 w-3.5" />
                  {isReplying ? "Cancel" : "Reply"}
                </button>
              )}

              {canEdit && (
                 <button
                  onClick={() => setIsEditing(true)}
                  className="flex items-center gap-1.5 rounded-xl bg-slate-100 px-4 py-2 text-[10px] font-black uppercase tracking-widest text-slate-600 transition-all hover:bg-slate-200"
                >
                  Edit
                </button>
              )}

              {canDelete && (
                <button
                  onClick={handleDelete}
                  className="flex items-center gap-1.5 rounded-xl bg-red-50 px-4 py-2 text-[10px] font-black uppercase tracking-widest text-red-500 transition-all hover:bg-red-500 hover:text-white"
                >
                  <Trash2 className="h-3.5 w-3.5" />
                  Delete
                </button>
              )}
            </div>
          )}

          {actionError && <p className="mt-3 text-[10px] font-bold text-red-500 uppercase tracking-widest">{actionError}</p>}

          {/* Reply Form */}
          {isReplying && !isEditing && (
            <div className="mt-6 rounded-2xl border border-slate-100 bg-slate-50/50 p-4">
              <CommentForm
                isSubmitting={isSaving}
                placeholder="Share your perspective..."
                submitLabel="Post Reply"
                onSubmit={handleReplySubmit}
                onCancel={() => setIsReplying(false)}
              />
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default CommentItem;
