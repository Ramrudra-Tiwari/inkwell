import {
  createContext,
  ReactNode,
  useContext,
  useEffect,
  useMemo,
  useState
} from "react";
import { useNavigate } from "react-router-dom";
import apiClient, { TOKEN_STORAGE_KEY, USER_STORAGE_KEY } from "../api/axiosInstance";

export interface AuthUser {
  userId: number;
  username: string;
  email: string;
  fullName: string;
  role: "READER" | "AUTHOR" | "ADMIN";
  bio?: string;
  avatarUrl?: string;
  provider: "LOCAL" | "GOOGLE" | "GITHUB";
  isActive: boolean;
  createdAt?: string;
}

export interface LoginPayload {
  email: string;
  password: string;
}

export interface RegisterPayload {
  fullName: string;
  username: string;
  email: string;
  password: string;
}

export interface ForgotPasswordPayload {
  email: string;
}

export interface VerifyOtpPayload {
  email: string;
  otp: string;
}

export interface ResetPasswordPayload {
  email: string;
  otp: string;
  newPassword: string;
}

interface LoginResponse {
  token: string;
  user: AuthUser;
}

interface AuthContextValue {
  currentUser: AuthUser | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (payload: LoginPayload) => Promise<void>;
  register: (payload: RegisterPayload) => Promise<void>;
  requestRegistrationOtp: (payload: RegisterPayload) => Promise<void>;
  verifyRegistrationOtp: (payload: VerifyOtpPayload) => Promise<void>;
  logout: () => void;
  signInWithGoogle: () => void;
  forgotPassword: (payload: ForgotPasswordPayload) => Promise<void>;
  verifyOtp: (payload: VerifyOtpPayload) => Promise<void>;
  resetPassword: (payload: ResetPasswordPayload) => Promise<void>;
  updateProfile: (updates: Partial<AuthUser>) => Promise<void>;
  updatePassword: (currentPassword: string, newPassword: string) => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

const getHomeRouteForRole = (role: AuthUser["role"]) => {
  if (role === "ADMIN") {
    return "/admin-panel";
  }

  return "/author-dashboard";
};

export function AuthProvider({ children }: AuthProviderProps) {
  const [currentUser, setCurrentUser] = useState<AuthUser | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const oauthToken = params.get("token");
    const oauthEmail = params.get("email");

    if (oauthToken && oauthEmail) {
      const oauthUser: AuthUser = {
        userId: Number(params.get("userId") ?? 0),
        username: params.get("username") ?? "",
        email: oauthEmail,
        fullName: params.get("fullName") ?? "",
        avatarUrl: params.get("avatarUrl") ?? undefined,
        role: (params.get("role") as AuthUser["role"] | null) ?? "READER",
        provider: (params.get("provider") as AuthUser["provider"] | null) ?? "GOOGLE",
        isActive: params.get("isActive") !== "false"
      };

      localStorage.setItem(TOKEN_STORAGE_KEY, oauthToken);
      localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(oauthUser));
      setCurrentUser(oauthUser);

      params.delete("token");
      params.delete("userId");
      params.delete("username");
      params.delete("email");
      params.delete("fullName");
      params.delete("role");
      params.delete("provider");
      params.delete("isActive");
      params.delete("avatarUrl");
      const cleanQuery = params.toString();
      const cleanUrl = `${window.location.pathname}${cleanQuery ? `?${cleanQuery}` : ""}${window.location.hash}`;
      window.history.replaceState({}, document.title, cleanUrl);
      setIsLoading(false);
      navigate(getHomeRouteForRole(oauthUser.role), { replace: true });
      return;
    }

    const token = localStorage.getItem(TOKEN_STORAGE_KEY);
    const storedUser = localStorage.getItem(USER_STORAGE_KEY);

    if (token && storedUser) {
      const parsedUser = JSON.parse(storedUser) as AuthUser;
      setCurrentUser(parsedUser);

      if (parsedUser.userId) {
        void apiClient
          .get<AuthUser>(`/api/v1/users/${parsedUser.userId}`)
          .then(({ data }) => {
            const mergedUser: AuthUser = {
              ...parsedUser,
              ...data,
            };
            localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(mergedUser));
            setCurrentUser(mergedUser);
          })
          .catch(() => {
            // Keep the locally cached session if the refresh call fails.
          });
      }
    }

    setIsLoading(false);
  }, []);

  const persistSession = (response: LoginResponse) => {
    localStorage.setItem(TOKEN_STORAGE_KEY, response.token);
    localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(response.user));
    setCurrentUser(response.user);
  };

  const login = async (payload: LoginPayload) => {
    const { data } = await apiClient.post<LoginResponse>("/api/v1/auth/login", payload, {
      headers: { "x-skip-toast": "true" }
    });
    persistSession(data);
  };

  const register = async (payload: RegisterPayload) => {
    await apiClient.post("/api/v1/auth/register", payload, {
      headers: { "x-skip-toast": "true" }
    });
    await login({ email: payload.email, password: payload.password });
  };

  const requestRegistrationOtp = async (payload: RegisterPayload) => {
    await apiClient.post("/api/v1/auth/register/request-otp", payload, {
      headers: { "x-skip-toast": "true" }
    });
  };

  const verifyRegistrationOtp = async (payload: VerifyOtpPayload) => {
    await apiClient.post("/api/v1/auth/register/verify-otp", payload, {
      headers: { "x-skip-toast": "true" }
    });
  };

  const logout = () => {
    localStorage.removeItem(TOKEN_STORAGE_KEY);
    localStorage.removeItem(USER_STORAGE_KEY);
    setCurrentUser(null);
  };

  const signInWithGoogle = () => {
    // Connect directly to auth-service (8081), bypassing the Gateway.
    // This avoids session/proxy issues with OAuth2 callback flows.
    const googleAuthUrl =
      import.meta.env.VITE_GOOGLE_AUTH_URL ?? "http://localhost:8080/oauth2/authorization/google";
    window.location.href = googleAuthUrl;
  };

  const forgotPassword = async (payload: ForgotPasswordPayload) => {
    await apiClient.post("/api/v1/auth/forgot-password", payload, {
      headers: { "x-skip-toast": "true" }
    });
  };

  const verifyOtp = async (payload: VerifyOtpPayload) => {
    await apiClient.post("/api/v1/auth/verify-otp", payload, {
      headers: { "x-skip-toast": "true" }
    });
  };

  const resetPassword = async (payload: ResetPasswordPayload) => {
    await apiClient.post("/api/v1/auth/reset-password", payload, {
      headers: { "x-skip-toast": "true" }
    });
  };

  const updateProfile = async (updates: Partial<AuthUser>) => {
    const { data } = await apiClient.put<AuthUser>("/api/v1/users/profile", updates);
    localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(data));
    setCurrentUser(data);
  };

  const updatePassword = async (currentPassword: string, newPassword: string) => {
    await apiClient.put("/api/v1/users/password", { currentPassword, newPassword });
  };

  const value = useMemo<AuthContextValue>(
    () => ({
      currentUser,
      isAuthenticated: Boolean(currentUser),
      isLoading,
      login,
      register,
      requestRegistrationOtp,
      verifyRegistrationOtp,
      logout,
      signInWithGoogle,
      forgotPassword,
      verifyOtp,
      resetPassword,
      updateProfile,
      updatePassword
    }),
    [currentUser, isLoading]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }

  return context;
}
