package br.com.mariojp.figureeditor;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Figure Editor");
            DrawingPanel panel = new DrawingPanel();

            JButton corBtn = new JButton("Cor...");
            corBtn.addActionListener(e -> {
                Color c = JColorChooser.showDialog(frame, "Escolha a cor", panel.getCurrentColor());
                if (c != null) panel.setCurrentColor(c);
            });

            JButton exportPng = new JButton("Exportar PNG");
            exportPng.addActionListener(e -> {
                JFileChooser chooser = new JFileChooser();
                if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    try { panel.exportToPNG(file); } catch (Exception ex) { ex.printStackTrace(); }
                }
            });

            JButton exportSvg = new JButton("Exportar SVG");
            exportSvg.addActionListener(e -> {
                JFileChooser chooser = new JFileChooser();
                if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    try { panel.exportToSVG(file); } catch (Exception ex) { ex.printStackTrace(); }
                }
            });

            JButton undoBtn = new JButton("Undo");
            undoBtn.addActionListener(e -> panel.undo());

            JButton redoBtn = new JButton("Redo");
            redoBtn.addActionListener(e -> panel.redo());

            JPanel top = new JPanel();
            top.add(corBtn);
            top.add(exportPng);
            top.add(exportSvg);
            top.add(undoBtn);
            top.add(redoBtn);

            frame.add(top, BorderLayout.NORTH);
            frame.add(panel, BorderLayout.CENTER);

            frame.setSize(800, 600);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }
}

