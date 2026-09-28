import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import { TicketCreatePage } from "./TicketCreatePage";

test("empty title does not call create", async () => {
  const fetchMock = vi.fn().mockResolvedValue({
    json: async () => ({ status: "success", data: [{ id: "p1", name: "Support" }], code: "SUCCESS", message: "OK" }),
  });
  vi.stubGlobal("fetch", fetchMock);
  render(
    <MemoryRouter>
      <TicketCreatePage />
    </MemoryRouter>,
  );
  await screen.findByText("Create ticket");
  fetchMock.mockClear();
  await userEvent.click(screen.getByRole("button", { name: "Create" }));
  expect(fetchMock).not.toHaveBeenCalled();
});
