
package com.forestfire.app;


import com.forestfire.config.ConfigLoader;
import com.forestfire.config.SimulationConfig;
import com.forestfire.engine.Simulation;
import com.forestfire.model.CellState;
import com.forestfire.model.Grid;
import com.forestfire.model.Position;
import com.forestfire.render.ConsoleRenderer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Random;

/**
 * Point d'entree CLI.
 * Usage : java -cp out com.forestfire.app.Main [chemin-du-fichier-de-config]
 * Si aucun argument n'est fourni, utilise config/forest.properties par defaut.
 */
public final class Main {

    private static final int MAX_STEPS = 100_000;

    public static void main(String[] args) {
        String configFile = args.length > 0 ? args[0] : "config/forest.properties";

        SimulationConfig config;
        try {
            config = ConfigLoader.load(Path.of(configFile));
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la config (" + configFile + ") : " + e.getMessage());            System.exit(1);
            return;
        } catch (IllegalArgumentException e) {
            System.err.println("Configuration invalide : " + e.getMessage());
            System.exit(1);
            return;
        }

        Grid grid = new Grid(config.getHeight(), config.getWidth());
        for (Position p : config.getInitialFires()) {
            grid.setState(p, CellState.BURNING);
        }

        Random random = config.getSeed()
                .map(seed -> new Random(seed))
                .orElseGet(() -> new Random());

        Simulation simulation = new Simulation(grid, config.getProbability(), random);

        System.out.println("Simulation demarree : grille " + config.getHeight() + "x" + config.getWidth()
                + ", p=" + config.getProbability() + ", foyers initiaux=" + config.getInitialFires());
        System.out.println();
        ConsoleRenderer.render(simulation.getCurrentGrid(), simulation.getStepCount());

        simulation.run(MAX_STEPS, g -> ConsoleRenderer.render(g, simulation.getStepCount()));

        System.out.println("Simulation terminee en " + simulation.getStepCount() + " etapes.");
    }
}
