interface UploadProgressProps {
  progress: number;
}

const UploadProgress = ({ progress }: UploadProgressProps) => {
  return (
    <div className="space-y-2 rounded-2xl border border-brass/20 bg-brass/10 p-4">
      <div className="flex items-center justify-between text-xs uppercase tracking-[0.18em] text-ink/70">
        <span>Upload Progress</span>
        <span>{progress}%</span>
      </div>
      <div className="h-3 overflow-hidden rounded-full bg-ink/10">
        <div
          className="h-full rounded-full bg-brass transition-[width] duration-300"
          style={{ width: `${progress}%` }}
        />
      </div>
    </div>
  );
};

export default UploadProgress;
