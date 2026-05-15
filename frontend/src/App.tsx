import { lazy, Suspense } from "react";
import { Route, Routes } from "react-router-dom";
import Footer from "./components/Footer";
import Navbar from "./components/Navbar";
import PrivateRoute from "./components/PrivateRoute";

const Login = lazy(() => import("./pages/Login"));
const Register = lazy(() => import("./pages/Register"));
const PostFeed = lazy(() => import("./pages/PostFeed"));
const PostDetail = lazy(() => import("./pages/PostDetail"));
const AuthorPublicProfile = lazy(() => import("./pages/AuthorPublicProfile"));
const PostEditor = lazy(() => import("./pages/PostEditor"));
const CategoryResultPage = lazy(() => import("./pages/CategoryResultPage"));
const ConfirmSubscription = lazy(() => import("./pages/ConfirmSubscription"));
const Unsubscribe = lazy(() => import("./pages/Unsubscribe"));
const AuthorDashboard = lazy(() => import("./pages/AuthorDashboard"));
const AdminDashboard = lazy(() => import("./pages/AdminDashboard"));
const SearchResultsPage = lazy(() => import("./pages/SearchResultsPage"));
const ProfileView = lazy(() => import("./pages/ProfileView"));
const ForgotPassword = lazy(() => import("./pages/ForgotPassword"));
const CheckoutButton = lazy(() => import("./components/CheckoutButton"));

function App() {
  return (
    <div className="flex min-h-screen flex-col bg-canvas text-body transition-colors duration-300">
      <Navbar />
      <main className="flex-1">
        <Suspense
          fallback={
            <div className="flex min-h-[40vh] items-center justify-center px-4 text-muted">
              Loading…
            </div>
          }
        >
          <Routes>
            <Route path="/" element={<PostFeed />} />
            <Route path="/blog/:slug" element={<PostDetail />} />
            <Route path="/author/:userId" element={<AuthorPublicProfile />} />
            <Route path="/category/:slug" element={<CategoryResultPage />} />
            <Route path="/tag/:slug" element={<CategoryResultPage />} />
            <Route path="/search" element={<SearchResultsPage />} />
            <Route path="/confirm-subscription" element={<ConfirmSubscription />} />
            <Route path="/unsubscribe" element={<Unsubscribe />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/forgot-password" element={<ForgotPassword />} />
            <Route path="/checkout" element={<CheckoutButton />} />

            <Route element={<PrivateRoute />}>
              <Route path="/profile" element={<ProfileView />} />
            </Route>

            <Route element={<PrivateRoute allowedRoles={["READER", "AUTHOR", "ADMIN"]} />}>
              <Route path="/author-dashboard" element={<AuthorDashboard />} />
              <Route path="/author/posts/new" element={<PostEditor />} />
              <Route path="/author/posts/:postId/edit" element={<PostEditor />} />
            </Route>

            <Route element={<PrivateRoute allowedRoles={["ADMIN"]} />}>
              <Route path="/admin-panel" element={<AdminDashboard />} />
            </Route>

            <Route path="*" element={<div className="flex min-h-[40vh] items-center justify-center text-muted text-xl font-bold">404 - Page Not Found</div>} />
          </Routes>
        </Suspense>
      </main>
      <Footer />
    </div>
  );
}

export default App;
