import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { createTask, deleteTask, getColumns, moveTask, updateTask } from './api';

const BASE = 'http://localhost:8080/api';

function jsonResponse(body, status = 200) {
  return Promise.resolve({ ok: status < 400, status, json: () => Promise.resolve(body) });
}

describe('api', () => {
  let fetchMock;

  beforeEach(() => {
    fetchMock = vi.fn();
    vi.stubGlobal('fetch', fetchMock);
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('getColumns fetches the board', async () => {
    fetchMock.mockReturnValue(jsonResponse([{ id: 1 }]));

    await expect(getColumns()).resolves.toEqual([{ id: 1 }]);
    expect(fetchMock).toHaveBeenCalledWith(`${BASE}/columns`, expect.objectContaining({
      headers: { 'Content-Type': 'application/json' }
    }));
  });

  it('createTask POSTs title, description and column', async () => {
    fetchMock.mockReturnValue(jsonResponse({ id: 9 }, 201));

    await createTask('Title', 'Desc', 3);

    const [url, options] = fetchMock.mock.calls[0];
    expect(url).toBe(`${BASE}/tasks`);
    expect(options.method).toBe('POST');
    expect(JSON.parse(options.body)).toEqual({ title: 'Title', description: 'Desc', columnId: 3 });
  });

  it('updateTask PUTs to the task URL', async () => {
    fetchMock.mockReturnValue(jsonResponse({ id: 4 }));

    await updateTask(4, 'New', 'Body');

    const [url, options] = fetchMock.mock.calls[0];
    expect(url).toBe(`${BASE}/tasks/4`);
    expect(options.method).toBe('PUT');
    expect(JSON.parse(options.body)).toEqual({ title: 'New', description: 'Body' });
  });

  it('moveTask PUTs the target column and position', async () => {
    fetchMock.mockReturnValue(jsonResponse({ id: 4 }));

    await moveTask(4, 2, 0);

    const [url, options] = fetchMock.mock.calls[0];
    expect(url).toBe(`${BASE}/tasks/4/move`);
    expect(JSON.parse(options.body)).toEqual({ columnId: 2, position: 0 });
  });

  it('deleteTask resolves to null on 204', async () => {
    fetchMock.mockReturnValue(Promise.resolve({ ok: true, status: 204 }));

    await expect(deleteTask(4)).resolves.toBeNull();
    expect(fetchMock.mock.calls[0][1].method).toBe('DELETE');
  });

  it('surfaces the backend error message', async () => {
    fetchMock.mockReturnValue(jsonResponse({ message: 'Task not found: 4' }, 404));

    await expect(deleteTask(4)).rejects.toThrow('Task not found: 4');
  });

  it('falls back to the status code when the error body is not JSON', async () => {
    fetchMock.mockReturnValue(Promise.resolve({ ok: false, status: 500, json: () => Promise.reject(new Error('bad json')) }));

    await expect(getColumns()).rejects.toThrow('Request failed: 500');
  });
});
