import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import { LoginPage } from "../pages/LoginPage";

test("invalid login shows error and does not store token", async () => {
  vi.stubGlobal(
    "fetch",
    vi.fn().mockResolvedValue({
      json: async () => ({
        message: "Authentication failed",
        code: "AUTHENTICATION_FAILED",
        status: "failed",
        data: null,
      }),
    }),
  );
  render(
    <MemoryRouter>
      <LoginPage />
    </MemoryRouter>,
  );
  await userEvent.type(screen.getByLabelText("Username"), "alice");
  await userEvent.type(screen.getByLabelText("Password"), "bad");
  await userEvent.click(screen.getByRole("button", { name: "Sign in" }));
  expect(await screen.findByRole("alert")).toBeInTheDocument();
  expect(sessionStorage.getItem("jwt")).toBeNull();
});
