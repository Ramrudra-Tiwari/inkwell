import { useState } from "react";
import { useForm } from "react-hook-form";
import { Link, useNavigate } from "react-router-dom";
import { Mail, Lock, ArrowRight, ShieldCheck, Key, ArrowLeft, CheckCircle2 } from "lucide-react";
import { useAuth, ForgotPasswordPayload, VerifyOtpPayload, ResetPasswordPayload } from "../context/AuthContext";
import { isAxiosError } from "axios";

type Step = "EMAIL" | "OTP" | "RESET" | "SUCCESS";

const ForgotPassword = () => {
  const { forgotPassword, verifyOtp, resetPassword } = useAuth();
  const navigate = useNavigate();
  const [step, setStep] = useState<Step>("EMAIL");
  const [email, setEmail] = useState("");
  const [otp, setOtp] = useState("");
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const emailForm = useForm<ForgotPasswordPayload>();
  const otpForm = useForm<{ otp: string }>();
  const resetForm = useForm<{ newPassword: string; confirmPassword: string }>();

  const onEmailSubmit = async (data: ForgotPasswordPayload) => {
    try {
      setIsLoading(true);
      setError("");
      await forgotPassword(data);
      setEmail(data.email);
      setStep("OTP");
    } catch (err) {
      if (isAxiosError(err)) {
        const errorData = err.response?.data;
        setError(typeof errorData === 'string' ? errorData : (errorData?.message || "Failed to send OTP. Please check your email."));
      } else {
        setError("Something went wrong.");
      }
    } finally {
      setIsLoading(false);
    }
  };

  const onOtpSubmit = async (data: { otp: string }) => {
    try {
      setIsLoading(true);
      setError("");
      await verifyOtp({ email, otp: data.otp });
      setOtp(data.otp);
      setStep("RESET");
    } catch (err) {
      if (isAxiosError(err)) {
        const errorData = err.response?.data;
        setError(typeof errorData === 'string' ? errorData : (errorData?.message || "Invalid OTP. Please try again."));
      } else {
        setError("Something went wrong.");
      }
    } finally {
      setIsLoading(false);
    }
  };

  const onResetSubmit = async (data: { newPassword: string; confirmPassword: string }) => {
    if (data.newPassword !== data.confirmPassword) {
      setError("Passwords do not match.");
      return;
    }
    try {
      setIsLoading(true);
      setError("");
      await resetPassword({ email, otp, newPassword: data.newPassword });
      setStep("SUCCESS");
    } catch (err) {
      if (isAxiosError(err)) {
        const errorData = err.response?.data;
        setError(typeof errorData === 'string' ? errorData : (errorData?.message || "Failed to reset password."));
      } else {
        setError("Something went wrong.");
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-violet-950 to-slate-900 flex items-center justify-center px-4 py-12 relative overflow-hidden">
      {/* Background decoration */}
      <div className="pointer-events-none absolute inset-0 overflow-hidden">
        <div className="absolute -top-40 -right-40 h-80 w-80 rounded-full bg-brand/20 blur-3xl" />
        <div className="absolute -bottom-40 -left-40 h-80 w-80 rounded-full bg-accent/20 blur-3xl" />
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 h-96 w-96 rounded-full bg-violet-600/10 blur-3xl" />
      </div>

      <div className="relative w-full max-w-md">
        <div className="rounded-2xl bg-white p-8 lg:p-10 shadow-2xl">
          {step !== "SUCCESS" && (
            <button 
              onClick={() => step === "EMAIL" ? navigate("/login") : setStep(step === "OTP" ? "EMAIL" : "OTP")}
              className="mb-6 flex items-center gap-2 text-sm text-muted hover:text-brand transition-colors"
            >
              <ArrowLeft className="h-4 w-4" />
              Back
            </button>
          )}

          {step === "EMAIL" && (
            <>
              <div className="flex items-center gap-2 mb-6">
                <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-brand/10 text-brand">
                  <Mail className="h-6 w-6" />
                </div>
                <h2 className="text-2xl font-bold text-heading">Forgot Password</h2>
              </div>
              <p className="text-sm text-muted mb-8">
                Enter your email address and we'll send you a 6-digit OTP to reset your password.
              </p>

              <form className="space-y-5" onSubmit={emailForm.handleSubmit(onEmailSubmit)}>
                <div>
                  <label className="mb-1.5 block text-xs font-semibold uppercase tracking-widest text-muted">
                    Email Address
                  </label>
                  <div className="relative">
                    <Mail className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-subtle" />
                    <input
                      type="email"
                      className="w-full rounded-xl border border-border bg-canvas pl-10 pr-4 py-3 text-sm text-heading outline-none transition focus:border-brand focus:ring-2 focus:ring-brand/10"
                      placeholder="writer@inkwell.com"
                      {...emailForm.register("email", {
                        required: "Email is required",
                        pattern: { value: /^\S+@\S+\.\S+$/, message: "Enter a valid email" },
                      })}
                    />
                  </div>
                  {emailForm.formState.errors.email && (
                    <p className="mt-1.5 text-xs text-red-500">{emailForm.formState.errors.email.message}</p>
                  )}
                </div>

                {error && (
                  <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600">
                    {error}
                  </div>
                )}

                <button
                  type="submit"
                  disabled={isLoading}
                  className="inline-flex w-full items-center justify-center gap-2 rounded-xl bg-brand py-3 text-sm font-bold text-white transition hover:bg-brand-hover disabled:cursor-not-allowed disabled:opacity-70 shadow-lg shadow-brand/20"
                >
                  {isLoading ? "Sending OTP..." : "Send OTP"}
                  {!isLoading && <ArrowRight className="h-4 w-4" />}
                </button>
              </form>
            </>
          )}

          {step === "OTP" && (
            <>
              <div className="flex items-center gap-2 mb-6">
                <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-brand/10 text-brand">
                  <ShieldCheck className="h-6 w-6" />
                </div>
                <h2 className="text-2xl font-bold text-heading">Verify OTP</h2>
              </div>
              <p className="text-sm text-muted mb-8">
                We've sent a 6-digit code to <span className="font-semibold text-heading">{email}</span>. Enter it below to continue.
              </p>

              <form className="space-y-5" onSubmit={otpForm.handleSubmit(onOtpSubmit)}>
                <div>
                  <label className="mb-1.5 block text-xs font-semibold uppercase tracking-widest text-muted">
                    6-Digit Code
                  </label>
                  <div className="relative">
                    <Key className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-subtle" />
                    <input
                      type="text"
                      maxLength={6}
                      className="w-full rounded-xl border border-border bg-canvas pl-10 pr-4 py-3 text-lg font-mono tracking-[0.5em] text-heading outline-none transition focus:border-brand focus:ring-2 focus:ring-brand/10"
                      placeholder="000000"
                      {...otpForm.register("otp", {
                        required: "OTP is required",
                        minLength: { value: 6, message: "Enter all 6 digits" },
                        pattern: { value: /^\d{6}$/, message: "Only digits are allowed" }
                      })}
                    />
                  </div>
                  {otpForm.formState.errors.otp && (
                    <p className="mt-1.5 text-xs text-red-500">{otpForm.formState.errors.otp.message}</p>
                  )}
                </div>

                {error && (
                  <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600">
                    {error}
                  </div>
                )}

                <button
                  type="submit"
                  disabled={isLoading}
                  className="inline-flex w-full items-center justify-center gap-2 rounded-xl bg-brand py-3 text-sm font-bold text-white transition hover:bg-brand-hover disabled:cursor-not-allowed disabled:opacity-70 shadow-lg shadow-brand/20"
                >
                  {isLoading ? "Verifying..." : "Verify OTP"}
                  {!isLoading && <ArrowRight className="h-4 w-4" />}
                </button>
              </form>
            </>
          )}

          {step === "RESET" && (
            <>
              <div className="flex items-center gap-2 mb-6">
                <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-brand/10 text-brand">
                  <Lock className="h-6 w-6" />
                </div>
                <h2 className="text-2xl font-bold text-heading">Reset Password</h2>
              </div>
              <p className="text-sm text-muted mb-8">
                Your identity has been verified. Choose a strong new password for your account.
              </p>

              <form className="space-y-5" onSubmit={resetForm.handleSubmit(onResetSubmit)}>
                <div>
                  <label className="mb-1.5 block text-xs font-semibold uppercase tracking-widest text-muted">
                    New Password
                  </label>
                  <div className="relative">
                    <Lock className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-subtle" />
                    <input
                      type="password"
                      className="w-full rounded-xl border border-border bg-canvas pl-10 pr-4 py-3 text-sm text-heading outline-none transition focus:border-brand focus:ring-2 focus:ring-brand/10"
                      placeholder="••••••••"
                      {...resetForm.register("newPassword", {
                        required: "New password is required",
                        minLength: { value: 6, message: "At least 6 characters" },
                        pattern: {
                          value: /^(?=.*[0-9])(?=.*[!@#$%^&*()_+{}[\]:;<>,.?~\\\/ -]).{6,}$/,
                          message: "Must include at least one number and one special character"
                        }
                      })}
                    />
                  </div>
                  {resetForm.formState.errors.newPassword && (
                    <p className="mt-1.5 text-xs text-red-500">{resetForm.formState.errors.newPassword.message}</p>
                  )}
                </div>

                <div>
                  <label className="mb-1.5 block text-xs font-semibold uppercase tracking-widest text-muted">
                    Confirm New Password
                  </label>
                  <div className="relative">
                    <Lock className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-subtle" />
                    <input
                      type="password"
                      className="w-full rounded-xl border border-border bg-canvas pl-10 pr-4 py-3 text-sm text-heading outline-none transition focus:border-brand focus:ring-2 focus:ring-brand/10"
                      placeholder="••••••••"
                      {...resetForm.register("confirmPassword", {
                        required: "Please confirm your password"
                      })}
                    />
                  </div>
                  {resetForm.formState.errors.confirmPassword && (
                    <p className="mt-1.5 text-xs text-red-500">{resetForm.formState.errors.confirmPassword.message}</p>
                  )}
                </div>

                {error && (
                  <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600">
                    {error}
                  </div>
                )}

                <button
                  type="submit"
                  disabled={isLoading}
                  className="inline-flex w-full items-center justify-center gap-2 rounded-xl bg-brand py-3 text-sm font-bold text-white transition hover:bg-brand-hover disabled:cursor-not-allowed disabled:opacity-70 shadow-lg shadow-brand/20"
                >
                  {isLoading ? "Resetting..." : "Reset Password"}
                  {!isLoading && <ArrowRight className="h-4 w-4" />}
                </button>
              </form>
            </>
          )}

          {step === "SUCCESS" && (
            <div className="text-center py-4">
              <div className="flex justify-center mb-6">
                <div className="flex h-16 w-16 items-center justify-center rounded-full bg-green-100 text-green-600">
                  <CheckCircle2 className="h-10 w-10" />
                </div>
              </div>
              <h2 className="text-2xl font-bold text-heading mb-2">Password Reset!</h2>
              <p className="text-sm text-muted mb-8">
                Your password has been successfully updated. You can now sign in with your new password.
              </p>
              <Link
                to="/login"
                className="inline-flex w-full items-center justify-center gap-2 rounded-xl bg-brand py-3 text-sm font-bold text-white transition hover:bg-brand-hover shadow-lg shadow-brand/20"
              >
                Go to Login
                <ArrowRight className="h-4 w-4" />
              </Link>
            </div>
          )}
        </div>

        <p className="mt-8 text-center text-sm text-white/50">
          Remember your password?{" "}
          <Link to="/login" className="font-semibold text-white hover:underline underline-offset-4 transition-colors">
            Sign In
          </Link>
        </p>
      </div>
    </div>
  );
};

export default ForgotPassword;
