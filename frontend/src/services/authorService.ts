import axios from "axios";
import { BASE_URL } from "../api/axiosInstance";

export interface PublicAuthor {
  userId: number;
  fullName: string;
  avatarUrl?: string | null;
}

const authorRequestCache = new Map<number, Promise<PublicAuthor>>();

const publicAuthorClient = axios.create({
  baseURL: BASE_URL,
  timeout: 10000,
  headers: {
    "Content-Type": "application/json",
  },
});

const fetchAuthorById = async (authorId: number) => {
  const { data } = await publicAuthorClient.get<PublicAuthor>(`/api/v1/users/${authorId}`);
  return data;
};

const getById = (authorId: number) => {
  const cachedRequest = authorRequestCache.get(authorId);
  if (cachedRequest) {
    return cachedRequest;
  }

  const request = fetchAuthorById(authorId).catch((error) => {
    authorRequestCache.delete(authorId);
    throw error;
  });

  authorRequestCache.set(authorId, request);
  return request;
};

const getDisplayName = async (authorId: number) => {
  const author = await getById(authorId);
  return author.fullName;
};

const getAvatarUrl = async (authorId: number) => {
  const author = await getById(authorId);
  return author.avatarUrl ?? null;
};

const authorService = {
  getById,
  getDisplayName,
  getAvatarUrl,
};

export default authorService;
