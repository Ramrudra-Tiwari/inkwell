import { FormEvent, useMemo, useState } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import { Search } from "lucide-react";
import { useAuth } from "../context/AuthContext";
import ConfirmationModal from "./ConfirmationModal";

const RoleBasedNav = () => {
  const navigate = useNavigate();
  const { currentUser, isAuthenticated, logout } = useAuth();
  const [keyword, setKeyword] = useState("");
  const [searchOpen, setSearchOpen] = useState(false);
  const [isLogoutModalOpen, setIsLogoutModalOpen] = useState(false);

  const links = useMemo(() => {
    if (!isAuthenticated || !currentUser) {
      return [{ to: "/", label: "Feed" }];
    }

    if (currentUser.role === "ADMIN") {
      return [
        { to: "/", label: "Feed" },
        { to: "/admin-panel", label: "Admin" },
        { to: "/profile", label: "Profile" },
      ];
    }

    if (currentUser.role === "AUTHOR") {
      return [
        { to: "/", label: "Feed" },
        { to: "/author-dashboard", label: "Dashboard" },
        { to: "/author/posts/new", label: "Write" },
        { to: "/profile", label: "Profile" },
      ];
    }

    return [
      { to: "/", label: "Feed" },
      { to: "/author-dashboard", label: "Dashboard" },
      { to: "/author/posts/new", label: "Write" },
      { to: "/profile", label: "Profile" },
    ];
  }, [currentUser, isAuthenticated]);

  const handleSearch = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const normalized = keyword.trim();
    if (!normalized) return;
    navigate(`/search?keyword=${encodeURIComponent(normalized)}`);
    setSearchOpen(false);
    setKeyword("");
  };

  return (
    <div className="flex flex-1 items-center gap-1">
      {/* Nav links */}
      <nav className="flex items-center gap-1">
        {links.map((link) => (
          <NavLink
            key={link.to}
            to={link.to}
            end={link.to === "/"}
            className={({ isActive }) =>
              `rounded-xl px-4 py-2 text-sm font-bold transition-all duration-200 active:scale-95 active:opacity-80 ${
                isActive
                  ? "bg-brand text-white shadow-md shadow-brand/20"
                  : "text-slate-500 hover:bg-brand/10 hover:text-brand hover:scale-105"
              }`
            }
          >
            {link.label}
          </NavLink>
        ))}

        {isAuthenticated && (
          <button
            type="button"
            onClick={() => setIsLogoutModalOpen(true)}
            className="rounded-md px-3 py-1.5 text-sm font-medium text-muted transition hover:bg-canvas hover:text-heading"
          >
            Logout
          </button>
        )}
      </nav>

      <ConfirmationModal
        isOpen={isLogoutModalOpen}
        title="Confirm Logout"
        message="Are you sure you want to log out of InkWell? You will need to sign in again to access your dashboard."
        confirmLabel="Logout"
        onConfirm={() => {
          setIsLogoutModalOpen(false);
          logout();
        }}
        onCancel={() => setIsLogoutModalOpen(false)}
      />

      {/* Spacer */}
      <div className="flex-1" />

      {/* Search */}
      {searchOpen ? (
        <form
          onSubmit={handleSearch}
          className="flex items-center gap-2 rounded-full border border-border bg-canvas px-3 py-1.5 shadow-sm transition-all"
          role="search"
        >
          <Search className="h-4 w-4 shrink-0 text-subtle" />
          <input
            autoFocus
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            onBlur={() => { if (!keyword) setSearchOpen(false); }}
            placeholder="Search stories…"
            className="w-40 bg-transparent text-sm text-heading outline-none placeholder:text-subtle"
          />
          <button
            type="submit"
            className="rounded-full bg-brand px-2.5 py-0.5 text-[11px] font-semibold text-white transition hover:bg-brand-hover"
          >
            Go
          </button>
        </form>
      ) : (
        <button
          type="button"
          aria-label="Open search"
          onClick={() => setSearchOpen(true)}
          className="flex h-8 w-8 items-center justify-center rounded-full text-muted transition hover:bg-canvas hover:text-heading"
        >
          <Search className="h-4 w-4" />
        </button>
      )}
    </div>
  );
};

export default RoleBasedNav;
