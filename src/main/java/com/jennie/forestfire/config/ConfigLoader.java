package com.jennie.forestfire.config;

import com.jennie.forestfire.model.Position;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Charge la configuration de la simulation depuis un fichier au format
 * "java.util.Properties" (cle=valeur), choisi car nativement supporte par le
 * JDK (aucune dependance externe necessaire) et lisible/modifiable a la main.
 *
 * Format attendu (voir config/forest.properties pour un exemple complet) :
 *   grid.height=10
 *   grid.width=10
 *   propagation.probability=0.3
 *   fire.initial=2,3;5,5
 *   simulation.seed=42            (optionnel)
 */
public final class ConfigLoader {

    private ConfigLoader() {
    }

    public static SimulationConfig load(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return load(reader);
        }
    }

    public static SimulationConfig load(InputStream inputStream) throws IOException {
        try (Reader reader = new java.io.InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            return load(reader);
        }
    }

    static SimulationConfig load(Reader reader) throws IOException {
        Properties props = new Properties();
        props.load(reader);

        int height = requireInt(props, "grid.height");
        int width = requireInt(props, "grid.width");
        double probability = requireDouble(props, "propagation.probability");
        List<Position> initialFires = parsePositions(requireString(props, "fire.initial"));

        if (height <= 0 || width <= 0) {
            throw new IllegalArgumentException("grid.height et grid.width doivent etre strictement positifs");
        }
        if (probability < 0.0 || probability > 1.0) {
            throw new IllegalArgumentException("propagation.probability doit etre compris entre 0 et 1");
        }
        if (initialFires.isEmpty()) {
            throw new IllegalArgumentException("fire.initial doit contenir au moins une position");
        }
        for (Position p : initialFires) {
            if (p.row() < 0 || p.row() >= height || p.col() < 0 || p.col() >= width) {
                throw new IllegalArgumentException("Position de feu initiale hors grille : " + p);
            }
        }

        Long seed = null;
        String seedRaw = props.getProperty("simulation.seed");
        if (seedRaw != null && !seedRaw.isBlank()) {
            seed = Long.parseLong(seedRaw.trim());
        }

        return new SimulationConfig(height, width, probability, initialFires, seed);
    }

    /** Parse "2,3;5,5" en liste de Position(2,3) et Position(5,5). */
    private static List<Position> parsePositions(String raw) {
        List<Position> positions = new ArrayList<>();
        for (String token : raw.split(";")) {
            String trimmed = token.trim();
            if (trimmed.isEmpty()) continue;
            String[] parts = trimmed.split(",");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Format de position invalide : '" + trimmed + "' (attendu: ligne,colonne)");
            }
            int row = Integer.parseInt(parts[0].trim());
            int col = Integer.parseInt(parts[1].trim());
            positions.add(new Position(row, col));
        }
        return positions;
    }

    private static String requireString(Properties props, String key) {
        String value = props.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Parametre manquant dans le fichier de configuration : " + key);
        }
        return value.trim();
    }

    private static int requireInt(Properties props, String key) {
        return Integer.parseInt(requireString(props, key));
    }

    private static double requireDouble(Properties props, String key) {
        return Double.parseDouble(requireString(props, key));
    }
}
