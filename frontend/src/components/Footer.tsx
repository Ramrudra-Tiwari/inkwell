import { Link } from "react-router-dom";
import { Heart, User, BookOpen } from "lucide-react";

const Footer = () => {
  return (
    <footer className="border-t border-border bg-surface mt-auto">
      <div className="mx-auto max-w-6xl px-4 py-8">
        <div className="flex flex-col gap-6 md:flex-row md:items-start md:justify-between">
          {/* Brand */}
          <div className="flex flex-col gap-2">
            <Link to="/" className="flex items-center gap-2">
              <span className="flex h-7 w-7 items-center justify-center rounded-md bg-brand text-white text-xs font-bold">
                IW
              </span>
              <span className="font-sans text-base font-bold text-heading">InkWell</span>
            </Link>
            <p className="max-w-xs text-xs leading-relaxed text-muted">
              A clean publishing platform for independent writers and thoughtful readers.
            </p>
          </div>

          {/* Links */}
          <div className="flex gap-12">
            <div className="flex flex-col gap-2">
              <p className="text-xs font-semibold uppercase tracking-widest text-subtle">Platform</p>
              <Link to="/" className="text-sm text-muted transition hover:text-heading">Feed</Link>
              <Link to="/login" className="text-sm text-muted transition hover:text-heading">Sign In</Link>
              <Link to="/register" className="text-sm text-muted transition hover:text-heading">Get Started</Link>
            </div>
            <div className="flex flex-col gap-2">
              <p className="text-xs font-semibold uppercase tracking-widest text-subtle">Write</p>
              <Link to="/author-dashboard" className="text-sm text-muted transition hover:text-heading">Dashboard</Link>
              <Link to="/author/posts/new" className="text-sm text-muted transition hover:text-heading">New Story</Link>
            </div>
          </div>

          {/* Social */}
            <a
              href="#"
              aria-label="Twitter"
              className="flex h-8 w-8 items-center justify-center rounded-full border border-border text-muted transition hover:border-heading hover:text-heading"
            >
              <Heart className="h-3.5 w-3.5" />
            </a>
            <a
              href="#"
              aria-label="GitHub"
              className="flex h-8 w-8 items-center justify-center rounded-full border border-border text-muted transition hover:border-heading hover:text-heading"
            >
              <User className="h-3.5 w-3.5" />
            </a>
            <a
              href="#"
              aria-label="Blog"
              className="flex h-8 w-8 items-center justify-center rounded-full border border-border text-muted transition hover:border-heading hover:text-heading"
            >
              <BookOpen className="h-3.5 w-3.5" />
            </a>
        </div>

        <div className="mt-8 flex flex-col gap-1 border-t border-border pt-6 md:flex-row md:items-center md:justify-between">
          <p className="text-xs text-subtle">
            © {new Date().getFullYear()} InkWell. Built for independent writers.
          </p>
          <p className="text-xs text-subtle">Made with ❤️ by the InkWell team</p>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
