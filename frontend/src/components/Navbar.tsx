import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import NotificationBell from "./NotificationBell";
import RoleBasedNav from "./RoleBasedNav";
import ThemeToggle from "./ThemeToggle";
import UserAvatar from "./UserAvatar";

const Navbar = () => {
  const { isAuthenticated, currentUser } = useAuth();

  return (
    <header className="glass-nav">
      <div className="mx-auto flex max-w-6xl items-center gap-6 px-4 py-3">
        {/* Logo */}
        <Link
          to="/"
          className="group flex items-center gap-3 font-sans text-xl font-[900] text-heading tracking-tight shrink-0 transition-transform hover:scale-105"
        >
          <div className="relative">
            <div className="absolute inset-0 bg-brand blur-md opacity-20 group-hover:opacity-40 transition-opacity" />
            <span className="relative flex h-10 w-10 items-center justify-center rounded-2xl bg-gradient-to-br from-brand to-accent text-white text-base font-black shadow-lg shadow-brand/20">
              IW
            </span>
          </div>
          <span className="bg-gradient-to-r from-heading to-muted bg-clip-text text-transparent">InkWell</span>
        </Link>

        {/* Nav + Search — takes remaining space */}
        <div className="flex flex-1 items-center justify-center">
          <RoleBasedNav />
        </div>

        {/* Right side: notifications + user info */}
        <div className="flex items-center gap-4 shrink-0">
          <ThemeToggle />
          {isAuthenticated && <NotificationBell />}
          {isAuthenticated && (
            <Link to="/profile" className="flex items-center gap-2 group">
              <UserAvatar
                src={currentUser?.avatarUrl}
                alt={currentUser?.fullName ?? "User"}
                fallbackText={currentUser?.fullName}
                className="h-9 w-9 rounded-2xl border border-brand/20 bg-gradient-to-br from-brand/10 to-accent/10 text-brand shadow-sm transition-all group-hover:border-brand/40 group-hover:shadow-md"
                fallbackClassName="text-sm text-brand"
                iconClassName="h-4 w-4 text-brand"
              />
              <span className="hidden text-xs font-black uppercase tracking-widest text-heading md:block group-hover:text-brand transition-colors">
                {currentUser?.fullName?.split(" ")[0]}
              </span>
            </Link>
          )}
          {!isAuthenticated && (
            <div className="flex items-center gap-3">
              <Link
                to="/login"
                className="text-xs font-black uppercase tracking-widest text-muted transition hover:text-brand"
              >
                Sign in
              </Link>
              <Link
                to="/register"
                className="rounded-xl bg-heading px-6 py-3 text-xs font-black uppercase tracking-widest text-canvas transition-all hover:bg-brand hover:scale-105 hover:shadow-xl hover:shadow-brand/20 active:scale-95"
              >
                Get started
              </Link>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};

export default Navbar;
