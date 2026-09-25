import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import App from './App';
import * as api from './api';

vi.mock('./api', () => ({
  getColumns: vi.fn(),
  createTask: vi.fn(),
  deleteTask: vi.fn(),
  moveTask: vi.fn(),
  updateTask: vi.fn()
}));

const COLUMNS = [
  { id: 1, name: 'To Do', position: 0, tasks: [{ id: 10, title: 'Write tests', description: 'Vitest + RTL', position: 0, columnId: 1 }] },
  { id: 2, name: 'In Progress', position: 1, tasks: [] },
  { id: 3, name: 'Done', position: 2, tasks: [] }
];

function column(name) {
  return screen.getByRole('heading', { name: new RegExp(name) }).parentElement;
}

describe('App', () => {
  beforeEach(() => {
    vi.resetAllMocks();
    api.getColumns.mockResolvedValue(COLUMNS);
  });

  it('shows a loading state and then renders the board', async () => {
    render(<App />);

    expect(screen.getByText('Loading board...')).toBeInTheDocument();
    expect(await screen.findByText('Write tests')).toBeInTheDocument();
    expect(screen.getByText('Vitest + RTL')).toBeInTheDocument();
    expect(screen.getAllByRole('heading', { level: 2 }).map((h) => h.textContent)).toEqual([
      'To Do (1)', 'In Progress (0)', 'Done (0)'
    ]);
  });

  it('adds a card to a column when Enter is pressed', async () => {
    api.createTask.mockResolvedValue({ id: 11, title: 'Ship it', description: '', position: 0, columnId: 2 });
    const user = userEvent.setup();
    render(<App />);
    await screen.findByText('Write tests');

    const input = within(column('In Progress')).getByPlaceholderText('Add a card...');
    await user.type(input, '  Ship it  {Enter}');

    expect(api.createTask).toHaveBeenCalledWith('Ship it', '', 2);
    expect(await within(column('In Progress')).findByText('Ship it')).toBeInTheDocument();
    expect(input).toHaveValue('');
  });

  it('ignores blank card titles', async () => {
    const user = userEvent.setup();
    render(<App />);
    await screen.findByText('Write tests');

    await user.type(within(column('Done')).getByPlaceholderText('Add a card...'), '   {Enter}');

    expect(api.createTask).not.toHaveBeenCalled();
  });

  it('deletes a card', async () => {
    api.deleteTask.mockResolvedValue(null);
    const user = userEvent.setup();
    render(<App />);
    await screen.findByText('Write tests');

    await user.click(screen.getByRole('button', { name: 'Delete task' }));

    expect(api.deleteTask).toHaveBeenCalledWith(10);
    expect(screen.queryByText('Write tests')).not.toBeInTheDocument();
    expect(column('To Do')).toHaveTextContent('(0)');
  });

  it('shows a dismissible error when the board fails to load', async () => {
    api.getColumns.mockRejectedValue(new Error('Request failed: 500'));
    const user = userEvent.setup();
    render(<App />);

    expect(await screen.findByText('Request failed: 500')).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: '×' }));
    expect(screen.queryByText('Request failed: 500')).not.toBeInTheDocument();
  });

  it('shows an error when adding a card fails', async () => {
    api.createTask.mockRejectedValue(new Error('Column not found: 1'));
    const user = userEvent.setup();
    render(<App />);
    await screen.findByText('Write tests');

    await user.type(within(column('To Do')).getByPlaceholderText('Add a card...'), 'Oops{Enter}');

    expect(await screen.findByText('Failed to add task: Column not found: 1')).toBeInTheDocument();
  });
});
