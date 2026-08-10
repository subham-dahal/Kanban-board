import React, { useEffect, useState } from 'react';
import { DragDropContext, Droppable, Draggable } from '@hello-pangea/dnd';
import { getColumns, createTask, deleteTask, moveTask } from './api';

function TaskCard({ item, index, onDelete }) {
  return (
    <Draggable draggableId={String(item.id)} index={index}>
      {(provided, snapshot) => (
        <div
          ref={provided.innerRef}
          {...provided.draggableProps}
          {...provided.dragHandleProps}
          className={`bg-white rounded-lg shadow p-3 mb-3 select-none group relative ${
            snapshot.isDragging ? "shadow-lg ring-2 ring-blue-400" : ""
          }`}
        >
          <button
            type="button"
            onClick={onDelete}
            className="absolute top-1 right-1 text-slate-300 hover:text-red-500 opacity-0 group-hover:opacity-100 px-1 text-sm"
            aria-label="Delete task"
          >
            ×
          </button>
          <p className="font-medium text-slate-800 pr-4">{item.title}</p>
          {item.description && (
            <p className="text-sm text-slate-500 mt-1">{item.description}</p>
          )}
        </div>
      )}
    </Draggable>
  );
}

function App() {
  const [columns, setColumns] = useState({});
  const [columnOrder, setColumnOrder] = useState([]);
  const [drafts, setDrafts] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    getColumns()
      .then((data) => {
        const map = {};
        data.forEach((col) => {
          map[col.id] = { ...col, items: col.tasks };
        });
        setColumns(map);
        setColumnOrder(data.map((col) => col.id));
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  const onDragEnd = (result) => {
    if (!result.destination) return;

    const { source, destination, draggableId } = result;
    const sourceColId = Number(source.droppableId);
    const destColId = Number(destination.droppableId);

    if (sourceColId !== destColId) {
      const sourceCol = columns[sourceColId];
      const destCol = columns[destColId];
      const sourceItems = [...sourceCol.items];
      const destItems = [...destCol.items];

      const [removed] = sourceItems.splice(source.index, 1);
      destItems.splice(destination.index, 0, removed);

      setColumns({
        ...columns,
        [sourceColId]: { ...sourceCol, items: sourceItems },
        [destColId]: { ...destCol, items: destItems }
      });
    } else {
      const column = columns[sourceColId];
      const copiedItems = [...column.items];
      const [removed] = copiedItems.splice(source.index, 1);
      copiedItems.splice(destination.index, 0, removed);

      setColumns({
        ...columns,
        [sourceColId]: { ...column, items: copiedItems }
      });
    }

    moveTask(Number(draggableId), destColId, destination.index).catch((err) =>
      setError(`Failed to save move: ${err.message}`)
    );
  };

  const handleAddTask = (columnId) => {
    const draft = drafts[columnId];
    if (!draft?.title?.trim()) return;

    createTask(draft.title.trim(), draft.description?.trim() || '', columnId)
      .then((task) => {
        setColumns((prev) => ({
          ...prev,
          [columnId]: { ...prev[columnId], items: [...prev[columnId].items, task] }
        }));
        setDrafts((prev) => ({ ...prev, [columnId]: { title: '', description: '' } }));
      })
      .catch((err) => setError(`Failed to add task: ${err.message}`));
  };

  const handleDeleteTask = (columnId, taskId) => {
    deleteTask(taskId)
      .then(() => {
        setColumns((prev) => ({
          ...prev,
          [columnId]: {
            ...prev[columnId],
            items: prev[columnId].items.filter((item) => item.id !== taskId)
          }
        }));
      })
      .catch((err) => setError(`Failed to delete task: ${err.message}`));
  };

  if (loading) {
    return <div className="min-h-screen flex items-center justify-center text-slate-500">Loading board...</div>;
  }

  return (
    <div className="min-h-screen bg-slate-100 p-8">
      <h1 className="text-3xl font-bold text-slate-800 mb-4 text-center">
        Kanban Board
      </h1>

      {error && (
        <div className="max-w-2xl mx-auto mb-4 bg-red-100 text-red-700 px-4 py-2 rounded-lg text-sm flex justify-between items-center">
          <span>{error}</span>
          <button type="button" onClick={() => setError(null)} className="font-bold px-2">×</button>
        </div>
      )}

      <DragDropContext onDragEnd={onDragEnd}>
        <div className="flex gap-6 justify-center items-start">
          {columnOrder.map((columnId) => {
            const column = columns[columnId];
            const draft = drafts[columnId] || { title: '', description: '' };

            return (
              <div key={columnId} className="bg-slate-200 rounded-xl w-80 flex flex-col">
                <h2 className="font-semibold text-slate-700 px-4 pt-4 pb-2">
                  {column.name}{" "}
                  <span className="text-sm text-slate-400">({column.items.length})</span>
                </h2>

                <Droppable droppableId={String(columnId)}>
                  {(provided, snapshot) => (
                    <div
                      ref={provided.innerRef}
                      {...provided.droppableProps}
                      className={`flex-1 p-3 min-h-[80px] transition-colors ${
                        snapshot.isDraggingOver ? "bg-slate-300" : ""
                      }`}
                    >
                      {column.items.map((item, index) => (
                        <TaskCard
                          key={item.id}
                          item={item}
                          index={index}
                          onDelete={() => handleDeleteTask(columnId, item.id)}
                        />
                      ))}
                      {provided.placeholder}
                    </div>
                  )}
                </Droppable>

                <div className="p-3 pt-0">
                  <input
                    value={draft.title}
                    onChange={(e) =>
                      setDrafts((prev) => ({ ...prev, [columnId]: { ...draft, title: e.target.value } }))
                    }
                    onKeyDown={(e) => e.key === 'Enter' && handleAddTask(columnId)}
                    placeholder="Add a card..."
                    className="w-full text-sm px-3 py-2 rounded-lg border border-slate-300 focus:outline-none focus:ring-2 focus:ring-blue-400"
                  />
                </div>
              </div>
            );
          })}
        </div>
      </DragDropContext>
    </div>
  );
}

export default App;
