import { isAxiosError } from "axios";
import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { MessageSquare, Sparkles, LogIn } from "lucide-react";
import { useAuth } from "../context/AuthContext";
import commentService from "../services/commentService";
import { Comment, CommentApiError, ThreadedComment } from "../types/comment";
import CommentForm from "./CommentForm";
import { useToast } from "./GlobalToastProvider";
import CommentItem from "./CommentItem";

interface CommentSectionProps {
  postId: number;
}

const buildThreadedComments = (comments: Comment[]): ThreadedComment[] => {
  const topLevelComments = comments
    .filter((comment) => comment.parentCommentId === null)
    .sort((first, second) => new Date(first.createdAt).getTime() - new Date(second.createdAt).getTime());

  return topLevelComments.map((comment) => ({
    ...comment,
    replies: comments
      .filter((reply) => reply.parentCommentId === comment.commentId)
      .sort((first, second) => new Date(first.createdAt).getTime() - new Date(second.createdAt).getTime())
  }));
};

const CommentSection = ({ postId }: CommentSectionProps) => {
  const { currentUser, isAuthenticated } = useAuth();
  const { showToast } = useToast();
  const [comments, setComments] = useState<Comment[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    const loadComments = async () => {
      try {
        setIsLoading(true);
        setError("");
        const fetchedComments = await commentService.getByPost(postId);
        setComments(fetchedComments);
      } catch (loadError) {
        console.error(loadError);
        setError("Unable to load the conversation right now.");
      } finally {
        setIsLoading(false);
      }
    };

    void loadComments();
  }, [postId]);

  const threadedComments = useMemo(() => buildThreadedComments(comments), [comments]);

  const handleCreateTopLevelComment = async (content: string) => {
    if (!currentUser) {
      setError("Please log in to join the conversation.");
      return false;
    }

    try {
      setIsSubmitting(true);
      setError("");

      const createdComment = await commentService.add({
        postId,
        authorId: currentUser.userId,
        parentCommentId: null,
        content
      });

      setComments((currentComments) => [...currentComments, createdComment]);
      showToast("Your thought has been shared with the world.", "success");
      return true;
    } catch (submitError) {
      if (isAxiosError(submitError)) {
        const responseData = submitError.response?.data as CommentApiError | string | undefined;
        setError(
          typeof responseData === "string"
            ? responseData
            : responseData?.message ?? "Unable to post your comment."
        );
      } else {
        setError("Unable to post your comment.");
      }
      return false;
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleReplyCreated = (_parentCommentId: number, reply: Comment) => {
    setComments((currentComments) => {
      const nextComments = [...currentComments, reply];
      return nextComments.filter(
        (comment, index, collection) =>
          collection.findIndex((candidate) => candidate.commentId === comment.commentId) === index
      );
    });
  };

  const handleCommentDeleted = (commentId: number) => {
    setComments((currentComments) =>
      currentComments.map((comment) =>
        comment.commentId === commentId
          ? {
              ...comment,
              status: "DELETED",
              content: "This comment has been removed"
            }
          : comment
      )
    );
  };

  const handleCommentLiked = (commentId: number) => {
    setComments((currentComments) =>
      currentComments.map((comment) =>
        comment.commentId === commentId
          ? { ...comment, likesCount: comment.likesCount + 1 }
          : comment
      )
    );
  };

  const handleCommentUpdated = (commentId: number, updatedComment: Comment) => {
    setComments((currentComments) =>
      currentComments.map((comment) =>
        comment.commentId === commentId ? updatedComment : comment
      )
    );
  };

  return (
    <section className="mt-20">
      <div className="rounded-[2.5rem] bg-white p-8 md:p-12 shadow-sm border border-slate-100">
        <div className="flex flex-wrap items-end justify-between gap-6 border-b border-slate-50 pb-10">
          <div className="space-y-4">
            <div className="inline-flex items-center gap-2 rounded-full bg-brand/5 px-4 py-2 text-[10px] font-black uppercase tracking-[0.2em] text-brand">
              <Sparkles className="h-3.5 w-3.5" />
              Community Discussion
            </div>
            <h2 className="font-sans text-4xl font-black tracking-tight text-heading">The Perspective Section</h2>
            <p className="max-w-xl text-lg text-slate-500 leading-relaxed">
              Join the conversation. Share your thoughts, provide feedback, or start a new thread of wisdom.
            </p>
          </div>

          <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-slate-50 text-brand">
            <div className="relative">
              <MessageSquare className="h-6 w-6" />
              <div className="absolute -right-1 -top-1 flex h-4 w-4 items-center justify-center rounded-full bg-brand text-[8px] font-black text-white">
                {comments.length}
              </div>
            </div>
          </div>
        </div>

        <div className="mt-12 rounded-[2rem] border border-slate-100 bg-slate-50/50 p-8">
          {isAuthenticated ? (
            <CommentForm
              isSubmitting={isSubmitting}
              placeholder="What are your thoughts on this piece?"
              submitLabel="Share Perspective"
              onSubmit={handleCreateTopLevelComment}
            />
          ) : (
            <div className="flex flex-wrap items-center justify-between gap-6">
              <div className="flex items-center gap-4">
                 <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-white shadow-sm border border-slate-100">
                    <LogIn className="h-5 w-5 text-brand" />
                 </div>
                 <p className="text-sm font-bold text-slate-500">Log in to join this thoughtful discussion.</p>
              </div>
              <Link
                to="/login"
                className="rounded-2xl bg-slate-900 px-8 py-3 text-xs font-black uppercase tracking-widest text-white transition-all hover:bg-brand hover:scale-105 active:scale-95 shadow-lg shadow-slate-200"
              >
                Sign In to Comment
              </Link>
            </div>
          )}
        </div>

        {error && (
          <div className="mt-6 rounded-2xl border border-red-100 bg-red-50/50 px-6 py-4 text-[10px] font-black uppercase tracking-widest text-red-500">
            {error}
          </div>
        )}

        <div className="mt-12 space-y-2">
          {isLoading ? (
            <div className="space-y-6">
              {Array.from({ length: 3 }).map((_, index) => (
                <div
                  key={index}
                  className="h-40 animate-pulse rounded-[2rem] bg-slate-50"
                />
              ))}
            </div>
          ) : threadedComments.length === 0 ? (
            <div className="rounded-[2rem] bg-slate-50/50 p-16 text-center border-2 border-dashed border-slate-100">
              <MessageSquare className="mx-auto h-12 w-12 text-slate-200" />
              <h3 className="mt-4 text-lg font-black text-heading">Be the first to speak.</h3>
              <p className="mt-2 text-slate-400">The conversation starts with you.</p>
            </div>
          ) : (
            threadedComments.map((comment) => (
              <div key={comment.commentId} className="group">
                <CommentItem
                  comment={comment}
                  depth={0}
                  onReplyCreated={handleReplyCreated}
                  onCommentDeleted={handleCommentDeleted}
                  onCommentUpdated={handleCommentUpdated}
                  onCommentLiked={handleCommentLiked}
                  showToast={showToast}
                />

                {comment.replies.length > 0 && (
                  <div className="relative">
                    {comment.replies.map((reply) => (
                      <CommentItem
                        key={reply.commentId}
                        comment={reply}
                        depth={1}
                        onReplyCreated={handleReplyCreated}
                        onCommentDeleted={handleCommentDeleted}
                        onCommentUpdated={handleCommentUpdated}
                        onCommentLiked={handleCommentLiked}
                        showToast={showToast}
                      />
                    ))}
                  </div>
                )}
              </div>
            ))
          )}
        </div>
      </div>
    </section>
  );
};

export default CommentSection;

