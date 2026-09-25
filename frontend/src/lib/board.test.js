import { describe, expect, it } from 'vitest';
import { boardFromColumns, moveCard } from './board';

const card = (id) => ({ id, title: `Card ${id}` });

function sampleColumns() {
  return {
    1: { id: 1, name: 'To Do', items: [card(10), card(11), card(12)] },
    2: { id: 2, name: 'Done', items: [card(20)] }
  };
}

describe('boardFromColumns', () => {
  it('keys columns by id, exposes tasks as items and keeps API order', () => {
    const board = boardFromColumns([
      { id: 5, name: 'In Progress', position: 1, tasks: [card(1)] },
      { id: 3, name: 'To Do', position: 0, tasks: [] }
    ]);

    expect(board.columnOrder).toEqual([5, 3]);
    expect(board.columns[5].items).toEqual([card(1)]);
    expect(board.columns[3].name).toBe('To Do');
  });
});

describe('moveCard', () => {
  it('reorders a card within the same column', () => {
    const result = moveCard(sampleColumns(), { droppableId: '1', index: 0 }, { droppableId: '1', index: 2 });

    expect(result[1].items.map((c) => c.id)).toEqual([11, 12, 10]);
  });

  it('moves a card to another column at the drop index', () => {
    const result = moveCard(sampleColumns(), { droppableId: '1', index: 1 }, { droppableId: '2', index: 0 });

    expect(result[1].items.map((c) => c.id)).toEqual([10, 12]);
    expect(result[2].items.map((c) => c.id)).toEqual([11, 20]);
  });

  it('does not mutate the original state', () => {
    const columns = sampleColumns();
    moveCard(columns, { droppableId: '1', index: 0 }, { droppableId: '2', index: 1 });

    expect(columns[1].items.map((c) => c.id)).toEqual([10, 11, 12]);
    expect(columns[2].items.map((c) => c.id)).toEqual([20]);
  });
});
