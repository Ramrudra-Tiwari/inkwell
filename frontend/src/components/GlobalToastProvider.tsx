import {
  createContext,
  ReactNode,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState
} from "react";
import { CheckCircle2, AlertTriangle, X } from "lucide-react";
import apiClient from "../api/axiosInstance";

type ToastTone = "success" | "error" | "info";

interface ToastItem {
  id: number;
  message: string;
  tone: ToastTone;
}

interface ToastContextValue {
  showToast: (message: string, tone?: ToastTone) => void;
}

const ToastContext = createContext<ToastContextValue | undefined>(undefined);

interface GlobalToastProviderProps {
  children: ReactNode;
}

const toneStyles: Record<ToastTone, string> = {
  success: "border-emerald-400/50 bg-slate-950 text-emerald-100 shadow-emerald-950/30",
  error: "border-red-400/55 bg-slate-950 text-red-100 shadow-red-950/30",
  info: "border-sky-300/45 bg-slate-950 text-slate-100 shadow-slate-950/30"
};

export const GlobalToastProvider = ({ children }: GlobalToastProviderProps) => {
  const [toasts, setToasts] = useState<ToastItem[]>([]);
  const idRef = useRef(0);

  const showToast = (message: string, tone: ToastTone = "info") => {
    idRef.current += 1;
    const nextToast: ToastItem = {
      id: idRef.current,
      message,
      tone
    };

    setToasts((current) => [...current, nextToast]);
    window.setTimeout(() => {
      setToasts((current) => current.filter((toast) => toast.id !== nextToast.id));
    }, 3500);
  };

  useEffect(() => {
    const interceptorId = apiClient.interceptors.response.use(
      (response) => response,
      (error) => {
        const skipToast = error.config?.headers?.["x-skip-toast"] === "true";

        if (!skipToast) {
          const responseData = error.response?.data as { message?: string } | string | undefined;
          const message =
            typeof responseData === "string"
              ? responseData
              : responseData?.message ?? "A service is temporarily unavailable.";

          showToast(message, "error");
        }

        return Promise.reject(error);
      }
    );

    return () => {
      apiClient.interceptors.response.eject(interceptorId);
    };
  }, []);

  const value = useMemo(
    () => ({
      showToast
    }),
    []
  );

  return (
    <ToastContext.Provider value={value}>
      {children}

      <div className="pointer-events-none fixed right-4 top-4 z-[100] flex w-full max-w-sm flex-col gap-3">
        {toasts.map((toast) => (
          <div
            key={toast.id}
            className={`pointer-events-auto rounded-[1.25rem] border px-4 py-3 shadow-2xl backdrop-blur-xl ${toneStyles[toast.tone]}`}
          >
            <div className="flex items-start gap-3">
              {toast.tone === "success" ? (
                <CheckCircle2 className="mt-0.5 h-5 w-5 shrink-0 text-emerald-300" />
              ) : toast.tone === "error" ? (
                <AlertTriangle className="mt-0.5 h-5 w-5 shrink-0 text-red-300" />
              ) : (
                <CheckCircle2 className="mt-0.5 h-5 w-5 shrink-0 text-sky-300" />
              )}
              <p className="flex-1 text-sm font-semibold leading-6">{toast.message}</p>
              <button
                type="button"
                onClick={() =>
                  setToasts((current) => current.filter((currentToast) => currentToast.id !== toast.id))
                }
                className="rounded-full p-1 text-slate-300 transition hover:bg-white/10 hover:text-white"
                aria-label="Dismiss notification"
              >
                <X className="h-4 w-4" />
              </button>
            </div>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
};

export const useToast = () => {
  const context = useContext(ToastContext);

  if (!context) {
    throw new Error("useToast must be used within GlobalToastProvider");
  }

  return context;
};

export default GlobalToastProvider;
