import { isAxiosError } from "axios";
import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { Feather, Mail, Lock, ArrowRight } from "lucide-react";
import { LoginPayload, useAuth } from "../context/AuthContext";

interface LocationState {
  from?: { pathname?: string };
}

const getDefaultRouteForRole = (role?: "READER" | "AUTHOR" | "ADMIN") => {
  if (role === "ADMIN") return "/admin-panel";
  return "/author-dashboard";
};

const Login = () => {
  const { currentUser, login, signInWithGoogle, isAuthenticated, isLoading } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [serverError, setServerError] = useState("");

  useEffect(() => {
    if (!isLoading && isAuthenticated) {
      const destination =
        (location.state as LocationState | null)?.from?.pathname ??
        getDefaultRouteForRole(currentUser?.role);
      navigate(destination, { replace: true });
    }
  }, [currentUser?.role, isAuthenticated, isLoading, location.state, navigate]);

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    if (params.get("error") === "true") {
      setServerError("Google sign-in could not be completed. Please try again.");
    }
  }, [location.search]);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginPayload>({ defaultValues: { email: "", password: "" } });

  const onSubmit = async (data: LoginPayload) => {
    try {
      setServerError("");
      await login(data);
    } catch (error) {
      if (isAxiosError(error)) {
        const responseData = error.response?.data;
        const errorMessage =
          typeof responseData === "string"
            ? responseData
            : (responseData as { message?: string } | undefined)?.message;
        setServerError(errorMessage ?? "Login failed.");
        return;
      }
      setServerError("Something went wrong while signing in.");
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-violet-950 to-slate-900 flex items-center justify-center px-4 py-12">
      {/* Background decoration */}
      <div className="pointer-events-none absolute inset-0 overflow-hidden">
        <div className="absolute -top-40 -right-40 h-80 w-80 rounded-full bg-brand/20 blur-3xl" />
        <div className="absolute -bottom-40 -left-40 h-80 w-80 rounded-full bg-accent/20 blur-3xl" />
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 h-96 w-96 rounded-full bg-violet-600/10 blur-3xl" />
      </div>

      <div className="relative w-full max-w-5xl">
        <div className="grid gap-6 lg:grid-cols-[1.1fr_0.9fr]">
          {/* Left — branding panel */}
          <div className="flex flex-col justify-between rounded-2xl bg-white/5 border border-white/10 backdrop-blur-sm p-8 lg:p-10">
            {/* Logo */}
            <div>
              <Link to="/" className="inline-flex items-center gap-2 mb-8">
                <span className="flex h-9 w-9 items-center justify-center rounded-xl bg-brand text-white font-bold text-sm shadow-lg">
                  IW
                </span>
                <span className="font-bold text-xl text-white tracking-tight">InkWell</span>
              </Link>

              <p className="text-xs font-bold uppercase tracking-widest text-brand mb-3">
                Writers' Access
              </p>
              <h1 className="text-4xl font-extrabold text-white leading-tight lg:text-5xl">
                Return to your<br />
                <span className="bg-gradient-to-r from-brand to-pink-400 bg-clip-text text-transparent">
                  editorial desk.
                </span>
              </h1>
              <p className="mt-4 text-base leading-relaxed text-white/60 max-w-sm">
                Sign in to manage drafts, publish stories, and keep your newsroom in motion.
              </p>

              {/* Features */}
              <div className="mt-8 space-y-3">
                {[
                  "Manage your posts & drafts",
                  "Track views, likes & comments",
                  "Upload media & featured images",
                ].map((feat) => (
                  <div key={feat} className="flex items-center gap-3">
                    <span className="flex h-5 w-5 items-center justify-center rounded-full bg-brand/20 text-brand text-xs">✓</span>
                    <span className="text-sm text-white/70">{feat}</span>
                  </div>
                ))}
              </div>
            </div>

            {/* Google button */}
            <div className="mt-10 rounded-xl border border-white/10 bg-white/5 p-5">
              <p className="text-sm font-semibold text-white mb-1">Sign in with Google</p>
              <button
                type="button"
                onClick={signInWithGoogle}
                className="inline-flex w-full items-center justify-center gap-2 rounded-xl border border-[#d7deea] bg-[#ffffff] px-5 py-2.5 text-sm font-semibold text-[#1f2937] shadow-sm transition hover:bg-[#f8fafc]"
              >
                <Feather className="h-4 w-4 text-blue-500" />
                Continue with Google
              </button>
            </div>
          </div>

          {/* Right — form panel */}
          <div className="rounded-2xl bg-white p-8 lg:p-10 shadow-2xl">
            <div className="flex items-center gap-2 mb-6">
              <Feather className="h-5 w-5 text-brand" />
              <h2 className="text-xl font-bold text-heading">Sign in to InkWell</h2>
            </div>
            <p className="text-sm text-muted mb-8">
              Use the email and password from your auth service.
            </p>

            <form className="space-y-5" onSubmit={handleSubmit(onSubmit)}>
              <div>
                <label className="mb-1.5 block text-xs font-semibold uppercase tracking-widest text-muted">
                  Email
                </label>
                <div className="relative">
                  <Mail className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-subtle" />
                  <input
                    type="email"
                    className="w-full rounded-xl border border-border bg-canvas pl-10 pr-4 py-3 text-sm text-heading outline-none transition focus:border-brand focus:ring-2 focus:ring-brand/10"
                    placeholder="writer@inkwell.com"
                    {...register("email", {
                      required: "Email is required",
                      pattern: { value: /^\S+@\S+\.\S+$/, message: "Enter a valid email" },
                    })}
                  />
                </div>
                {errors.email && <p className="mt-1.5 text-xs text-red-500">{errors.email.message}</p>}
              </div>

              <div>
                <label className="mb-1.5 block text-xs font-semibold uppercase tracking-widest text-muted">
                  Password
                </label>
                <div className="relative">
                  <Lock className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-subtle" />
                  <input
                    type="password"
                    className="w-full rounded-xl border border-border bg-canvas pl-10 pr-4 py-3 text-sm text-heading outline-none transition focus:border-brand focus:ring-2 focus:ring-brand/10"
                    placeholder="••••••••"
                    {...register("password", {
                      required: "Password is required",
                      minLength: { value: 6, message: "At least 6 characters" },
                    })}
                  />
                </div>
                {errors.password && <p className="mt-1.5 text-xs text-red-500">{errors.password.message}</p>}
                <div className="flex items-center justify-end">
                  <Link
                    to="/forgot-password"
                    className="text-xs font-semibold text-brand hover:underline underline-offset-4 transition-all"
                  >
                    Forgot password?
                  </Link>
                </div>
              </div>

              {serverError && (
                <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600">
                  {serverError}
                </div>
              )}

              <button
                type="submit"
                disabled={isSubmitting}
                className="inline-flex w-full items-center justify-center gap-2 rounded-xl bg-brand py-3 text-sm font-bold text-white transition hover:bg-brand-hover disabled:cursor-not-allowed disabled:opacity-70"
              >
                {isSubmitting ? "Signing in…" : "Sign In"}
                {!isSubmitting && <ArrowRight className="h-4 w-4" />}
              </button>
            </form>

            <p className="mt-6 text-center text-sm text-muted">
              New to InkWell?{" "}
              <Link to="/register" className="font-semibold text-brand hover:underline underline-offset-4">
                Create an account
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;
