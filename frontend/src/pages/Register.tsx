import { isAxiosError } from "axios";
import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { Link, useNavigate } from "react-router-dom";
import {
  ArrowRight,
  AtSign,
  Feather,
  Globe,
  KeyRound,
  Lock,
  Mail,
  User,
} from "lucide-react";
import { RegisterPayload, useAuth } from "../context/AuthContext";

const getDefaultRouteForRole = (role?: "READER" | "AUTHOR" | "ADMIN") => {
  if (role === "ADMIN") return "/admin-panel";
  return "/author-dashboard";
};

const Register = () => {
  const {
    currentUser,
    login,
    requestRegistrationOtp,
    verifyRegistrationOtp,
    signInWithGoogle,
    isAuthenticated,
    isLoading,
  } = useAuth();
  const navigate = useNavigate();
  const [serverError, setServerError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [pendingRegistration, setPendingRegistration] = useState<RegisterPayload | null>(null);
  const [isOtpStep, setIsOtpStep] = useState(false);
  const [otp, setOtp] = useState("");
  const [isVerifyingOtp, setIsVerifyingOtp] = useState(false);

  useEffect(() => {
    if (!isLoading && isAuthenticated) {
      navigate(getDefaultRouteForRole(currentUser?.role), { replace: true });
    }
  }, [currentUser?.role, isAuthenticated, isLoading, navigate]);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<RegisterPayload>({
    defaultValues: { fullName: "", username: "", email: "", password: "" },
  });

  const onSubmit = async (data: RegisterPayload) => {
    try {
      setServerError("");
      setSuccessMessage("");
      await requestRegistrationOtp(data);
      setPendingRegistration(data);
      setIsOtpStep(true);
      setOtp("");
      setSuccessMessage(`We sent a 6-digit OTP to ${data.email}. Enter it below to finish creating your account.`);
    } catch (error) {
      if (isAxiosError(error)) {
        const responseData = error.response?.data;
        const errorMessage =
          typeof responseData === "string"
            ? responseData
            : (responseData as { message?: string } | undefined)?.message;
        setServerError(errorMessage ?? "Registration failed.");
        return;
      }
      setServerError("Something went wrong while creating the account.");
    }
  };

  const handleVerifyOtp = async () => {
    if (!pendingRegistration) {
      setServerError("Your registration session expired. Please fill the form again.");
      setIsOtpStep(false);
      return;
    }

    try {
      setIsVerifyingOtp(true);
      setServerError("");
      setSuccessMessage("");
      await verifyRegistrationOtp({ email: pendingRegistration.email, otp });
      await login({ email: pendingRegistration.email, password: pendingRegistration.password });
    } catch (error) {
      if (isAxiosError(error)) {
        const responseData = error.response?.data;
        const errorMessage =
          typeof responseData === "string"
            ? responseData
            : (responseData as { message?: string } | undefined)?.message;
        setServerError(errorMessage ?? "OTP verification failed.");
        return;
      }
      setServerError("Something went wrong while verifying the OTP.");
    } finally {
      setIsVerifyingOtp(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-violet-950 to-slate-900 flex items-center justify-center px-4 py-12">
      <div className="pointer-events-none absolute inset-0 overflow-hidden">
        <div className="absolute -top-40 -right-40 h-80 w-80 rounded-full bg-brand/20 blur-3xl" />
        <div className="absolute -bottom-40 -left-40 h-80 w-80 rounded-full bg-accent/20 blur-3xl" />
      </div>

      <div className="relative w-full max-w-5xl">
        <div className="grid gap-6 lg:grid-cols-[0.9fr_1.1fr]">
          <div className="flex flex-col justify-between rounded-2xl bg-white/5 border border-white/10 backdrop-blur-sm p-8">
            <div>
              <Link to="/" className="inline-flex items-center gap-2 mb-8">
                <span className="flex h-9 w-9 items-center justify-center rounded-xl bg-brand text-white font-bold text-sm shadow-lg">IW</span>
                <span className="font-bold text-xl text-white tracking-tight">InkWell</span>
              </Link>
              <p className="text-xs font-bold uppercase tracking-widest text-brand mb-3">Founding Account</p>
              <h1 className="text-4xl font-extrabold text-white leading-tight">
                Set your byline
                <br />
                <span className="bg-gradient-to-r from-brand to-pink-400 bg-clip-text text-transparent">in ink.</span>
              </h1>
              <p className="mt-4 text-sm leading-relaxed text-white/60">
                Create your account with email verification so every new writer joins with a confirmed inbox.
              </p>
            </div>

            <div className="mt-8 rounded-xl border border-white/10 bg-white/5 p-5">
              <p className="text-sm font-semibold text-white mb-3">Quick sign-up</p>
              <button
                type="button"
                onClick={signInWithGoogle}
                className="inline-flex w-full items-center justify-center gap-2 rounded-xl border border-[#d7deea] bg-[#ffffff] px-5 py-2.5 text-sm font-semibold text-[#1f2937] shadow-sm transition hover:bg-[#f8fafc]"
              >
                <Globe className="h-4 w-4 text-blue-500" />
                Sign up with Google
              </button>
            </div>
          </div>

          <div className="rounded-2xl bg-white p-8 shadow-2xl">
            <div className="flex items-center gap-2 mb-2">
              <Feather className="h-5 w-5 text-brand" />
              <h2 className="text-xl font-bold text-heading">
                {isOtpStep ? "Verify your email" : "Create your account"}
              </h2>
            </div>
            <p className="text-sm text-muted mb-6">
              {isOtpStep
                ? "Enter the one-time password we just sent to your email."
                : "Join InkWell and start publishing today."}
            </p>

            <form className="grid gap-4 md:grid-cols-2" onSubmit={handleSubmit(onSubmit)}>
              {!isOtpStep ? (
                <>
                  <div className="md:col-span-2">
                    <label className="mb-1.5 block text-xs font-semibold uppercase tracking-widest text-muted">Full Name</label>
                    <div className="relative">
                      <User className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-subtle" />
                      <input
                        type="text"
                        className="w-full rounded-xl border border-border bg-canvas pl-10 pr-4 py-3 text-sm text-heading outline-none transition focus:border-brand focus:ring-2 focus:ring-brand/10"
                        placeholder="Ava Thompson"
                        {...register("fullName", {
                          required: "Full name is required",
                          minLength: { value: 3, message: "At least 3 characters" },
                        })}
                      />
                    </div>
                    {errors.fullName && <p className="mt-1 text-xs text-red-500">{errors.fullName.message}</p>}
                  </div>

                  <div>
                    <label className="mb-1.5 block text-xs font-semibold uppercase tracking-widest text-muted">Username</label>
                    <div className="relative">
                      <AtSign className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-subtle" />
                      <input
                        type="text"
                        className="w-full rounded-xl border border-border bg-canvas pl-10 pr-4 py-3 text-sm text-heading outline-none transition focus:border-brand focus:ring-2 focus:ring-brand/10"
                        placeholder="avawrites"
                        {...register("username", {
                          required: "Username is required",
                          minLength: { value: 3, message: "At least 3 characters" },
                        })}
                      />
                    </div>
                    {errors.username && <p className="mt-1 text-xs text-red-500">{errors.username.message}</p>}
                  </div>

                  <div>
                    <label className="mb-1.5 block text-xs font-semibold uppercase tracking-widest text-muted">Email</label>
                    <div className="relative">
                      <Mail className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-subtle" />
                      <input
                        type="email"
                        className="w-full rounded-xl border border-border bg-canvas pl-10 pr-4 py-3 text-sm text-heading outline-none transition focus:border-brand focus:ring-2 focus:ring-brand/10"
                        placeholder="ava@inkwell.com"
                        {...register("email", {
                          required: "Email is required",
                          pattern: { value: /^\S+@\S+\.\S+$/, message: "Valid email required" },
                        })}
                      />
                    </div>
                    {errors.email && <p className="mt-1 text-xs text-red-500">{errors.email.message}</p>}
                  </div>

                  <div className="md:col-span-2">
                    <label className="mb-1.5 block text-xs font-semibold uppercase tracking-widest text-muted">Password</label>
                    <div className="relative">
                      <Lock className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-subtle" />
                      <input
                        type="password"
                        className="w-full rounded-xl border border-border bg-canvas pl-10 pr-4 py-3 text-sm text-heading outline-none transition focus:border-brand focus:ring-2 focus:ring-brand/10"
                        placeholder="Choose a strong password"
                        {...register("password", {
                          required: "Password is required",
                          minLength: { value: 6, message: "At least 6 characters" },
                          pattern: {
                            value: /^(?=.*[0-9])(?=.*[!@#$%^&*()_+{}[\]:;<>,.?~\\\/ -]).{6,}$/,
                            message: "Must include at least one number and one special character",
                          },
                        })}
                      />
                    </div>
                    {errors.password && <p className="mt-1 text-xs text-red-500">{errors.password.message}</p>}
                  </div>
                </>
              ) : (
                <>
                  <div className="md:col-span-2 rounded-xl border border-brand/15 bg-brand/5 px-4 py-3 text-sm text-heading">
                    <p className="font-semibold text-heading">Email verification pending</p>
                    <p className="mt-1 text-muted">
                      We sent a verification code to{" "}
                      <span className="font-semibold text-heading">{pendingRegistration?.email}</span>.
                    </p>
                  </div>

                  <div className="md:col-span-2">
                    <label className="mb-1.5 block text-xs font-semibold uppercase tracking-widest text-muted">OTP Code</label>
                    <div className="relative">
                      <KeyRound className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-subtle" />
                      <input
                        type="text"
                        inputMode="numeric"
                        maxLength={6}
                        value={otp}
                        onChange={(event) => setOtp(event.target.value.replace(/\D/g, "").slice(0, 6))}
                        className="w-full rounded-xl border border-border bg-canvas pl-10 pr-4 py-3 text-sm tracking-[0.35em] text-heading outline-none transition focus:border-brand focus:ring-2 focus:ring-brand/10"
                        placeholder="123456"
                      />
                    </div>
                    <p className="mt-1 text-xs text-muted">Use the 6-digit code from your inbox.</p>
                  </div>
                </>
              )}

              {serverError && (
                <div className="md:col-span-2 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600">
                  {serverError}
                </div>
              )}

              {successMessage && (
                <div className="md:col-span-2 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700">
                  {successMessage}
                </div>
              )}

              {!isOtpStep ? (
                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="md:col-span-2 inline-flex w-full items-center justify-center gap-2 rounded-xl bg-brand py-3 text-sm font-bold text-white transition hover:bg-brand-hover disabled:cursor-not-allowed disabled:opacity-70"
                >
                  {isSubmitting ? "Sending OTP..." : "Create Account"}
                  {!isSubmitting && <ArrowRight className="h-4 w-4" />}
                </button>
              ) : (
                <>
                  <button
                    type="button"
                    onClick={handleVerifyOtp}
                    disabled={isVerifyingOtp || otp.length !== 6}
                    className="md:col-span-2 inline-flex w-full items-center justify-center gap-2 rounded-xl bg-brand py-3 text-sm font-bold text-white transition hover:bg-brand-hover disabled:cursor-not-allowed disabled:opacity-70"
                  >
                    {isVerifyingOtp ? "Verifying OTP..." : "Verify OTP & Create Account"}
                    {!isVerifyingOtp && <ArrowRight className="h-4 w-4" />}
                  </button>

                  <button
                    type="button"
                    onClick={() => {
                      setIsOtpStep(false);
                      setOtp("");
                      setServerError("");
                      setSuccessMessage("");
                    }}
                    className="md:col-span-2 inline-flex w-full items-center justify-center rounded-xl border border-border bg-white py-3 text-sm font-semibold text-heading transition hover:bg-slate-50"
                  >
                    Back to form
                  </button>
                </>
              )}
            </form>

            <p className="mt-5 text-center text-sm text-muted">
              Already have an account?{" "}
              <Link to="/login" className="font-semibold text-brand hover:underline underline-offset-4">
                Sign in here
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Register;
