package com.jennie.forestfire;

import com.jennie.forestfire.model.CellState;
import com.jennie.forestfire.model.Grid;
import com.jennie.forestfire.model.Position;

import java.util.List;

import static com.jennie.forestfire.Assert.*;

public final class GridTest {

    public static void run() {
        testInitialGridIsAllHealthy();
        testNeighborsInCenter();
        testNeighborsOnCorner();
        testNeighborsOnEdge();
        testHasBurningCell();
        testCopyConstructorIsIndependent();
        testOutOfBoundsThrows();
        testInvalidDimensionsThrow();
        System.out.println("GridTest: OK");
    }

    private static void testInitialGridIsAllHealthy() {
        Grid grid = new Grid(3, 3);
        assertEquals("toutes les cases doivent etre saines au depart", 9, grid.countState(CellState.HEALTHY));
    }

    private static void testNeighborsInCenter() {
        Grid grid = new Grid(5, 5);
        List<Position> neighbors = grid.neighborsOf(new Position(2, 2));
        assertEquals("une case centrale a 4 voisins", 4, neighbors.size());
    }

    private static void testNeighborsOnCorner() {
        Grid grid = new Grid(5, 5);
        List<Position> neighbors = grid.neighborsOf(new Position(0, 0));
        assertEquals("une case en coin a 2 voisins", 2, neighbors.size());
    }

    private static void testNeighborsOnEdge() {
        Grid grid = new Grid(5, 5);
        List<Position> neighbors = grid.neighborsOf(new Position(0, 2));
        assertEquals("une case de bord (non coin) a 3 voisins", 3, neighbors.size());
    }

    private static void testHasBurningCell() {
        Grid grid = new Grid(3, 3);
        assertFalse("aucune case en feu au depart", grid.hasBurningCell());
        grid.setState(new Position(1, 1), CellState.BURNING);
        assertTrue("une case en feu doit etre detectee", grid.hasBurningCell());
    }

    private static void testCopyConstructorIsIndependent() {
        Grid original = new Grid(2, 2);
        original.setState(new Position(0, 0), CellState.BURNING);
        Grid copy = new Grid(original);
        copy.setState(new Position(0, 0), CellState.ASH);
        assertEquals("la copie ne doit pas affecter l'original", CellState.BURNING, original.getState(new Position(0, 0)));
        assertEquals("la modification doit s'appliquer a la copie", CellState.ASH, copy.getState(new Position(0, 0)));
    }

    private static void testOutOfBoundsThrows() {
        Grid grid = new Grid(2, 2);
        assertThrows("un acces hors grille doit lever une exception",
                IndexOutOfBoundsException.class,
                () -> grid.getState(new Position(5, 5)));
    }

    private static void testInvalidDimensionsThrow() {
        assertThrows("une hauteur negative ou nulle doit etre rejetee",
                IllegalArgumentException.class,
                () -> new Grid(0, 5));
    }
}
