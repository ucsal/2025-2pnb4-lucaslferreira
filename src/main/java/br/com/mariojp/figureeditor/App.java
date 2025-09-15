package br.com.mariojp.figureeditor;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            JFrame frame = new JFrame("Figure Editor");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

            DrawingPanel panel = new DrawingPanel();

            JButton colorButton = new JButton("Cor...");
            colorButton.addActionListener(e -> {
                Color chosen = JColorChooser.showDialog(frame, "Escolha a cor", panel.getCurrentColor());
                if (chosen != null) {
                    panel.setCurrentColor(chosen);
                }
            });

            JButton exportPNG = new JButton("Exportar PNG");
            exportPNG.addActionListener(e -> {
                JFileChooser fc = new JFileChooser();
                if (fc.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    File f = fc.getSelectedFile();
                    try {
                        panel.exportToPNG(f);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            JButton exportSVG = new JButton("Exportar SVG");
            exportSVG.addActionListener(e -> {
                JFileChooser fc = new JFileChooser();
                if (fc.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    File f = fc.getSelectedFile();
                    try {
                        panel.exportToSVG(f);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            JPanel controls = new JPanel();
            controls.add(colorButton);
            controls.add(exportPNG);
            controls.add(exportSVG);

            frame.setLayout(new BorderLayout());
            frame.add(panel, BorderLayout.CENTER);
            frame.add(controls, BorderLayout.SOUTH);

            frame.setSize(900, 600);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
