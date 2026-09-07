package com.jennie.forestfire.model;

import java.util.Objects;

/**
 * Coordonnee immuable (ligne, colonne) dans la grille.
 * Type dedie plutot qu'un simple tableau d'int : plus lisible,
 * plus sur (pas d'inversion ligne/colonne possible par erreur),
 * et fournit equals/hashCode pour etre utilisable dans des collections.
 */
public final class Position {

    private final int row;
    private final int col;

    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int row() {
        return row;
    }

    public int col() {
        return col;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position)) return false;
        Position position = (Position) o;
        return row == position.row && col == position.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }

    @Override
    public String toString() {
        return "(" + row + "," + col + ")";
    }
}
