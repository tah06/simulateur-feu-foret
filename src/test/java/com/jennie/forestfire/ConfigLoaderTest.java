package com.jennie.forestfire;

import com.jennie.forestfire.config.ConfigLoader;
import com.jennie.forestfire.config.SimulationConfig;
import com.jennie.forestfire.model.Position;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.jennie.forestfire.Assert.*;

public final class ConfigLoaderTest {

    public static void run() throws IOException {
        testLoadsValidConfig();
        testLoadsMultipleFirePositions();
        testMissingParameterRejected();
        testFireOutsideGridRejected();
        testInvalidProbabilityRejected();
        System.out.println("ConfigLoaderTest: OK");
    }

    private static void testLoadsValidConfig() throws IOException {
        Path file = writeTempConfig(
                "grid.height=4\n" +
                "grid.width=5\n" +
                "propagation.probability=0.4\n" +
                "fire.initial=1,2\n");

        SimulationConfig config = ConfigLoader.load(file);

        assertEquals("hauteur lue correctement", 4, config.getHeight());
        assertEquals("largeur lue correctement", 5, config.getWidth());
        assertEquals("probabilite lue correctement", 0.4, config.getProbability());
        assertEquals("une seule position de feu", 1, config.getInitialFires().size());
        assertEquals("position de feu correcte", new Position(1, 2), config.getInitialFires().get(0));
    }

    private static void testLoadsMultipleFirePositions() throws IOException {
        Path file = writeTempConfig(
                "grid.height=10\n" +
                "grid.width=10\n" +
                "propagation.probability=0.5\n" +
                "fire.initial=0,0;9,9;3,3\n");

        SimulationConfig config = ConfigLoader.load(file);
        List<Position> fires = config.getInitialFires();

        assertEquals("trois positions de feu attendues", 3, fires.size());
        assertTrue("doit contenir (0,0)", fires.contains(new Position(0, 0)));
        assertTrue("doit contenir (9,9)", fires.contains(new Position(9, 9)));
        assertTrue("doit contenir (3,3)", fires.contains(new Position(3, 3)));
    }

    private static void testMissingParameterRejected() throws IOException {
        Path file = writeTempConfig(
                "grid.height=4\n" +
                "propagation.probability=0.4\n" +
                "fire.initial=1,2\n");
        // grid.width manquant

        assertThrows("un parametre manquant doit etre rejete",
                IllegalArgumentException.class,
                () -> {
                    try {
                        ConfigLoader.load(file);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private static void testFireOutsideGridRejected() throws IOException {
        Path file = writeTempConfig(
                "grid.height=3\n" +
                "grid.width=3\n" +
                "propagation.probability=0.4\n" +
                "fire.initial=10,10\n");

        assertThrows("une position de feu hors grille doit etre rejetee",
                IllegalArgumentException.class,
                () -> {
                    try {
                        ConfigLoader.load(file);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private static void testInvalidProbabilityRejected() throws IOException {
        Path file = writeTempConfig(
                "grid.height=3\n" +
                "grid.width=3\n" +
                "propagation.probability=1.7\n" +
                "fire.initial=0,0\n");

        assertThrows("une probabilite hors [0,1] doit etre rejetee",
                IllegalArgumentException.class,
                () -> {
                    try {
                        ConfigLoader.load(file);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private static Path writeTempConfig(String content) throws IOException {
        Path file = Files.createTempFile("forest-test-config", ".properties");
        file.toFile().deleteOnExit();
        Files.writeString(file, content, StandardCharsets.UTF_8);
        return file;
    }
}
