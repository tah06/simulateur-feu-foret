package com.forestfire;
import com.forestfire.config.ConfigLoader;
import com.forestfire.config.SimulationConfig;
import com.forestfire.engine.Simulation;
import com.forestfire.model.CellState;
import com.forestfire.model.Grid;
import com.forestfire.model.Position;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;

import static com.forestfire.Assert.*;

public final class ForestFireTests {

    public static void main(String[] args) {
        int failures = 0;
        failures += runSafely("Grid Tests", ForestFireTests::runGridTests);
        failures += runSafely("ConfigLoader Tests", ForestFireTests::runConfigLoaderTests);
        failures += runSafely("Simulation Tests", ForestFireTests::runSimulationTests);

        System.out.println();
        if (failures == 0) {
            System.out.println("Tous les tests sont passes.");
        } else {
            System.out.println("❌ " + failures + " suite(s) de test en echec.");
            System.exit(1);
        }
    }

    private static int runSafely(String name, RunnableTest test) {
        try {
            test.run();
            System.out.println(name + ": OK");
            return 0;
        } catch (Throwable t) {
            System.err.println(name + ": ECHEC -> " + t.getMessage());
            return 1;
        }
    }

    // Interface fonctionnelle qui permet de throw des exceptions (pratique pour l'I/O)
    private interface RunnableTest {
        void run() throws Exception;
    }

    // =================================================================================
    // GRID TESTS
    // =================================================================================
    private static void runGridTests() {
        Grid grid = new Grid(3, 3);
        assertEquals("Init: toutes saines", 9, grid.countState(CellState.HEALTHY));
        assertFalse("Init: pas de feu", grid.hasBurningCell());

        List<Position> centerNeighbors = new Grid(5, 5).neighborsOf(new Position(2, 2));
        assertEquals("Voisins: centre", 4, centerNeighbors.size());

        List<Position> cornerNeighbors = new Grid(5, 5).neighborsOf(new Position(0, 0));
        assertEquals("Voisins: coin", 2, cornerNeighbors.size());

        grid.setState(new Position(1, 1), CellState.BURNING);
        assertTrue("Feu detecte", grid.hasBurningCell());

        Grid copy = new Grid(grid);
        Position pos = new Position(1, 01);
        copy.setState(pos, CellState.ASH);
        assertEquals("La copie ne modifie pas l'original", CellState.BURNING, grid.getState(pos));
        assertEquals("La copie est modifiee", CellState.ASH, copy.getState(pos));

        assertThrows("Hors limite", IndexOutOfBoundsException.class, () -> grid.getState(new Position(5, 5)));
        assertThrows("Dimensions invalides", IllegalArgumentException.class, () -> new Grid(0, 5));
    }

    // =================================================================================
    // CONFIGLOADER TESTS
    // =================================================================================
    private static void runConfigLoaderTests() throws IOException {
        Path validFile = writeTempConfig(
                "grid.height=4\n" +
                "grid.width=5\n" +
                "propagation.probability=0.4\n" +
                "fire.initial=1,2\n");
        SimulationConfig config = ConfigLoader.load(validFile);
        assertEquals("Height", 4, config.getHeight());
        assertEquals("Probability", 0.4, config.getProbability());
        assertEquals("Initial fires size", 1, config.getInitialFires().size());

        Path multiFireFile = writeTempConfig(
                "grid.height=10\n" +
                "grid.width=10\n" +
                "propagation.probability=0.5\n" +
                "fire.initial=0,0;9,9;3,3\n");
        SimulationConfig multiConfig = ConfigLoader.load(multiFireFile);
        assertTrue("Contains (0,0)", multiConfig.getInitialFires().contains(new Position(0, 0)));
        assertEquals("Total fires", 3, multiConfig.getInitialFires().size());

        Path missingParam = writeTempConfig("grid.height=4\npropagation.probability=0.4\nfire.initial=1,2\n");
        assertThrows("Param manquant", IllegalArgumentException.class, () -> {
            try {
                ConfigLoader.load(missingParam);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });

        Path fireOutside = writeTempConfig("grid.height=3\ngrid.width=3\npropagation.probability=0.4\nfire.initial=10,10\n");
        assertThrows("Feu hors grille", IllegalArgumentException.class, () -> {
            try {
                ConfigLoader.load(fireOutside);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
    }

    private static Path writeTempConfig(String content) throws IOException {
        Path file = Files.createTempFile("forest-test-config", ".properties");
        file.toFile().deleteOnExit();
        Files.writeString(file, content, StandardCharsets.UTF_8);
        return file;
    }

    // =================================================================================
    // SIMULATION TESTS
    // =================================================================================
    private static void runSimulationTests() {
        Random dummyRandom = new Random(42);

        // Test P=0 (Le feu s'eteint sans se propager)
        Grid gridZero = new Grid(3, 3);
        gridZero.setState(new Position(1, 1), CellState.BURNING);
        Simulation simZero = new Simulation(gridZero, 0.0, dummyRandom);
        simZero.step();
        assertTrue("Simulation terminee (P=0)", simZero.isFinished());
        assertEquals("1 case cendre", 1, simZero.getCurrentGrid().countState(CellState.ASH));

        // Test P=1 (Le feu ravage tout)
        Grid gridOne = new Grid(3, 3);
        gridOne.setState(new Position(0, 0), CellState.BURNING);
        Simulation simOne = new Simulation(gridOne, 1.0, dummyRandom);
        simOne.run(100, null);
        assertTrue("Simulation terminee (P=1)", simOne.isFinished());
        assertEquals("Toute la grille en cendre", 9, simOne.getCurrentGrid().countState(CellState.ASH));
        
        // Test: Une case cendre ne rebrûle jamais
        Grid gridAsh = new Grid(1, 3);
        Position pos = new Position(0, 0);
        gridAsh.setState(pos, CellState.BURNING);
        Simulation simAsh = new Simulation(gridAsh, 1.0, dummyRandom);
        simAsh.step(); // (0,0) -> ASH, (0,1) -> BURNING
        simAsh.step(); // (0,1) -> ASH
        assertEquals("Cendre reste Cendre", CellState.ASH, simAsh.getCurrentGrid().getState(pos));

        assertThrows("Probabilite invalide", IllegalArgumentException.class, 
                () -> new Simulation(gridOne, 1.5, dummyRandom));
    }
}
