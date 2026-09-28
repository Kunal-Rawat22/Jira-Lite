import { FormEvent, useEffect, useState } from "react";
import { api, ApiClientError } from "../api/client";
import { Ticket } from "../api/tickets";
import { CatalogUser, listUsers } from "../api/catalog";

export function TicketEditForm({
  ticket,
  onSaved,
  onReload,
}: {
  ticket: Ticket;
  onSaved: (t: Ticket) => void;
  onReload: () => Promise<void>;
}) {
  const [title, setTitle] = useState(ticket.title);
  const [description, setDescription] = useState(ticket.description ?? "");
  const [priority, setPriority] = useState(ticket.priority);
  const [assigneeId, setAssigneeId] = useState(ticket.assigneeId);
  const [users, setUsers] = useState<CatalogUser[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    void listUsers().then(setUsers);
  }, []);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    if (!title.trim()) {
      setError("Title is required");
      return;
    }
    try {
      const saved = await api<Ticket>(`/api/tickets/${ticket.id}`, {
        method: "PATCH",
        body: JSON.stringify({ version: ticket.version, title, description, priority, assigneeId }),
      });
      onSaved(saved);
    } catch (err) {
      if (err instanceof ApiClientError && err.code === "STALE_VERSION") {
        setError(err.message);
        await onReload();
        return;
      }
      setError(err instanceof ApiClientError ? err.message : "Something went wrong. Try again.");
    }
  }

  const field = "mt-1 w-full rounded border border-jira-border bg-white px-3 py-2 text-sm";

  return (
    <form className="space-y-3" onSubmit={onSubmit}>
      {error ? (
        <p className="text-sm text-red-700" role="alert">
          {error}
        </p>
      ) : null}
      <label className="block text-sm font-medium">
        Title
        <input className={field} value={title} onChange={(e) => setTitle(e.target.value)} />
      </label>
      <label className="block text-sm font-medium">
        Description
        <textarea className={`${field} min-h-28`} value={description} onChange={(e) => setDescription(e.target.value)} />
      </label>
      <label className="block text-sm font-medium">
        Priority
        <select className={field} value={priority} onChange={(e) => setPriority(e.target.value)}>
          <option>LOW</option>
          <option>MEDIUM</option>
          <option>HIGH</option>
        </select>
      </label>
      <label className="block text-sm font-medium">
        Assignee
        <select className={field} value={assigneeId} onChange={(e) => setAssigneeId(e.target.value)}>
          {users.map((u) => (
            <option key={u.id} value={u.id}>
              {u.displayName}
            </option>
          ))}
        </select>
      </label>
      <button type="submit" className="rounded bg-jira-blue px-4 py-2 text-sm font-semibold text-white hover:bg-jira-blue-hover">
        Save
      </button>
    </form>
  );
}
