package com.jennie.forestfire.engine;

import com.jennie.forestfire.model.CellState;
import com.jennie.forestfire.model.Grid;
import com.jennie.forestfire.model.Position;

import java.util.function.Consumer;

/**
 * Moteur de simulation de propagation d'un feu de foret.
 *
 * Regle appliquee a chaque etape t -> t+1, pour chaque case en feu a t :
 *   1. elle s'eteint (devient cendre, definitivement),
 *   2. pour chacune de ses 4 cases adjacentes encore saines, elle a une
 *      probabilite p de mettre le feu a cette case.
 *
 * Choix de conception :
 *  - La grille suivante est calculee a partir d'une COPIE de la grille
 *    courante (cf. Grid(Grid)), jamais en mutant la grille en cours de
 *    lecture. Cela evite un bug classique : si on modifiait la grille en
 *    place pendant qu'on la parcourt, une case fraichement enflammee a
 *    l'etape t pourrait, par erreur, en enflammer une autre a la MEME
 *    etape (effet de propagation en cascade non voulu, dependant de
 *    l'ordre de parcours). En travaillant sur un instantane immuable de
 *    l'etat t pour decider de l'etat t+1, le resultat ne depend jamais de
 *    l'ordre d'iteration sur la grille.
 *  - Le generateur aleatoire est injecte via RandomProvider (voir cette
 *    interface) pour rendre le moteur testable de maniere deterministe.
 *  - Le moteur ne fait aucun affichage : il expose un `step()` et un
 *    callback optionnel dans `run(...)` pour permettre a l'appelant
 *    (CLI, futur GUI, tests) de reagir a chaque etape sans coupler le
 *    moteur a une technologie de rendu particuliere.
 */
public class Simulation {

    private final double probability;
    private final RandomProvider randomProvider;
    private Grid currentGrid;
    private int stepCount;

    public Simulation(Grid initialGrid, double probability, RandomProvider randomProvider) {
        if (probability < 0.0 || probability > 1.0) {
            throw new IllegalArgumentException("La probabilite de propagation doit etre comprise entre 0 et 1");
        }
        this.currentGrid = initialGrid;
        this.probability = probability;
        this.randomProvider = randomProvider;
        this.stepCount = 0;
    }

    public Grid getCurrentGrid() {
        return currentGrid;
    }

    public int getStepCount() {
        return stepCount;
    }

    public boolean isFinished() {
        return !currentGrid.hasBurningCell();
    }

    /** Calcule et applique l'etape suivante. Ne fait rien si la simulation est deja terminee. */
    public Grid step() {
        if (isFinished()) {
            return currentGrid;
        }

        Grid next = new Grid(currentGrid);
        int height = currentGrid.getHeight();
        int width = currentGrid.getWidth();
        boolean[][] willIgnite = new boolean[height][width];

        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                Position pos = new Position(r, c);
                if (currentGrid.getState(pos) != CellState.BURNING) {
                    continue;
                }
                // 1. la case en feu s'eteint (devient cendre)
                next.setState(pos, CellState.ASH);

                // 2. tentative de propagation vers chacun des voisins sains
                for (Position neighbor : currentGrid.neighborsOf(pos)) {
                    if (currentGrid.getState(neighbor) == CellState.HEALTHY
                            && randomProvider.trial(probability)) {
                        willIgnite[neighbor.row()][neighbor.col()] = true;
                    }
                }
            }
        }

        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                if (willIgnite[r][c]) {
                    next.setState(new Position(r, c), CellState.BURNING);
                }
            }
        }

        currentGrid = next;
        stepCount++;
        return currentGrid;
    }

    /**
     * Deroule la simulation jusqu'a extinction complete, en notifiant `onStep`
     * apres chaque etape (utile pour l'affichage ou l'enregistrement de l'historique).
     *
     * `maxSteps` est une garde-fou defensive (la simulation termine toujours
     * en un nombre fini d'etapes puisqu'une case ne peut bruler qu'une fois,
     * mais borner explicitement evite tout risque en cas d'evolution future
     * des regles de propagation).
     */
    public void run(int maxSteps, Consumer<Grid> onStep) {
        while (!isFinished() && stepCount < maxSteps) {
            step();
            if (onStep != null) {
                onStep.accept(currentGrid);
            }
        }
    }
}
