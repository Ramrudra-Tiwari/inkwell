import { Check, CheckCheck, Bell, X } from "lucide-react";
import { Notification } from "../types/messaging";

interface NotificationDropdownProps {
  error?: string;
  isLoading?: boolean;
  notifications: Notification[];
  onMarkAllAsRead: () => void;
  onMarkAsRead: (notificationId: number) => void;
  onDismiss: (notificationId: number) => void;
}

const formatTimestamp = (value: string) =>
  new Intl.DateTimeFormat("en-IN", {
    day: "numeric",
    month: "short",
    hour: "numeric",
    minute: "2-digit",
  }).format(new Date(value));

const NotificationDropdown = ({
  error = "",
  isLoading = false,
  notifications,
  onMarkAllAsRead,
  onMarkAsRead,
  onDismiss,
}: NotificationDropdownProps) => {
  return (
    <div className="absolute right-0 z-[1000] mt-2 w-80 overflow-hidden rounded-2xl border border-border bg-white shadow-modal">
      {/* Header */}
      <div className="flex items-center justify-between border-b border-border bg-canvas px-4 py-3">
        <div className="flex items-center gap-2">
          <Bell className="h-4 w-4 text-brand" />
          <h3 className="text-sm font-bold text-heading">Notifications</h3>
          {notifications.filter(n => !n.isRead).length > 0 && (
            <span className="rounded-full bg-brand/10 px-2 py-0.5 text-[11px] font-bold text-brand">
              {notifications.filter(n => !n.isRead).length}
            </span>
          )}
        </div>
        <button
          type="button"
          onClick={onMarkAllAsRead}
          className="inline-flex items-center gap-1.5 rounded-lg border border-border px-2.5 py-1 text-[11px] font-semibold text-muted transition hover:bg-white hover:text-heading"
        >
          <CheckCheck className="h-3.5 w-3.5" />
          Read all
        </button>
      </div>

      {/* Body */}
      <div className="max-h-[360px] overflow-y-auto">
        {isLoading && (
          <div className="space-y-2 p-4">
            {Array.from({ length: 3 }).map((_, i) => (
              <div key={i} className="h-14 animate-pulse rounded-xl bg-gray-100" />
            ))}
          </div>
        )}

        {error && (
          <div className="m-4 rounded-xl border border-red-200 bg-red-50 px-3 py-2 text-xs text-red-600">
            {error}
          </div>
        )}

        {!isLoading && notifications.length === 0 && (
          <div className="flex flex-col items-center gap-2 py-10 text-center">
            <Bell className="h-8 w-8 text-subtle" />
            <p className="text-sm font-semibold text-heading">All caught up!</p>
            <p className="text-xs text-muted">No notifications yet.</p>
          </div>
        )}

        <div className="p-3 space-y-2">
          {notifications.map((notification) => (
            <article
              key={notification.notificationId}
              className={`rounded-xl border p-3 transition ${
                notification.isRead
                  ? "border-border bg-canvas"
                  : "border-brand/20 bg-brand/5"
              }`}
            >
              <div className="flex items-start justify-between gap-2">
                <div className="min-w-0">
                  <div className="flex items-center gap-2 flex-wrap">
                    <p className="text-sm font-semibold text-heading truncate">
                      {notification.title}
                    </p>
                    {!notification.isRead && (
                      <span className="shrink-0 rounded-full bg-brand px-1.5 py-0.5 text-[9px] font-bold uppercase text-white">
                        NEW
                      </span>
                    )}
                  </div>
                  <p className="mt-0.5 text-xs leading-relaxed text-muted line-clamp-2">
                    {notification.message}
                  </p>
                  <p className="mt-1.5 text-[10px] font-medium text-subtle">
                    {formatTimestamp(notification.createdAt)}
                  </p>
                </div>

                <div className="flex shrink-0 items-center gap-1.5">
                  {!notification.isRead && (
                    <button
                      type="button"
                      onClick={() => onMarkAsRead(notification.notificationId)}
                      className="flex h-7 w-7 items-center justify-center rounded-full border border-border bg-white text-muted transition hover:border-emerald-500 hover:text-emerald-600"
                      aria-label="Mark as read"
                    >
                      <Check className="h-3.5 w-3.5" />
                    </button>
                  )}
                  <button
                    type="button"
                    onClick={() => onDismiss(notification.notificationId)}
                    className="flex h-7 w-7 items-center justify-center rounded-full border border-border bg-white text-muted transition hover:border-red-500 hover:text-red-500"
                    aria-label="Dismiss"
                  >
                    <X className="h-3.5 w-3.5" />
                  </button>
                </div>
              </div>
            </article>
          ))}
        </div>
      </div>
    </div>
  );
};

export default NotificationDropdown;
