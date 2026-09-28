import { ReactNode } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { isSignedIn } from "./auth/session";
import { AppShell } from "./components/AppShell";
import { LoginPage } from "./pages/LoginPage";
import { TicketListPage } from "./pages/TicketListPage";
import { TicketDetailPage } from "./pages/TicketDetailPage";
import { TicketCreatePage } from "./pages/TicketCreatePage";

function RequireAuth({ children }: { children: ReactNode }) {
  if (!isSignedIn()) {
    return <Navigate to="/login" replace />;
  }
  return <AppShell>{children}</AppShell>;
}

export function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route
        path="/"
        element={
          <RequireAuth>
            <TicketListPage />
          </RequireAuth>
        }
      />
      <Route
        path="/tickets/new"
        element={
          <RequireAuth>
            <TicketCreatePage />
          </RequireAuth>
        }
      />
      <Route
        path="/tickets/:id"
        element={
          <RequireAuth>
            <TicketDetailPage />
          </RequireAuth>
        }
      />
    </Routes>
  );
}
