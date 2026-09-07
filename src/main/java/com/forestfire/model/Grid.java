package com.forestfire.model;

import java.util.ArrayList;
import java.util.List;

/** Represente la foret sous forme d'une grille h x l. */
public class Grid {

    private final int height;
    private final int width;
    private final CellState[][] cells;

    public Grid(int height, int width) {
        if (height <= 0 || width <= 0) {
            throw new IllegalArgumentException("Les dimensions de la grille doivent etre strictement positives");
        }
        this.height = height;
        this.width = width;
        this.cells = new CellState[height][width];
        for (CellState[] row : cells) {
            java.util.Arrays.fill(row, CellState.HEALTHY);
        }
    }

    public Grid(Grid other) {
        this.height = other.height;
        this.width = other.width;
        this.cells = new CellState[height][width];
        for (int r = 0; r < height; r++) {
            this.cells[r] = other.cells[r].clone();
        }
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public boolean isInBounds(Position p) {
        return p.row() >= 0 && p.row() < height && p.col() >= 0 && p.col() < width;
    }

    public CellState getState(Position p) {
        checkBounds(p);
        return cells[p.row()][p.col()];
    }

    public void setState(Position p, CellState state) {
        checkBounds(p);
        cells[p.row()][p.col()] = state;
    }

    /** Retourne les positions voisines valides (4-connexite : haut, bas, gauche, droite). */
    public List<Position> neighborsOf(Position p) {
        List<Position> neighbors = new ArrayList<>(4);
        int[][] deltas = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] d : deltas) {
            Position candidate = new Position(p.row() + d[0], p.col() + d[1]);
            if (isInBounds(candidate)) {
                neighbors.add(candidate);
            }
        }
        return neighbors;
    }

    public boolean hasBurningCell() {
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                if (cells[r][c] == CellState.BURNING) {
                    return true;
                }
            }
        }
        return false;
    }

    public int countState(CellState state) {
        int count = 0;
        for (CellState[] row : cells) {
            for (CellState c : row) {
                if (c == state) count++;
            }
        }
        return count;
    }

    private void checkBounds(Position p) {
        if (!isInBounds(p)) {
            throw new IndexOutOfBoundsException("Position hors grille : " + p);
        }
    }
}
