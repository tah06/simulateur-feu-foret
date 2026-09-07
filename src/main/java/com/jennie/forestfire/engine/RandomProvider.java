package com.jennie.forestfire.engine;

/**
 * Abstraction du generateur aleatoire.
 *
 * Choix de conception : la simulation est stochastique par nature (tirage
 * de Bernoulli de parametre p). Injecter cette dependance via une interface
 * (plutot que d'appeler `new Random()` directement dans Simulation) permet :
 *  - de tester le moteur de maniere deterministe (probabilite 0 ou 1,
 *    ou un stub controle) sans dependre du hasard,
 *  - de rejouer une simulation a l'identique en fixant une seed,
 *  - de remplacer facilement l'implementation (ex: PRNG different) sans
 *    toucher a la logique de propagation.
 */
public interface RandomProvider {

    /** Retourne true avec la probabilite p (0.0 <= p <= 1.0). */
    boolean trial(double p);
}
