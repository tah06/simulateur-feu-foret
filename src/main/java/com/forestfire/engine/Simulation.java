package com.forestfire.engine;

import com.forestfire.model.CellState;
import com.forestfire.model.Grid;
import com.forestfire.model.Position;

import java.util.Random;
import java.util.function.Consumer;

/** Moteur de simulation de propagation d'un feu de foret.*/
public class Simulation {

    private final double probability;
    private final Random random;
    private Grid grid;
    private int stepCount;

    public Simulation(Grid initialGrid, double probability, Random random) {
        if (probability < 0.0 || probability > 1.0) {
            throw new IllegalArgumentException("La probabilite de propagation doit etre comprise entre 0 et 1");
        }
        this.grid = initialGrid;
        this.probability = probability;
        this.random = random;
        this.stepCount = 0;
    }

    public Grid getCurrentGrid() {
        return grid;
    }

    public int getStepCount() {
        return stepCount;
    }

    public boolean isFinished() {
        return !grid.hasBurningCell();
    }

    /** Calcule et applique l'etape suivante. Ne fait rien si la simulation est deja terminee. */
    public Grid step() {
        if (isFinished()) {
            return grid;
        }

        Grid next = new Grid(grid);
        int height = grid.getHeight();
        int width = grid.getWidth();

        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                Position pos = new Position(r, c);
                if (grid.getState(pos) == CellState.BURNING) {
                    next.setState(pos, CellState.ASH);

                    for (Position neighbor : grid.neighborsOf(pos)) {
                        if (grid.getState(neighbor) == CellState.HEALTHY
                                && random.nextDouble() < probability) {
                            next.setState(neighbor, CellState.BURNING);  
                        }
                    }
                }

                
            }
        }

        grid = next;
        stepCount++;
        return grid;
    }

    // Limite de securite pour eviter les boucles infinies 
    public void run(int maxSteps, Consumer<Grid> onStep) {
        while (!isFinished() && stepCount < maxSteps) {
            step();
            if (onStep != null) {
                onStep.accept(grid);
            }
        }
    }
}
