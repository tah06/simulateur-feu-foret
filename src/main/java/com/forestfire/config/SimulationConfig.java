package com.forestfire.config;

import com.forestfire.model.Position;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** Parametres de la simulation */
public final class SimulationConfig {

    private final int height;
    private final int width;
    private final double probability;
    private final List<Position> initialFires;
    private final Long seed; // null = aleatoire non reproductible

    public SimulationConfig(int height, int width, double probability, List<Position> initialFires, Long seed) {
        this.height = height;
        this.width = width;
        this.probability = probability;
        this.initialFires = Collections.unmodifiableList(initialFires);
        this.seed = seed;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public double getProbability() {
        return probability;
    }

    public List<Position> getInitialFires() {
        return initialFires;
    }

    public Optional<Long> getSeed() {
        return Optional.ofNullable(seed);
    }
}
