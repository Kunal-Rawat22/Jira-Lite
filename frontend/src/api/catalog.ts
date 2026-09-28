import { api } from "./client";

export type CatalogUser = { id: string; username: string; displayName: string };
export type CatalogProduct = { id: string; name: string };

export function listUsers() {
  return api<CatalogUser[]>("/api/users");
}

export function listProducts() {
  return api<CatalogProduct[]>("/api/products");
}
