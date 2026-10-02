package tlaprojet;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/** Interface : saisie de la fonction, bouton Tracer (ou Entrée), curseur d'échelle, message d'erreur. */
public class Main {

    static final int PREF_WIDTH = 720;
    static final int PREF_HEIGHT = 520;
    static final double RANGE_ADJUST = 10;
    static final String DEFAULT_FUNCTION = "x^2 - 2";

    private Plot plot;

    public static void main(String[] args) {
        String initial = args.length > 0 ? String.join(" ", args) : DEFAULT_FUNCTION;
        SwingUtilities.invokeLater(() -> new Main().init(initial));
    }

    void init(String initialFunction) {
        plot = new Plot();

        JFrame frame = new JFrame("Traceur de fonctions");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        PlotPanel plotPanel = new PlotPanel(plot);
        plotPanel.setPreferredSize(new Dimension(PREF_WIDTH, PREF_HEIGHT));
        frame.add(plotPanel, BorderLayout.CENTER);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        top.add(new JLabel("f(x) ="));
        JTextField input = new JTextField(initialFunction, 24);
        top.add(input);
        JButton draw = new JButton("Tracer");
        top.add(draw);
        top.add(new JLabel("Échelle"));
        JSlider slider = new JSlider(JSlider.HORIZONTAL, 1, 100, (int) (plot.range * RANGE_ADJUST));
        slider.setToolTipText("Demi-largeur visible, de 0,1 à 10 unités");
        top.add(slider);
        frame.add(top, BorderLayout.NORTH);

        JLabel status = new JLabel(" ");
        status.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        frame.add(status, BorderLayout.SOUTH);

        Runnable apply = () -> {
            try {
                plot.setFunction(input.getText());
                status.setForeground(new Color(0x44, 0x4B, 0x57));
                status.setText("f(x) = " + plot.source());
            } catch (SyntaxError e) {
                status.setForeground(new Color(0xB0, 0x2A, 0x20));
                status.setText("Erreur : " + e.getMessage());
            }
            plotPanel.repaint();
        };
        draw.addActionListener(e -> apply.run());
        input.addActionListener(e -> apply.run()); // touche Entrée

        slider.addChangeListener(e -> {
            plot.setRange(slider.getValue() / RANGE_ADJUST);
            plotPanel.repaint();
        });

        apply.run();
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
