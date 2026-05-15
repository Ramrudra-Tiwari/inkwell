import axios from "axios";

export const TOKEN_STORAGE_KEY = "inkwell_token";
export const USER_STORAGE_KEY = "inkwell_user";

const configuredBaseUrl = import.meta.env.VITE_API_BASE_URL;
export const BASE_URL = configuredBaseUrl === undefined ? "http://localhost:8080" : configuredBaseUrl;
const axiosInstance = axios.create({
  baseURL: BASE_URL,
  timeout: 10000,
  headers: {
    "Content-Type": "application/json"
  }
});

axiosInstance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem(TOKEN_STORAGE_KEY);

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error) => Promise.reject(error)
);

export default axiosInstance;
