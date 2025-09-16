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

    private final List<ColoredShape> shapes = new ArrayList<>();
    private CommandManager cmdManager = new CommandManager();

    private Point startDrag = null;
    private Point endDrag = null;

    private int selectedIndex = -1;
    private boolean draggingShape = false;
    private Point dragOffset = null;

    private Color currentColor = new Color(30,144,255);

    private static final int GRID_SIZE = 20;
    private static final int MIN_SIZE = 10;

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
                        ColoredShape cs = new ColoredShape(s, currentColor);
                        cmdManager.executeCommand(new AddShapeCommand(DrawingPanel.this, cs));
                    }
                }
                startDrag = null;
                endDrag = null;
                repaint();
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
        Rectangle oldBounds = s.getBounds();

        int w = oldBounds.width;
        int h = oldBounds.height;
        int newX = mousePt.x - (dragOffset != null ? dragOffset.x : w / 2);
        int newY = mousePt.y - (dragOffset != null ? dragOffset.y : h / 2);

        newX = Math.round(newX / (float)GRID_SIZE) * GRID_SIZE;
        newY = Math.round(newY / (float)GRID_SIZE) * GRID_SIZE;

        Rectangle newBounds = new Rectangle(newX, newY, w, h);

        cmdManager.executeCommand(new MoveShapeCommand(cs, oldBounds, newBounds));
        repaint();
    }

    private Shape makeEllipse(Point p1, Point p2) {
        int x = Math.min(p1.x, p2.x);
        int y = Math.min(p1.y, p2.y);
        int w = Math.max(MIN_SIZE, Math.abs(p1.x - p2.x));
        int h = Math.max(MIN_SIZE, Math.abs(p1.y - p2.y));
        return new Ellipse2D.Double(x, y, w, h);
    }

    void addShapeInternal(ColoredShape cs) {
        shapes.add(cs);
        repaint();
    }

    void removeShapeInternal(ColoredShape cs) {
        shapes.remove(cs);
        repaint();
    }

    List<ColoredShape> getShapes() {
        return shapes;
    }

    public void undo() { cmdManager.undo(); repaint(); }
    public void redo() { cmdManager.redo(); repaint(); }
    
    public Color getCurrentColor() {
        return currentColor;
    }

    public void setCurrentColor(Color c) {
        this.currentColor = c;
    }

    public void exportToPNG(File file) throws IOException {
        Utils.exportToPNG(this, file);
    }

    public void exportToSVG(File file) throws IOException {
        Utils.exportToSVG(this, file);
    }


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
}
