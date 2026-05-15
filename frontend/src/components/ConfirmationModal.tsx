import { motion, AnimatePresence } from "framer-motion";
import { X, LogOut, Info } from "lucide-react";
import { createPortal } from "react-dom";

interface ConfirmationModalProps {
  isOpen: boolean;
  title: string;
  message: string;
  confirmLabel?: string;
  cancelLabel?: string;
  onConfirm: () => void;
  onCancel: () => void;
}

const ConfirmationModal = ({
  isOpen,
  title,
  message,
  confirmLabel = "Confirm",
  cancelLabel = "Cancel",
  onConfirm,
  onCancel,
}: ConfirmationModalProps) => {
  const isLogout = title.toLowerCase().includes("logout");

  const modalContent = (
    <AnimatePresence>
      {isOpen && (
        <div className="fixed inset-0 z-[99999] flex items-center justify-center p-6">
          {/* Backdrop with high-end blur */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={onCancel}
            className="fixed inset-0 bg-slate-900/40 backdrop-blur-[12px]"
          />

          {/* Premium Modal Card */}
          <motion.div
            initial={{ opacity: 0, scale: 0.94, y: 30 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            exit={{ opacity: 0, scale: 0.94, y: 30 }}
            transition={{ type: "spring", damping: 25, stiffness: 300 }}
            className="relative z-[100000] w-full max-w-md overflow-hidden rounded-[3.5rem] border border-white/60 bg-white/70 p-12 shadow-[0_48px_96px_-12px_rgba(15,23,42,0.15)] backdrop-blur-3xl"
          >
            {/* Subtle Inner Glow */}
            <div className="absolute inset-0 pointer-events-none rounded-[3.5rem] ring-1 ring-inset ring-white/50" />

            {/* Top Close Button */}
            <button
              onClick={onCancel}
              className="absolute right-8 top-8 flex h-10 w-10 items-center justify-center rounded-full bg-slate-100/50 text-slate-400 transition-all hover:bg-white hover:text-slate-900 hover:shadow-sm"
            >
              <X className="h-5 w-5" />
            </button>

            <div className="flex flex-col items-center text-center">
              {/* Dynamic Icon with Pulse Effect */}
              <motion.div 
                initial={{ scale: 0.8 }}
                animate={{ scale: 1 }}
                transition={{ delay: 0.1, type: "spring" }}
                className={`group relative mb-10 flex h-24 w-24 items-center justify-center rounded-[2.5rem] transition-all ${
                  isLogout 
                    ? "bg-rose-500 text-white shadow-[0_20px_40px_-10px_rgba(244,63,94,0.3)]" 
                    : "bg-indigo-600 text-white shadow-[0_20px_40px_-10px_rgba(79,70,229,0.3)]"
                }`}
              >
                {isLogout ? <LogOut className="h-10 w-10" /> : <Info className="h-10 w-10" />}
                <div className="absolute inset-0 animate-ping rounded-[2.5rem] bg-current opacity-10 group-hover:hidden" />
              </motion.div>

              {/* Typography Section */}
              <h3 className="mb-4 font-sans text-3xl font-black tracking-tight text-slate-900">
                {title}
              </h3>
              <p className="mb-12 text-[17px] leading-relaxed text-slate-500 font-medium">
                {message}
              </p>

              {/* Tacitile Action Buttons */}
              <div className="flex w-full flex-col gap-4 sm:flex-row">
                <button
                  onClick={onCancel}
                  className="flex-1 rounded-[1.75rem] bg-slate-100 px-8 py-5 text-[11px] font-black uppercase tracking-[0.25em] text-slate-500 transition-all hover:bg-slate-200 hover:text-slate-900 active:scale-95 shadow-sm"
                >
                  {cancelLabel}
                </button>
                <button
                  onClick={onConfirm}
                  className={`flex-1 rounded-[1.75rem] px-8 py-5 text-[11px] font-black uppercase tracking-[0.25em] text-white shadow-xl transition-all active:scale-95 ${
                    isLogout 
                      ? "bg-rose-500 shadow-rose-200 hover:bg-rose-600 hover:shadow-rose-300" 
                      : "bg-indigo-600 shadow-indigo-200 hover:bg-indigo-700 hover:shadow-indigo-300"
                  }`}
                >
                  {confirmLabel}
                </button>
              </div>
            </div>

            {/* Minimalist Footer Detail */}
            <div className="mt-12 flex flex-col items-center gap-2 opacity-10">
               <div className="h-1.5 w-10 rounded-full bg-slate-900" />
               <span className="text-[10px] font-black uppercase tracking-[0.6em] text-slate-900">InkWell Core</span>
            </div>
          </motion.div>
        </div>
      )}
    </AnimatePresence>
  );

  return createPortal(modalContent, document.body);
};

export default ConfirmationModal;



