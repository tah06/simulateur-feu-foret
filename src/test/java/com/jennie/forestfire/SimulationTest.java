package com.jennie.forestfire;

import com.jennie.forestfire.engine.RandomProvider;
import com.jennie.forestfire.engine.Simulation;
import com.jennie.forestfire.model.CellState;
import com.jennie.forestfire.model.Grid;
import com.jennie.forestfire.model.Position;

import static com.jennie.forestfire.Assert.*;

public final class SimulationTest {

    public static void run() {
        testProbabilityZeroFireDiesWithoutSpreading();
        testProbabilityOneSpreadsToAllReachableCells();
        testBurningCellAlwaysBecomesAshNextStep();
        testSimulationEventuallyFinishes();
        testAshCellNeverCatchesFireAgain();
        testInvalidProbabilityRejected();
        System.out.println("SimulationTest: OK");
    }

    /** Stub deterministe : le tirage reussit toujours (feu se propage systematiquement). */
    private static final RandomProvider ALWAYS_IGNITE = p -> true;

    /** Stub deterministe : le tirage echoue toujours (le feu ne se propage jamais). */
    private static final RandomProvider NEVER_IGNITE = p -> false;

    private static void testProbabilityZeroFireDiesWithoutSpreading() {
        Grid grid = new Grid(3, 3);
        grid.setState(new Position(1, 1), CellState.BURNING);
        Simulation sim = new Simulation(grid, 0.0, NEVER_IGNITE);

        sim.step();

        assertTrue("la simulation doit etre terminee apres 1 etape si p=0", sim.isFinished());
        assertEquals("une seule case (le foyer) doit etre cendre", 1, sim.getCurrentGrid().countState(CellState.ASH));
        assertEquals("les 8 autres cases doivent rester saines", 8, sim.getCurrentGrid().countState(CellState.HEALTHY));
    }

    private static void testProbabilityOneSpreadsToAllReachableCells() {
        Grid grid = new Grid(3, 3);
        grid.setState(new Position(0, 0), CellState.BURNING);
        Simulation sim = new Simulation(grid, 1.0, ALWAYS_IGNITE);

        sim.run(100, null);

        assertTrue("la simulation doit se terminer", sim.isFinished());
        assertEquals("avec p=1, toute la grille connexe doit finir en cendre", 9,
                sim.getCurrentGrid().countState(CellState.ASH));
        assertEquals("aucune case ne doit rester saine", 0, sim.getCurrentGrid().countState(CellState.HEALTHY));
    }

    private static void testBurningCellAlwaysBecomesAshNextStep() {
        Grid grid = new Grid(1, 1);
        grid.setState(new Position(0, 0), CellState.BURNING);
        Simulation sim = new Simulation(grid, 0.5, NEVER_IGNITE);

        sim.step();

        assertEquals("une case en feu doit devenir cendre a l'etape suivante",
                CellState.ASH, sim.getCurrentGrid().getState(new Position(0, 0)));
    }

    private static void testSimulationEventuallyFinishes() {
        Grid grid = new Grid(6, 6);
        grid.setState(new Position(3, 3), CellState.BURNING);
        // probabilite intermediaire avec un vrai generateur (seed fixe pour reproductibilite)
        Simulation sim = new Simulation(grid, 0.5, new com.jennie.forestfire.engine.DefaultRandomProvider(123));

        sim.run(1000, null);

        assertTrue("la simulation doit toujours atteindre un etat sans feu", sim.isFinished());
    }

    private static void testAshCellNeverCatchesFireAgain() {
        Grid grid = new Grid(1, 3);
        grid.setState(new Position(0, 0), CellState.BURNING);
        Simulation sim = new Simulation(grid, 1.0, ALWAYS_IGNITE);

        sim.step(); // (0,0) -> ASH, (0,1) -> BURNING
        sim.step(); // (0,1) -> ASH, (0,0) est voisin mais deja ASH -> doit rester ASH, jamais rebruler

        assertEquals("une case cendre ne doit jamais redevenir saine ou en feu",
                CellState.ASH, sim.getCurrentGrid().getState(new Position(0, 0)));
    }

    private static void testInvalidProbabilityRejected() {
        Grid grid = new Grid(2, 2);
        assertThrows("une probabilite hors [0,1] doit etre rejetee",
                IllegalArgumentException.class,
                () -> new Simulation(grid, 1.5, NEVER_IGNITE));
    }
}
