// Pure helpers for board state so the drag-and-drop logic can be tested without a browser.

/** Converts the `/api/columns` response into a lookup keyed by column id plus the display order. */
export function boardFromColumns(data) {
  const columns = {};
  data.forEach((col) => {
    columns[col.id] = { ...col, items: col.tasks };
  });
  return { columns, columnOrder: data.map((col) => col.id) };
}

/**
 * Returns a new columns map with the card at `source.index` moved to `destination.index`.
 * `droppableId`s are the column ids as strings (as provided by @hello-pangea/dnd).
 */
export function moveCard(columns, source, destination) {
  const sourceColId = Number(source.droppableId);
  const destColId = Number(destination.droppableId);

  if (sourceColId === destColId) {
    const column = columns[sourceColId];
    const items = [...column.items];
    const [removed] = items.splice(source.index, 1);
    items.splice(destination.index, 0, removed);
    return { ...columns, [sourceColId]: { ...column, items } };
  }

  const sourceCol = columns[sourceColId];
  const destCol = columns[destColId];
  const sourceItems = [...sourceCol.items];
  const destItems = [...destCol.items];
  const [removed] = sourceItems.splice(source.index, 1);
  destItems.splice(destination.index, 0, removed);

  return {
    ...columns,
    [sourceColId]: { ...sourceCol, items: sourceItems },
    [destColId]: { ...destCol, items: destItems }
  };
}
