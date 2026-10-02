package tlaprojet;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;

/** Modèle du tracé : la fonction analysée, l'échelle, et le dessin des axes et de la courbe. */
public class Plot {

    private static final Color AXIS = new Color(0x55, 0x5B, 0x66);
    private static final Color GRID = new Color(0xE4, 0xE7, 0xEC);
    private static final Color CURVE = new Color(0x1F, 0x4F, 0xC8);

    /** Demi-largeur visible (en unités) sur le plus petit côté de la zone de tracé. */
    double range = 5;
    private ASTNode function;
    private String source = "";

    /** Analyse l'expression ; lève SyntaxError si elle est invalide (l'ancienne courbe est conservée). */
    void setFunction(String expression) {
        ASTNode parsed = Parser.parse(expression);
        this.function = parsed;
        this.source = expression.trim();
    }

    void setRange(double range) {
        this.range = range;
    }

    String source() {
        return source;
    }

    void paint(Graphics2D g, int w, int h) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);

        double ppu = Math.min(w, h) / (2 * range); // pixels par unité, identique sur les deux axes
        double cx = w / 2.0, cy = h / 2.0;

        drawGridAndAxes(g, w, h, ppu, cx, cy);
        if (function == null) return;

        g.setColor(CURVE);
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D.Double path = new Path2D.Double();
        boolean penDown = false;
        double prevPy = 0;

        // Un point par pixel de largeur ; on lève le crayon sur les valeurs non définies
        // (ln(x) pour x <= 0) et sur les sauts verticaux (asymptotes de 1/x, tan(x)).
        for (int px = 0; px <= w; px++) {
            double x = (px - cx) / ppu;
            double y = function.evaluate(x);
            double py = cy - y * ppu;
            boolean drawable = Double.isFinite(y) && Math.abs(py) < 1e6;
            if (!drawable) {
                penDown = false;
                continue;
            }
            if (penDown && Math.abs(py - prevPy) < h) {
                path.lineTo(px, py);
            } else {
                path.moveTo(px, py);
            }
            penDown = true;
            prevPy = py;
        }
        g.draw(path);
    }

    private void drawGridAndAxes(Graphics2D g, int w, int h, double ppu, double cx, double cy) {
        double step = niceStep(range / 4);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        int maxX = (int) Math.ceil(cx / ppu / step), maxY = (int) Math.ceil(cy / ppu / step);

        g.setStroke(new BasicStroke(1f));
        for (int i = -maxX; i <= maxX; i++) {
            int px = (int) Math.round(cx + i * step * ppu);
            g.setColor(GRID);
            g.drawLine(px, 0, px, h);
            if (i != 0) {
                g.setColor(AXIS);
                g.drawString(label(i * step), px + 3, (int) cy + 13);
            }
        }
        for (int i = -maxY; i <= maxY; i++) {
            int py = (int) Math.round(cy - i * step * ppu);
            g.setColor(GRID);
            g.drawLine(0, py, w, py);
            if (i != 0) {
                g.setColor(AXIS);
                g.drawString(label(i * step), (int) cx + 4, py - 3);
            }
        }
        g.setColor(AXIS);
        g.setStroke(new BasicStroke(1.4f));
        g.drawLine((int) cx, 0, (int) cx, h);
        g.drawLine(0, (int) cy, w, (int) cy);
    }

    /** Pas de graduation « rond » : 1, 2 ou 5 × 10^k. */
    static double niceStep(double raw) {
        double exp = Math.pow(10, Math.floor(Math.log10(raw)));
        double f = raw / exp;
        return (f < 1.5 ? 1 : f < 3.5 ? 2 : f < 7.5 ? 5 : 10) * exp;
    }

    private static String label(double v) {
        if (Math.abs(v - Math.rint(v)) < 1e-9) return String.valueOf((long) Math.rint(v));
        return String.format("%.2f", v).replaceAll("0+$", "").replace('.', ',');
    }
}
