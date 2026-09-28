import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { CommentList } from "../components/CommentList";

test("empty comment does not post", async () => {
  const fetchMock = vi.fn().mockResolvedValue({
    json: async () => ({ status: "success", data: [], code: "SUCCESS", message: "OK" }),
  });
  vi.stubGlobal("fetch", fetchMock);
  render(<CommentList ticketId="t1" frozen={false} />);
  await screen.findByRole("button", { name: "Comment" });
  fetchMock.mockClear();
  await userEvent.click(screen.getByRole("button", { name: "Comment" }));
  expect(fetchMock).not.toHaveBeenCalled();
});
