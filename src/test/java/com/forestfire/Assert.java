package com.forestfire;

/**
 * Mini-utilitaire d'assertions, pour rester sans dependance externe (pas de
 * telechargement JUnit necessaire). Voir README.md, section "Tests", pour la
 * justification de ce choix et la maniere de migrer vers JUnit5 si souhaite.
 */
public final class Assert {

    private Assert() {
    }

    public static void assertTrue(String message, boolean condition) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    public static void assertFalse(String message, boolean condition) {
        assertTrue(message, !condition);
    }

    public static void assertEquals(String message, Object expected, Object actual) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(message + " (attendu=" + expected + ", obtenu=" + actual + ")");
        }
    }

    public static void assertThrows(String message, Class<? extends Throwable> expectedType, Runnable action) {
        try {
            action.run();
        } catch (Throwable t) {
            if (expectedType.isInstance(t)) {
                return;
            }
            throw new AssertionError(message + " (mauvais type d'exception: " + t.getClass() + ")");
        }
        throw new AssertionError(message + " (aucune exception levee)");
    }
}
