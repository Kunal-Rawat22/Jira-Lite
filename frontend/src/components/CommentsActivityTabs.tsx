import { ReactNode, useState } from "react";

export function CommentsActivityTabs({ comments, activity }: { comments: ReactNode; activity: ReactNode }) {
  const [tab, setTab] = useState<"comments" | "activity">("comments");
  const tabClass = (active: boolean) =>
    `border-b-2 px-3 py-2 text-sm font-medium ${
      active ? "border-jira-blue text-jira-blue" : "border-transparent text-jira-muted"
    }`;
  return (
    <div>
      <div className="mb-3 flex gap-1 border-b border-jira-border">
        <button type="button" className={tabClass(tab === "comments")} onClick={() => setTab("comments")}>
          Comments
        </button>
        <button type="button" className={tabClass(tab === "activity")} onClick={() => setTab("activity")}>
          Activity
        </button>
      </div>
      {tab === "comments" ? comments : activity}
    </div>
  );
}
