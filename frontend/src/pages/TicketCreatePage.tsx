import { FormEvent, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { api, ApiClientError } from "../api/client";
import { CatalogProduct, CatalogUser, listProducts, listUsers } from "../api/catalog";

export function TicketCreatePage() {
  const navigate = useNavigate();
  const [products, setProducts] = useState<CatalogProduct[]>([]);
  const [users, setUsers] = useState<CatalogUser[]>([]);
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [priority, setPriority] = useState("MEDIUM");
  const [productId, setProductId] = useState("");
  const [assigneeId, setAssigneeId] = useState("");
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    void listProducts().then(setProducts);
    void listUsers().then(setUsers);
  }, []);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    if (!title.trim() || title.length > 40 || description.length > 1000) {
      setError("Check title and description");
      return;
    }
    if (products.length > 1 && !productId) {
      setError("Choose a product");
      return;
    }
    if (products.length === 0) {
      setError("You need a product membership to create tickets");
      return;
    }
    try {
      const created = await api<{ id: string }>("/api/tickets", {
        method: "POST",
        body: JSON.stringify({
          title,
          description: description || null,
          priority,
          productId: products.length === 1 ? null : productId,
          assigneeId: assigneeId || null,
        }),
      });
      navigate(`/tickets/${created.id}`);
    } catch (err) {
      setError(err instanceof ApiClientError ? err.message : "Something went wrong. Try again.");
    }
  }

  const field = "mt-1 w-full rounded border border-jira-border bg-white px-3 py-2 text-sm";

  return (
    <form className="mx-auto max-w-2xl rounded border border-jira-border bg-white p-6" onSubmit={onSubmit}>
      <h1 className="mb-4 text-xl font-semibold text-jira-navy">Create ticket</h1>
      {error ? (
        <p className="mb-3 text-sm text-red-700" role="alert">
          {error}
        </p>
      ) : null}
      <label className="mb-3 block text-sm font-medium">
        Title
        <input className={field} value={title} onChange={(e) => setTitle(e.target.value)} />
      </label>
      <label className="mb-3 block text-sm font-medium">
        Description
        <textarea className={`${field} min-h-28`} value={description} onChange={(e) => setDescription(e.target.value)} />
      </label>
      <label className="mb-3 block text-sm font-medium">
        Priority
        <select className={field} value={priority} onChange={(e) => setPriority(e.target.value)}>
          <option>LOW</option>
          <option>MEDIUM</option>
          <option>HIGH</option>
        </select>
      </label>
      <label className="mb-3 block text-sm font-medium">
        Assignee
        <select className={field} value={assigneeId} onChange={(e) => setAssigneeId(e.target.value)}>
          <option value="">Assign to me (reporter)</option>
          {users.map((u) => (
            <option key={u.id} value={u.id}>
              {u.displayName}
            </option>
          ))}
        </select>
      </label>
      {products.length > 1 ? (
        <label className="mb-3 block text-sm font-medium">
          Product
          <select className={field} value={productId} onChange={(e) => setProductId(e.target.value)}>
            <option value="">Select</option>
            {products.map((p) => (
              <option key={p.id} value={p.id}>
                {p.name}
              </option>
            ))}
          </select>
        </label>
      ) : null}
      <button
        type="submit"
        disabled={products.length === 0}
        className="rounded bg-jira-blue px-4 py-2 text-sm font-semibold text-white hover:bg-jira-blue-hover disabled:opacity-50"
      >
        Create
      </button>
    </form>
  );
}
