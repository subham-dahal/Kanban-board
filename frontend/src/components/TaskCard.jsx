import { Draggable } from '@hello-pangea/dnd';

export default function TaskCard({ item, index, onDelete }) {
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
