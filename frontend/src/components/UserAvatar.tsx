import { useState } from "react";
import { User } from "lucide-react";

interface UserAvatarProps {
  src?: string | null;
  alt: string;
  fallbackText?: string;
  className?: string;
  imageClassName?: string;
  fallbackClassName?: string;
  iconClassName?: string;
}

const UserAvatar = ({
  src,
  alt,
  fallbackText,
  className = "",
  imageClassName = "",
  fallbackClassName = "",
  iconClassName = "",
}: UserAvatarProps) => {
  const [imageFailed, setImageFailed] = useState(false);
  const canRenderImage = Boolean(src) && !imageFailed;
  const firstLetter = fallbackText?.trim().charAt(0).toUpperCase();

  return (
    <div
      className={`flex items-center justify-center overflow-hidden bg-slate-100 text-heading dark:bg-slate-800 ${className}`.trim()}
    >
      {canRenderImage ? (
        <img
          src={src ?? undefined}
          alt={alt}
          className={`h-full w-full object-cover ${imageClassName}`.trim()}
          onError={() => setImageFailed(true)}
        />
      ) : firstLetter ? (
        <span className={`font-black uppercase ${fallbackClassName}`.trim()}>{firstLetter}</span>
      ) : (
        <User className={`h-6 w-6 text-slate-400 dark:text-slate-500 ${iconClassName}`.trim()} />
      )}
    </div>
  );
};

export default UserAvatar;
