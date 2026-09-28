import { render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { TicketListPage } from "./TicketListPage";

test("default list request is page 0 size 20", async () => {
  const fetchMock = vi.fn().mockImplementation(async (url: string, init?: RequestInit) => {
    if (typeof url === "string" && url.includes("/api/tickets/list")) {
      expect(JSON.parse(String(init?.body))).toMatchObject({
        searchKey: null,
        page: 0,
        size: 20,
        reporter: [],
      });
      return {
        json: async () => ({
          status: "success",
          code: "SUCCESS",
          message: "OK",
          data: { content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 },
        }),
      };
    }
    return {
      json: async () => ({ status: "success", code: "SUCCESS", message: "OK", data: [] }),
    };
  });
  vi.stubGlobal("fetch", fetchMock);
  render(
    <MemoryRouter>
      <TicketListPage />
    </MemoryRouter>,
  );
  await waitFor(() => expect(fetchMock).toHaveBeenCalled());
  expect(screen.getByText("Page 1")).toBeInTheDocument();
});
