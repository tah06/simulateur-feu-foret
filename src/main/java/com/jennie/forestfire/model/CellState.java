package com.jennie.forestfire.model;

/**
 * Etat possible d'une case de la grille.
 *
 * HEALTHY : case non brulee, potentiellement inflammable.
 * BURNING : case en feu a l'etape courante.
 * ASH     : case brulee, definitivement eteinte (ne peut plus bruler).
 */
public enum CellState {
    HEALTHY,
    BURNING,
    ASH
}
