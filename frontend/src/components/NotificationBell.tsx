import { isAxiosError } from "axios";
import { Bell } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { useAuth } from "../context/AuthContext";
import messagingService from "../services/messagingService";
import { Notification } from "../types/messaging";
import NotificationDropdown from "./NotificationDropdown";

const NotificationBell = () => {
  const { currentUser, isAuthenticated } = useAuth();
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [isOpen, setIsOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");

  const recentNotifications = useMemo(
    () =>
      [...notifications]
        .sort((first, second) => new Date(second.createdAt).getTime() - new Date(first.createdAt).getTime())
        .slice(0, 6),
    [notifications]
  );

  useEffect(() => {
    if (!isAuthenticated || !currentUser?.userId) {
      setNotifications([]);
      setUnreadCount(0);
      setIsOpen(false);
      return;
    }

    let intervalId: number | undefined;

    const loadNotifications = async () => {
      try {
        setIsLoading(true);
        setError("");
        const [count, items] = await Promise.all([
          messagingService.getUnreadCountForUser(currentUser.userId),
          messagingService.getNotificationsForUser(currentUser.userId)
        ]);

        setUnreadCount(count);
        setNotifications(items);
      } catch (loadError) {
        console.error(loadError);
        setError("Unable to refresh notifications.");
      } finally {
        setIsLoading(false);
      }
    };

    void loadNotifications();
    intervalId = window.setInterval(() => {
      void loadNotifications();
    }, 30000);

    return () => {
      if (intervalId) {
        window.clearInterval(intervalId);
      }
    };
  }, [currentUser?.userId, isAuthenticated]);

  const markAsRead = async (notificationId: number) => {
    try {
      await messagingService.markNotificationAsRead(notificationId);
      setNotifications((currentNotifications) =>
        currentNotifications.map((notification) =>
          notification.notificationId === notificationId
            ? { ...notification, isRead: true }
            : notification
        )
      );
      setUnreadCount((currentCount) => Math.max(0, currentCount - 1));
    } catch (markError) {
      if (isAxiosError(markError)) {
        const responseData = markError.response?.data as { message?: string } | string | undefined;
        setError(
          typeof responseData === "string"
            ? responseData
            : responseData?.message ?? "Unable to mark notification as read."
        );
      } else {
        setError("Unable to mark notification as read.");
      }
    }
  };

  const markAllAsRead = async () => {
    if (!currentUser?.userId) {
      return;
    }

    try {
      await messagingService.markAllNotificationsAsRead(currentUser.userId);
      setNotifications((currentNotifications) =>
        currentNotifications.map((notification) => ({ ...notification, isRead: true }))
      );
      setUnreadCount(0);
    } catch (markError) {
      console.error(markError);
      setError("Unable to mark all notifications as read.");
    }
  };

  const handleDismiss = async (notificationId: number) => {
    try {
      await messagingService.deleteNotification(notificationId);
      setNotifications((current) => current.filter((n) => n.notificationId !== notificationId));
      // If it was unread, decrement the count
      setUnreadCount((current) => {
        const wasUnread = notifications.find((n) => n.notificationId === notificationId && !n.isRead);
        return wasUnread ? Math.max(0, current - 1) : current;
      });
    } catch (err) {
      console.error(err);
      setError("Unable to dismiss notification.");
    }
  };

  if (!isAuthenticated) {
    return null;
  }

  return (
    <div className="relative">
      <button
        type="button"
        onClick={() => setIsOpen((currentValue) => !currentValue)}
        className="relative flex h-8 w-8 items-center justify-center rounded-full border border-border bg-surface text-muted transition hover:border-brand/40 hover:text-brand"
        aria-label="Open notifications"
      >
        <Bell className="h-5 w-5" />
        {unreadCount > 0 && (
          <span className="absolute -right-1 -top-1 flex h-5 min-w-5 items-center justify-center rounded-full bg-red-600 px-1 text-[10px] font-bold text-white">
            {unreadCount > 9 ? "9+" : unreadCount}
          </span>
        )}
      </button>

      {isOpen && (
        <NotificationDropdown
          error={error}
          isLoading={isLoading}
          notifications={recentNotifications}
          onMarkAllAsRead={() => {
            void markAllAsRead();
          }}
          onMarkAsRead={(notificationId) => {
            void markAsRead(notificationId);
          }}
          onDismiss={(notificationId) => {
            void handleDismiss(notificationId);
          }}
        />
      )}
    </div>
  );
};

export default NotificationBell;
