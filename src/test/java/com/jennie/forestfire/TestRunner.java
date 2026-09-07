package com.jennie.forestfire;

/**
 * Point d'entree de la suite de tests (sans dependance externe, cf. README).
 * Usage : java -cp out:out-test com.jennie.forestfire.TestRunner
 * Code de sortie 0 si tout passe, 1 sinon (exploitable en CI).
 */
public final class TestRunner {

    public static void main(String[] args) {
        int failures = 0;

        failures += runSafely("GridTest", GridTest::run);
        failures += runSafely("SimulationTest", SimulationTest::run);
        failures += runSafely("ConfigLoaderTest", () -> {
            try {
                ConfigLoaderTest.run();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        System.out.println();
        if (failures == 0) {
            System.out.println("Tous les tests sont passes.");
        } else {
            System.out.println(failures + " suite(s) de test en echec.");
            System.exit(1);
        }
    }

    private static int runSafely(String name, Runnable test) {
        try {
            test.run();
            return 0;
        } catch (Throwable t) {
            System.out.println(name + ": ECHEC -> " + t.getMessage());
            return 1;
        }
    }
}
