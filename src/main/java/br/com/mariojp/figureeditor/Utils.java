package br.com.mariojp.figureeditor;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

class Utils {
    static void exportToPNG(DrawingPanel panel, File file) throws IOException {
        BufferedImage img = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        panel.paint(g2);
        g2.dispose();
        ImageIO.write(img, "png", file);
    }

    static void exportToSVG(DrawingPanel panel, File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"" + panel.getWidth() +
                         "\" height=\"" + panel.getHeight() + "\">\n");
            for (ColoredShape cs : panel.getShapes()) {
                Rectangle bounds = cs.shape.getBounds();
                writer.write(String.format(
                    "<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" fill=\"%s\" />\n",
                    bounds.x, bounds.y, bounds.width, bounds.height, toHex(cs.color)
                ));
            }
            writer.write("</svg>");
        }
    }

    static String toHex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }
}
