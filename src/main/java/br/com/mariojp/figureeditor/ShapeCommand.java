package br.com.mariojp.figureeditor;

import java.awt.*;
import java.awt.geom.Rectangle2D;

interface ShapeCommand {
    void execute();
    void undo();
}

class AddShapeCommand implements ShapeCommand {
    private DrawingPanel panel;
    private ColoredShape shape;

    AddShapeCommand(DrawingPanel panel, ColoredShape shape) {
        this.panel = panel;
        this.shape = shape;
    }

    public void execute() { panel.addShapeInternal(shape); }
    public void undo() { panel.removeShapeInternal(shape); }
}

class MoveShapeCommand implements ShapeCommand {
    private ColoredShape shape;
    private Rectangle oldBounds;
    private Rectangle newBounds;

    MoveShapeCommand(ColoredShape shape, Rectangle oldBounds, Rectangle newBounds) {
        this.shape = shape;
        this.oldBounds = oldBounds;
        this.newBounds = newBounds;
    }

    public void execute() { applyBounds(newBounds); }
    public void undo() { applyBounds(oldBounds); }

    private void applyBounds(Rectangle b) {
        if (shape.shape instanceof Rectangle2D.Double) {
            Rectangle2D.Double r = (Rectangle2D.Double) shape.shape;
            r.setFrame(b.x, b.y, b.width, b.height);
        }
    }
}

class DeleteShapeCommand implements ShapeCommand {
    private DrawingPanel panel;
    private ColoredShape shape;

    DeleteShapeCommand(DrawingPanel panel, ColoredShape shape) {
        this.panel = panel;
        this.shape = shape;
    }

    public void execute() { panel.removeShapeInternal(shape); }
    public void undo() { panel.addShapeInternal(shape); }
}
