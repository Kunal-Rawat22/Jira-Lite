import { api } from "./client";

export type Ticket = {
  id: string;
  productId: string;
  title: string;
  description: string | null;
  status: string;
  priority: string;
  reporterId: string;
  assigneeId: string;
  version: number;
  createdAt: string;
  updatedAt: string;
};

export type Page<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type ListBody = {
  searchKey: string | null;
  status: string[];
  assignee: string[];
  reporter: string[];
  product: string[];
  user: string[];
  size: number;
  page: number;
};

export function listTickets(body: ListBody) {
  return api<Page<Ticket>>("/api/tickets/list", { method: "POST", body: JSON.stringify(body) });
}
