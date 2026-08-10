const API_BASE = import.meta.env.VITE_API_URL || "http://localhost:8080/api";

async function request(path, options) {
  const res = await fetch(`${API_BASE}${path}`, {
    headers: { "Content-Type": "application/json" },
    ...options
  });
  if (!res.ok) {
    const body = await res.json().catch(() => null);
    throw new Error(body?.message || `Request failed: ${res.status}`);
  }
  return res.status === 204 ? null : res.json();
}

export function getColumns() {
  return request("/columns");
}

export function createTask(title, description, columnId) {
  return request("/tasks", {
    method: "POST",
    body: JSON.stringify({ title, description, columnId })
  });
}

export function updateTask(id, title, description) {
  return request(`/tasks/${id}`, {
    method: "PUT",
    body: JSON.stringify({ title, description })
  });
}

export function deleteTask(id) {
  return request(`/tasks/${id}`, { method: "DELETE" });
}

export function moveTask(id, columnId, position) {
  return request(`/tasks/${id}/move`, {
    method: "PUT",
    body: JSON.stringify({ columnId, position })
  });
}
