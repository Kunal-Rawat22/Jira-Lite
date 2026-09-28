const STATUS: Record<string, string> = {
  OPEN: "bg-blue-100 text-blue-800",
  IN_PROGRESS: "bg-sky-100 text-sky-800",
  RESOLVED: "bg-emerald-100 text-emerald-800",
  CLOSED: "bg-slate-200 text-slate-700",
  CANCELLED: "bg-rose-100 text-rose-800",
  REOPEN: "bg-amber-100 text-amber-800",
};

const PRIORITY: Record<string, string> = {
  LOW: "bg-slate-100 text-slate-700",
  MEDIUM: "bg-yellow-100 text-yellow-800",
  HIGH: "bg-red-100 text-red-800",
};

export function StatusLozenge({ status }: { status: string }) {
  return (
    <span className={`inline-flex rounded px-2 py-0.5 text-xs font-semibold ${STATUS[status] ?? "bg-slate-100"}`}>
      {status.replaceAll("_", " ")}
    </span>
  );
}

export function PriorityLozenge({ priority }: { priority: string }) {
  return (
    <span className={`inline-flex rounded px-2 py-0.5 text-xs font-semibold ${PRIORITY[priority] ?? "bg-slate-100"}`}>
      {priority}
    </span>
  );
}
