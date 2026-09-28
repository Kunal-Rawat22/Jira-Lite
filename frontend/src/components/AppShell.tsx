import { ReactNode, useState } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import { clearToken } from "../auth/session";

export function AppShell({ children }: { children: ReactNode }) {
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);

  function logout() {
    clearToken();
    navigate("/login");
  }

  const linkClass = ({ isActive }: { isActive: boolean }) =>
    `block rounded px-3 py-2 text-sm font-medium ${
      isActive ? "bg-white/15 text-white" : "text-slate-200 hover:bg-white/10"
    }`;

  return (
    <div className="min-h-screen bg-jira-canvas">
      <header className="flex h-14 items-center gap-3 bg-jira-navy px-4 text-white">
        <button
          type="button"
          className="rounded p-2 md:hidden"
          aria-label="Open navigation"
          onClick={() => setOpen((v) => !v)}
        >
          Menu
        </button>
        <span className="text-sm font-semibold tracking-wide">Jira-Lite</span>
        <div className="ml-auto">
          <button
            type="button"
            className="rounded px-3 py-1.5 text-sm hover:bg-white/10"
            onClick={logout}
          >
            Log out
          </button>
        </div>
      </header>
      <div className="flex">
        {open ? (
          <button
            type="button"
            className="fixed inset-0 z-20 bg-black/40 md:hidden"
            aria-label="Close navigation"
            onClick={() => setOpen(false)}
          />
        ) : null}
        <nav
          className={`fixed z-30 h-[calc(100vh-3.5rem)] w-56 bg-jira-navy p-3 md:static md:block ${
            open ? "block" : "hidden md:block"
          }`}
        >
          <NavLink to="/" className={linkClass} end onClick={() => setOpen(false)}>
            Issues
          </NavLink>
          <NavLink to="/tickets/new" className={linkClass} onClick={() => setOpen(false)}>
            Create
          </NavLink>
        </nav>
        <main className="min-w-0 flex-1 p-4 md:p-6">{children}</main>
      </div>
    </div>
  );
}
