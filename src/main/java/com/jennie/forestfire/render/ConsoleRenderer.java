package com.jennie.forestfire.render;

import com.jennie.forestfire.model.CellState;
import com.jennie.forestfire.model.Grid;
import com.jennie.forestfire.model.Position;

/**
 * Rendu texte (ASCII) d'une grille dans la console.
 *
 * Isole volontairement du moteur de simulation : Simulation ne sait pas que
 * ce renderer existe. On pourrait demain ajouter un renderer HTML/Swing/JSON
 * sans toucher a la logique metier - simple respect du principe de
 * responsabilite unique (SRP) et d'inversion de dependance.
 */
public final class ConsoleRenderer {

    private static final char HEALTHY_SYMBOL = '.';
    private static final char BURNING_SYMBOL = '*';
    private static final char ASH_SYMBOL = '#';

    private ConsoleRenderer() {
    }

    public static void render(Grid grid, int stepNumber) {
        System.out.println("--- Etape " + stepNumber + " ---");
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < grid.getHeight(); r++) {
            for (int c = 0; c < grid.getWidth(); c++) {
                sb.append(symbolFor(grid.getState(new Position(r, c))));
            }
            sb.append('\n');
        }
        System.out.print(sb);
        System.out.println("Sain: " + grid.countState(CellState.HEALTHY)
                + " | En feu: " + grid.countState(CellState.BURNING)
                + " | Cendre: " + grid.countState(CellState.ASH));
        System.out.println();
    }

    private static char symbolFor(CellState state) {
        switch (state) {
            case HEALTHY: return HEALTHY_SYMBOL;
            case BURNING: return BURNING_SYMBOL;
            case ASH: return ASH_SYMBOL;
            default: throw new IllegalStateException("Etat inconnu : " + state);
        }
    }
}
