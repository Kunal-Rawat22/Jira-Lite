import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { StatusControl } from "./StatusControl";

test("illegal status does not call onChange", async () => {
  const onChange = vi.fn();
  render(<StatusControl status="CLOSED" version={1} onChange={onChange} />);
  await userEvent.selectOptions(screen.getByLabelText("status"), "OPEN");
  expect(onChange).not.toHaveBeenCalled();
  expect(screen.getByRole("alert")).toBeInTheDocument();
});

test("allowed status calls onChange", async () => {
  const onChange = vi.fn().mockResolvedValue(undefined);
  render(<StatusControl status="OPEN" version={1} onChange={onChange} />);
  await userEvent.selectOptions(screen.getByLabelText("status"), "IN_PROGRESS");
  expect(onChange).toHaveBeenCalledWith("IN_PROGRESS");
});
