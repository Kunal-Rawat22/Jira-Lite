import { ApiClientError, api } from "./client";

test("unexpected errors keep a generic message", async () => {
  vi.stubGlobal("fetch", vi.fn().mockRejectedValue(new Error("network")));
  await expect(api("/api/x")).rejects.toBeInstanceOf(ApiClientError);
});
