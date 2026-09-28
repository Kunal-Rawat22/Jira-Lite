import { FormEvent, useEffect, useState } from "react";
import { api, ApiClientError } from "../api/client";

type Comment = { id: string; body: string; authorId: string; createdAt: string };

export function CommentList({ ticketId, frozen }: { ticketId: string; frozen: boolean }) {
  const [comments, setComments] = useState<Comment[]>([]);
  const [body, setBody] = useState("");
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    void api<Comment[]>(`/api/tickets/${ticketId}/comments`).then(setComments);
  }, [ticketId]);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    if (!body.trim()) {
      setError("Comment cannot be empty");
      return;
    }
    try {
      const created = await api<Comment>(`/api/tickets/${ticketId}/comments`, {
        method: "POST",
        body: JSON.stringify({ body }),
      });
      setComments((c) => [...c, created]);
      setBody("");
    } catch (err) {
      setError(err instanceof ApiClientError ? err.message : "Something went wrong. Try again.");
    }
  }

  return (
    <div>
      <ul className="mb-4 space-y-2">
        {comments.map((c) => (
          <li key={c.id} className="rounded border border-jira-border p-3 text-sm">
            {c.body}
          </li>
        ))}
      </ul>
      {frozen ? null : (
        <form onSubmit={onSubmit}>
          {error ? (
            <p className="mb-2 text-sm text-red-700" role="alert">
              {error}
            </p>
          ) : null}
          <textarea
            className="mb-2 w-full rounded border border-jira-border px-3 py-2 text-sm"
            value={body}
            onChange={(e) => setBody(e.target.value)}
          />
          <button type="submit" className="rounded bg-jira-blue px-3 py-1.5 text-sm font-semibold text-white">
            Comment
          </button>
        </form>
      )}
    </div>
  );
}
