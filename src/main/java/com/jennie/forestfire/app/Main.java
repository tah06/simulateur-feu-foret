package com.jennie.forestfire.app;

import com.jennie.forestfire.config.ConfigLoader;
import com.jennie.forestfire.config.SimulationConfig;
import com.jennie.forestfire.engine.DefaultRandomProvider;
import com.jennie.forestfire.engine.RandomProvider;
import com.jennie.forestfire.engine.Simulation;
import com.jennie.forestfire.model.CellState;
import com.jennie.forestfire.model.Grid;
import com.jennie.forestfire.model.Position;
import com.jennie.forestfire.render.ConsoleRenderer;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Point d'entree CLI.
 * Usage : java -cp out com.jennie.forestfire.app.Main [chemin-du-fichier-de-config]
 * Si aucun argument n'est fourni, utilise config/forest.properties par defaut.
 *
 * Cette classe se contente d'orchestrer : charger la config, construire la
 * grille initiale, piloter la simulation, demander un affichage a chaque
 * etape. Elle ne contient aucune regle metier - c'est la "composition root"
 * de l'application.
 */
public final class Main {

    private static final int SAFETY_MAX_STEPS = 100_000;

    public static void main(String[] args) {
        String configPath = args.length > 0 ? args[0] : "config/forest.properties";

        SimulationConfig config;
        try {
            config = ConfigLoader.load(Path.of(configPath));
        } catch (IOException e) {
            System.err.println("Impossible de lire le fichier de configuration '" + configPath + "' : " + e.getMessage());
            System.exit(1);
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

        RandomProvider randomProvider = config.getSeed()
                .map(DefaultRandomProvider::new)
                .orElseGet(DefaultRandomProvider::new);

        Simulation simulation = new Simulation(grid, config.getProbability(), randomProvider);

        System.out.println("Simulation demarree : grille " + config.getHeight() + "x" + config.getWidth()
                + ", p=" + config.getProbability() + ", foyers initiaux=" + config.getInitialFires());
        System.out.println();
        ConsoleRenderer.render(simulation.getCurrentGrid(), simulation.getStepCount());

        simulation.run(SAFETY_MAX_STEPS, g -> ConsoleRenderer.render(g, simulation.getStepCount()));

        System.out.println("Simulation terminee en " + simulation.getStepCount() + " etapes.");
    }
}
