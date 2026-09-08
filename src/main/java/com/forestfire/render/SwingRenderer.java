package com.forestfire.render;

import com.forestfire.model.CellState;
import com.forestfire.model.Grid;
import com.forestfire.model.Position;

import javax.swing.*;
import java.awt.*;

/** Rendu graphique de la grille avec Java Swing. */
public final class SwingRenderer extends JPanel {

    private static final int CELL_SIZE = 25; // Taille d'une case en pixels
    private Grid currentGrid;

    public SwingRenderer(Grid initialGrid) {
        this.currentGrid = initialGrid;
        int width = initialGrid.getWidth() * CELL_SIZE;
        int height = initialGrid.getHeight() * CELL_SIZE;
        setPreferredSize(new Dimension(width, height));
        setBackground(Color.BLACK);
    }

    /** Met à jour la grille et force le redessin de la fenêtre. */
    public void updateGrid(Grid newGrid, int stepCount) {
        this.currentGrid = newGrid;
        repaint(); // Déclenche paintComponent
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        for (int r = 0; r < currentGrid.getHeight(); r++) {
            for (int c = 0; c < currentGrid.getWidth(); c++) {
                Position pos = new Position(r, c);
                CellState state = currentGrid.getState(pos);
                
                switch (state) {
                    case HEALTHY:
                        g.setColor(new Color(34, 139, 34)); // Vert forêt
                        break;
                    case BURNING:
                        g.setColor(new Color(255, 69, 0)); // Rouge orangé
                        break;
                    case ASH:
                        g.setColor(new Color(60, 60, 60)); // Gris sombre
                        break;
                }
                
                // Dessine la case
                g.fillRect(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                
                // Dessine une légère bordure noire autour de la case
                g.setColor(Color.BLACK);
                g.drawRect(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }
    }

    /** Initialise la fenêtre principale (JFrame) contenant le renderer. */
    public static SwingRenderer createAndShowGUI(Grid initialGrid) {
        JFrame frame = new JFrame("Simulation Feu de Forêt");
        SwingRenderer renderer = new SwingRenderer(initialGrid);
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(renderer);
        frame.pack();
        frame.setLocationRelativeTo(null); // Centre la fenêtre
        frame.setVisible(true);
        
        return renderer;
    }
}