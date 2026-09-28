import { CatalogProduct, CatalogUser } from "../api/catalog";

const STATUSES = ["OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED", "CANCELLED", "REOPEN"];

export type TicketFilterState = {
  status: string[];
  assignee: string[];
  reporter: string[];
  product: string[];
  user: string[];
};

export function TicketFilters({
  users,
  products,
  value,
  onChange,
}: {
  users: CatalogUser[];
  products: CatalogProduct[];
  value: TicketFilterState;
  onChange: (next: TicketFilterState) => void;
}) {
  function toggle(key: keyof TicketFilterState, id: string) {
    const current = value[key];
    const next = current.includes(id) ? current.filter((x) => x !== id) : [...current, id];
    onChange({ ...value, [key]: next });
  }

  return (
    <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-5">
      <FilterGroup label="Status" values={STATUSES} selected={value.status} onToggle={(v) => toggle("status", v)} />
      <FilterGroup
        label="Assignee"
        values={users.map((u) => u.id)}
        labels={Object.fromEntries(users.map((u) => [u.id, u.displayName]))}
        selected={value.assignee}
        onToggle={(v) => toggle("assignee", v)}
      />
      <FilterGroup
        label="Reporter"
        values={users.map((u) => u.id)}
        labels={Object.fromEntries(users.map((u) => [u.id, u.displayName]))}
        selected={value.reporter}
        onToggle={(v) => toggle("reporter", v)}
      />
      <FilterGroup
        label="User"
        values={users.map((u) => u.id)}
        labels={Object.fromEntries(users.map((u) => [u.id, u.displayName]))}
        selected={value.user}
        onToggle={(v) => toggle("user", v)}
      />
      <FilterGroup
        label="Product"
        values={products.map((p) => p.id)}
        labels={Object.fromEntries(products.map((p) => [p.id, p.name]))}
        selected={value.product}
        onToggle={(v) => toggle("product", v)}
      />
    </div>
  );
}

function FilterGroup({
  label,
  values,
  labels,
  selected,
  onToggle,
}: {
  label: string;
  values: string[];
  labels?: Record<string, string>;
  selected: string[];
  onToggle: (value: string) => void;
}) {
  return (
    <fieldset className="rounded border border-jira-border bg-white p-2">
      <legend className="px-1 text-xs font-semibold text-jira-muted">{label}</legend>
      <div className="max-h-32 space-y-1 overflow-auto text-sm">
        {values.map((v) => (
          <label key={v} className="flex items-center gap-2">
            <input type="checkbox" checked={selected.includes(v)} onChange={() => onToggle(v)} />
            <span>{labels?.[v] ?? v.replaceAll("_", " ")}</span>
          </label>
        ))}
      </div>
    </fieldset>
  );
}
