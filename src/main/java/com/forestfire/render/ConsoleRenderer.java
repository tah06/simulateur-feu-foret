package com.forestfire.render;

import com.forestfire.model.CellState;
import com.forestfire.model.Grid;
import com.forestfire.model.Position;

/** Rendu texte (ASCII) d'une grille dans la console. */
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
