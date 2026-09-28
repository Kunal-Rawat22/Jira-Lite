import { FormEvent, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { listTickets, Ticket } from "../api/tickets";
import { ApiClientError } from "../api/client";
import { CatalogProduct, CatalogUser, listProducts, listUsers } from "../api/catalog";
import { TicketFilterState, TicketFilters } from "../components/TicketFilters";
import { PriorityLozenge, StatusLozenge } from "../components/Lozenge";

const PAGE_SIZES = [10, 20, 50];
const EMPTY_FILTERS: TicketFilterState = {
  status: [],
  assignee: [],
  reporter: [],
  product: [],
  user: [],
};

export function TicketListPage() {
  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [searchKey, setSearchKey] = useState<string | null>(null);
  const [filters, setFilters] = useState<TicketFilterState>(EMPTY_FILTERS);
  const [users, setUsers] = useState<CatalogUser[]>([]);
  const [products, setProducts] = useState<CatalogProduct[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [empty, setEmpty] = useState(false);

  async function load(
    nextPage = page,
    nextSize = size,
    key: string | null = searchKey,
    nextFilters = filters,
  ) {
    try {
      const data = await listTickets({
        searchKey: key,
        status: nextFilters.status,
        assignee: nextFilters.assignee,
        reporter: nextFilters.reporter,
        product: nextFilters.product,
        user: nextFilters.user,
        page: nextPage,
        size: nextSize,
      });
      setTickets(data.content);
      setEmpty(data.content.length === 0);
      setError(null);
    } catch (err) {
      setError(err instanceof ApiClientError ? err.message : "Something went wrong. Try again.");
    }
  }

  useEffect(() => {
    void listUsers().then(setUsers);
    void listProducts().then(setProducts);
  }, []);

  useEffect(() => {
    void load();
  }, []);

  function onSearch(e: FormEvent) {
    e.preventDefault();
    const form = e.target as HTMLFormElement;
    const q = new FormData(form).get("q");
    const value = typeof q === "string" ? q : "";
    const key = value.length === 0 ? null : value;
    setSearchKey(key);
    setPage(0);
    void load(0, size, key, filters);
  }

  function onFilters(next: TicketFilterState) {
    setFilters(next);
    setPage(0);
    void load(0, size, searchKey, next);
  }

  const userName = Object.fromEntries(users.map((u) => [u.id, u.displayName]));
  const productName = Object.fromEntries(products.map((p) => [p.id, p.name]));

  return (
    <div>
      <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
        <h1 className="text-xl font-semibold text-jira-navy">Issues</h1>
        <Link
          to="/tickets/new"
          className="rounded bg-jira-blue px-3 py-2 text-sm font-semibold text-white hover:bg-jira-blue-hover"
        >
          Create
        </Link>
      </div>
      {error ? (
        <p className="mb-3 text-sm text-red-700" role="alert">
          {error}
        </p>
      ) : null}
      <form className="mb-4 flex gap-2" onSubmit={onSearch}>
        <input
          name="q"
          aria-label="search"
          className="min-w-0 flex-1 rounded border border-jira-border bg-white px-3 py-2 text-sm"
          placeholder="Search title or description"
        />
        <button type="submit" className="rounded bg-white px-3 py-2 text-sm font-medium ring-1 ring-jira-border">
          Search
        </button>
      </form>
      <div className="mb-4">
        <TicketFilters users={users} products={products} value={filters} onChange={onFilters} />
      </div>
      <div className="overflow-x-auto rounded border border-jira-border bg-white">
        <table className="min-w-full text-left text-sm">
          <thead className="bg-slate-50 text-xs uppercase text-jira-muted">
            <tr>
              <th className="px-3 py-2">Title</th>
              <th className="px-3 py-2">Status</th>
              <th className="px-3 py-2">Priority</th>
              <th className="px-3 py-2">Assignee</th>
              <th className="px-3 py-2">Reporter</th>
              <th className="px-3 py-2">Version</th>
              <th className="px-3 py-2">Product</th>
            </tr>
          </thead>
          <tbody>
            {tickets.map((t) => (
              <tr key={t.id} className="border-t border-jira-border hover:bg-slate-50">
                <td className="px-3 py-2">
                  <Link className="font-medium text-jira-blue hover:underline" to={`/tickets/${t.id}`}>
                    {t.title}
                  </Link>
                </td>
                <td className="px-3 py-2">
                  <StatusLozenge status={t.status} />
                </td>
                <td className="px-3 py-2">
                  <PriorityLozenge priority={t.priority} />
                </td>
                <td className="px-3 py-2">{userName[t.assigneeId] ?? t.assigneeId}</td>
                <td className="px-3 py-2">{userName[t.reporterId] ?? t.reporterId}</td>
                <td className="px-3 py-2">{t.version}</td>
                <td className="px-3 py-2">{productName[t.productId] ?? t.productId}</td>
              </tr>
            ))}
          </tbody>
        </table>
        {empty ? <p className="px-3 py-6 text-center text-sm text-jira-muted">No issues match these filters.</p> : null}
      </div>
      <div className="mt-3 flex justify-end gap-3 text-sm">
        <span>Page {page + 1}</span>
        <select
          aria-label="page size"
          className="rounded border border-jira-border bg-white px-2 py-1"
          value={size}
          onChange={(e) => {
            const next = Number(e.target.value);
            setSize(next);
            setPage(0);
            void load(0, next, searchKey, filters);
          }}
        >
          {PAGE_SIZES.map((s) => (
            <option key={s} value={s}>
              {s}
            </option>
          ))}
        </select>
      </div>
    </div>
  );
}
