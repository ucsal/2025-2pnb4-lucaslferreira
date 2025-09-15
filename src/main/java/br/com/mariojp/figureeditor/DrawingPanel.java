package br.com.mariojp.figureeditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;

class DrawingPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final int MIN_SIZE = 10;

    private final List<ColoredShape> shapes = new ArrayList<>();
    private Point startDrag = null;
    private Point endDrag = null;

    private int selectedIndex = -1;
    private boolean draggingShape = false;
    private Point dragOffset = null;

    private Color currentColor = new Color(30,144,255);

    private static final int GRID_SIZE = 20;

    DrawingPanel() {
        setBackground(Color.WHITE);
        setOpaque(true);
        setDoubleBuffered(true);

        MouseAdapter mouse = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                int idx = findTopmostShapeIndex(p);
                if (idx != -1) {
                    selectedIndex = idx;
                    draggingShape = true;
                    Shape s = shapes.get(selectedIndex).shape;
                    Rectangle bounds = s.getBounds();
                    dragOffset = new Point(p.x - bounds.x, p.y - bounds.y);
                    startDrag = null;
                    endDrag = null;
                } else {
                    startDrag = p;
                    endDrag = p;
                    selectedIndex = -1;
                    draggingShape = false;
                    dragOffset = null;
                }
                repaint();
            }

            @Override public void mouseDragged(MouseEvent e) {
                if (draggingShape && selectedIndex != -1) {
                    moveShapeTo(selectedIndex, e.getPoint());
                } else if (startDrag != null) {
                    endDrag = e.getPoint();
                }
                repaint();
            }

            @Override public void mouseReleased(MouseEvent e) {
                if (draggingShape) {
                    draggingShape = false;
                    dragOffset = null;
                } else if (startDrag != null && endDrag != null) {
                    Shape s = makeEllipse(startDrag, endDrag);
                    if (s != null) {
                        shapes.add(new ColoredShape(s, currentColor));
                    }
                }
                startDrag = null;
                endDrag = null;
                repaint();
            }

            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    Point p = e.getPoint();
                    int idx = findTopmostShapeIndex(p);
                    if (idx != -1) {
                        selectedIndex = idx;
                    } else {
                        selectedIndex = -1;
                    }
                    repaint();
                }
            }
        };

        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    private int findTopmostShapeIndex(Point p) {
        for (int i = shapes.size() - 1; i >= 0; i--) {
            if (shapes.get(i).shape.contains(p)) return i;
        }
        return -1;
    }

    private void moveShapeTo(int index, Point mousePt) {
        if (index < 0 || index >= shapes.size()) return;
        ColoredShape cs = shapes.get(index);
        Shape s = cs.shape;
        Rectangle bounds = s.getBounds();
        int w = bounds.width;
        int h = bounds.height;
        int newX = mousePt.x - (dragOffset != null ? dragOffset.x : w / 2);
        int newY = mousePt.y - (dragOffset != null ? dragOffset.y : h / 2);

        newX = Math.round(newX / (float)GRID_SIZE) * GRID_SIZE;
        newY = Math.round(newY / (float)GRID_SIZE) * GRID_SIZE;

        if (s instanceof Ellipse2D.Double) {
            Ellipse2D.Double e = (Ellipse2D.Double) s;
            e.setFrame(newX, newY, w, h);
        } else if (s instanceof Rectangle2D.Double) {
            Rectangle2D.Double r = (Rectangle2D.Double) s;
            r.setFrame(newX, newY, w, h);
        } else {
            shapes.set(index, new ColoredShape(new Ellipse2D.Double(newX, newY, w, h), cs.color));
        }
    }

    private Shape makeEllipse(Point p1, Point p2) {
        int x = Math.min(p1.x, p2.x);
        int y = Math.min(p1.y, p2.y);
        int w = Math.max(MIN_SIZE, Math.abs(p1.x - p2.x));
        int h = Math.max(MIN_SIZE, Math.abs(p1.y - p2.y));
        return new Ellipse2D.Double(x, y, w, h);
    }

    void clear() {
        shapes.clear();
        selectedIndex = -1;
        repaint();
    }

    private static class ColoredShape {
        Shape shape;
        Color color;
        ColoredShape(Shape s, Color c) { this.shape = s; this.color = c; }
    }

    public Color getCurrentColor() { return currentColor; }
    public void setCurrentColor(Color c) { this.currentColor = c; }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (int i = 0; i < shapes.size(); i++) {
            ColoredShape cs = shapes.get(i);
            g2.setColor(cs.color);
            g2.fill(cs.shape);
            g2.setColor(new Color(0,0,0,120));
            g2.setStroke(new BasicStroke(1.2f));
            g2.draw(cs.shape);
            if (i == selectedIndex) {
                Stroke old = g2.getStroke();
                float[] dash = {6f, 4f};
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT,
                                             BasicStroke.JOIN_BEVEL, 0, dash, 0));
                g2.setColor(Color.BLACK);
                g2.draw(cs.shape);
                g2.setStroke(old);
            }
        }

        if (startDrag != null && endDrag != null && !draggingShape) {
            Shape r = makeEllipse(startDrag, endDrag);
            float[] dash = {5f, 5f};
            g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT,
                                         BasicStroke.JOIN_BEVEL, 0, dash, 0));
            g2.setColor(Color.GRAY);
            g2.draw(r);
        }

        g2.dispose();
    }

    void exportToPNG(File file) throws IOException {
        BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        paint(g2);
        g2.dispose();
        ImageIO.write(img, "png", file);
    }

    void exportToSVG(File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
            writer.write("<svg xmlns=\"http://www.w3.org/2000/svg\" " +
                         "width=\"" + getWidth() + "\" height=\"" + getHeight() + "\">\n");
            for (int i = 0; i < shapes.size(); i++) {
                ColoredShape cs = shapes.get(i);
                if (cs.shape instanceof Ellipse2D.Double) {
                    Ellipse2D.Double e = (Ellipse2D.Double) cs.shape;
                    writer.write(String.format(
                        "<ellipse cx=\"%f\" cy=\"%f\" rx=\"%f\" ry=\"%f\" fill=\"%s\" stroke=\"black\" stroke-width=\"1\" />\n",
                        e.getCenterX(), e.getCenterY(),
                        e.width / 2, e.height / 2,
                        toHex(cs.color)));
                } else if (cs.shape instanceof Rectangle2D.Double) {
                    Rectangle2D.Double r = (Rectangle2D.Double) cs.shape;
                    writer.write(String.format(
                        "<rect x=\"%f\" y=\"%f\" width=\"%f\" height=\"%f\" fill=\"%s\" stroke=\"black\" stroke-width=\"1\" />\n",
                        r.x, r.y, r.width, r.height,
                        toHex(cs.color)));
                }
            }
            writer.write("</svg>");
        }
    }

    private String toHex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }
}
