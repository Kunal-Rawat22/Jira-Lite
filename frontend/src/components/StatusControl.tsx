import { useState } from "react";

const ALLOWED: Record<string, string[]> = {
  OPEN: ["IN_PROGRESS", "CANCELLED"],
  IN_PROGRESS: ["RESOLVED", "CANCELLED"],
  RESOLVED: ["CLOSED", "REOPEN"],
  CLOSED: ["REOPEN"],
  CANCELLED: ["REOPEN"],
  REOPEN: ["IN_PROGRESS", "CANCELLED"],
};

export function isAllowedTransition(from: string, to: string) {
  return (ALLOWED[from] ?? []).includes(to);
}

export function StatusControl({
  status,
  version,
  onChange,
}: {
  status: string;
  version: number;
  onChange: (next: string) => Promise<void>;
}) {
  const options = ["OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED", "CANCELLED", "REOPEN"];
  const [error, setError] = useState<string | null>(null);

  return (
    <div>
      <label className="block text-sm">
        Status
        <select
          aria-label="status"
          className="mt-1 w-full rounded border border-jira-border bg-white px-2 py-1.5 text-sm"
          value={status}
          onChange={async (e) => {
            const next = e.target.value;
            if (!isAllowedTransition(status, next)) {
              setError("That status change is not allowed");
              return;
            }
            setError(null);
            await onChange(next);
          }}
        >
          {options.map((s) => (
            <option key={s} value={s}>
              {s}
            </option>
          ))}
        </select>
      </label>
      {error ? (
        <p className="mt-1 text-sm text-red-700" role="alert">
          {error}
        </p>
      ) : null}
      <span data-version={version} />
    </div>
  );
}
