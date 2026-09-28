import { FormEvent, useState } from "react";
import { useNavigate } from "react-router-dom";
import { api, ApiClientError } from "../api/client";
import { saveToken, clearToken } from "../auth/session";

export function LoginPage() {
  const navigate = useNavigate();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    try {
      const data = await api<{ token: string }>("/api/auth/login", {
        method: "POST",
        body: JSON.stringify({ username, password }),
      });
      saveToken(data.token);
      navigate("/");
    } catch (err) {
      clearToken();
      setError(err instanceof ApiClientError ? err.message : "Something went wrong. Try again.");
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-[#deebff] p-4">
      <form className="w-full max-w-sm rounded-lg bg-white p-8 shadow-lg" onSubmit={onSubmit}>
        <p className="mb-1 text-center text-xs font-semibold uppercase tracking-wider text-jira-blue">Jira-Lite</p>
        <h1 className="mb-6 text-center text-2xl font-semibold text-jira-navy">Sign in</h1>
        {error ? (
          <p className="mb-4 text-sm text-red-700" role="alert">
            {error}
          </p>
        ) : null}
        <label className="mb-4 block text-sm font-medium text-jira-navy">
          Username
          <input
            className="mt-1 w-full rounded border border-jira-border px-3 py-2"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            autoComplete="username"
          />
        </label>
        <label className="mb-4 block text-sm font-medium text-jira-navy">
          Password
          <input
            className="mt-1 w-full rounded border border-jira-border px-3 py-2"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="current-password"
          />
        </label>
        <button
          type="submit"
          className="w-full rounded bg-jira-blue py-2.5 text-sm font-semibold text-white hover:bg-jira-blue-hover"
        >
          Sign in
        </button>
        <p className="mt-4 text-center text-xs text-jira-muted">Local seed: username alice, password password</p>
      </form>
    </div>
  );
}
