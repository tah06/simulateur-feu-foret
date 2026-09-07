package com.forestfire.model;

/**
 * Etat possible d'une case de la grille.
 *
 * HEALTHY : case non brulee.
 * BURNING : case en feu a l'etape courante.
 * ASH     : case brulee.
 */
public enum CellState {
    HEALTHY,
    BURNING,
    ASH
}
