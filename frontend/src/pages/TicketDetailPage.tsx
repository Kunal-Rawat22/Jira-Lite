import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { api, ApiClientError } from "../api/client";
import { Ticket } from "../api/tickets";
import { CatalogProduct, CatalogUser, listProducts, listUsers } from "../api/catalog";
import { StatusControl } from "../components/StatusControl";
import { TicketEditForm } from "./TicketEditForm";
import { CommentList } from "../components/CommentList";
import { CommentsActivityTabs } from "../components/CommentsActivityTabs";
import { PriorityLozenge, StatusLozenge } from "../components/Lozenge";

export function TicketDetailPage() {
  const { id } = useParams();
  const [ticket, setTicket] = useState<Ticket | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [users, setUsers] = useState<CatalogUser[]>([]);
  const [products, setProducts] = useState<CatalogProduct[]>([]);

  async function reload() {
    if (!id) {
      return;
    }
    try {
      setTicket(await api<Ticket>(`/api/tickets/${id}`));
      setError(null);
    } catch (err) {
      setTicket(null);
      setError(err instanceof ApiClientError ? err.message : "Something went wrong. Try again.");
    }
  }

  useEffect(() => {
    void reload();
  }, [id]);

  useEffect(() => {
    void listUsers().then(setUsers);
    void listProducts().then(setProducts);
  }, []);

  if (error && !ticket) {
    return (
      <p className="text-sm text-red-700" role="alert">
        {error}
      </p>
    );
  }
  if (!ticket) {
    return <p className="text-sm text-jira-muted">Loading</p>;
  }
  const frozen = ticket.status === "CLOSED" || ticket.status === "CANCELLED";
  const userName = Object.fromEntries(users.map((u) => [u.id, u.displayName]));
  const productName = Object.fromEntries(products.map((p) => [p.id, p.name]));

  async function changeStatus(next: string) {
    try {
      setTicket(
        await api<Ticket>(`/api/tickets/${ticket.id}/status`, {
          method: "POST",
          body: JSON.stringify({ version: ticket.version, status: next }),
        }),
      );
    } catch (err) {
      setError(err instanceof ApiClientError ? err.message : "Something went wrong. Try again.");
    }
  }

  return (
    <div>
      <Link to="/" className="mb-3 inline-block text-sm text-jira-blue hover:underline">
        Back to issues
      </Link>
      <div className="flex flex-col gap-6 lg:flex-row">
        <div className="min-w-0 flex-1 rounded border border-jira-border bg-white p-5">
          <h1 className="mb-2 text-2xl font-semibold text-jira-navy">{ticket.title}</h1>
          {error ? (
            <p className="mb-3 text-sm text-red-700" role="alert">
              {error}
            </p>
          ) : null}
          <p className="mb-4 whitespace-pre-wrap text-sm text-slate-700">{ticket.description}</p>
          {frozen ? null : <TicketEditForm ticket={ticket} onSaved={setTicket} onReload={reload} />}
          <div className="mt-6">
            <CommentsActivityTabs
              comments={<CommentList ticketId={ticket.id} frozen={frozen} />}
              activity={<ActivityPanel ticketId={ticket.id} />}
            />
          </div>
        </div>
        <aside className="w-full shrink-0 space-y-3 rounded border border-jira-border bg-white p-4 lg:w-72">
          <div>
            <p className="text-xs font-semibold uppercase text-jira-muted">Status</p>
            <div className="mt-1">
              <StatusLozenge status={ticket.status} />
            </div>
            <div className="mt-2">
              <StatusControl status={ticket.status} version={ticket.version} onChange={changeStatus} />
            </div>
          </div>
          <div>
            <p className="text-xs font-semibold uppercase text-jira-muted">Priority</p>
            <PriorityLozenge priority={ticket.priority} />
          </div>
          <p className="text-sm">
            <span className="text-jira-muted">Assignee</span>
            <br />
            {userName[ticket.assigneeId] ?? ticket.assigneeId}
          </p>
          <p className="text-sm">
            <span className="text-jira-muted">Reporter</span>
            <br />
            {userName[ticket.reporterId] ?? ticket.reporterId}
          </p>
          <p className="text-sm">
            <span className="text-jira-muted">Product</span>
            <br />
            {productName[ticket.productId] ?? ticket.productId}
          </p>
          <p className="text-sm">
            <span className="text-jira-muted">Version</span>
            <br />v{ticket.version}
          </p>
          <p className="text-xs text-jira-muted">Created {ticket.createdAt}</p>
          <p className="text-xs text-jira-muted">Updated {ticket.updatedAt}</p>
        </aside>
      </div>
    </div>
  );
}

function ActivityPanel({ ticketId }: { ticketId: string }) {
  const [rows, setRows] = useState<{ from: Record<string, unknown>; to: Record<string, unknown> }[]>([]);
  useEffect(() => {
    void api<typeof rows>(`/api/tickets/${ticketId}/activity`).then(setRows);
  }, [ticketId]);
  return (
    <ul className="space-y-2 text-sm">
      {rows.map((r, i) => (
        <li key={i} className="rounded bg-slate-50 p-2">
          {JSON.stringify(r.from)} → {JSON.stringify(r.to)}
        </li>
      ))}
    </ul>
  );
}
