package tlaprojet;

import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

/** Composant Swing qui délègue le dessin au modèle Plot. */
public class PlotPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final transient Plot plot;

    public PlotPanel(Plot plot) {
        this.plot = plot;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        plot.paint((Graphics2D) g.create(), getWidth(), getHeight());
    }
}
