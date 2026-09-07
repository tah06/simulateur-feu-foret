package com.jennie.forestfire.engine;

import java.util.Random;

/** Implementation par defaut, basee sur java.util.Random, avec seed optionnelle pour la reproductibilite. */
public class DefaultRandomProvider implements RandomProvider {

    private final Random random;

    public DefaultRandomProvider() {
        this.random = new Random();
    }

    public DefaultRandomProvider(long seed) {
        this.random = new Random(seed);
    }

    @Override
    public boolean trial(double p) {
        if (p <= 0.0) return false;
        if (p >= 1.0) return true;
        return random.nextDouble() < p;
    }
}
