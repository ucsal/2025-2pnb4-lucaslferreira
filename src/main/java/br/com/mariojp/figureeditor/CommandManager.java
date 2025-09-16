package br.com.mariojp.figureeditor;

import java.util.Stack;

class CommandManager {
    private Stack<ShapeCommand> undoStack = new Stack<>();
    private Stack<ShapeCommand> redoStack = new Stack<>();

    void executeCommand(ShapeCommand cmd) {
        cmd.execute();
        undoStack.push(cmd);
        redoStack.clear();
    }

    void undo() {
        if (!undoStack.isEmpty()) {
            ShapeCommand cmd = undoStack.pop();
            cmd.undo();
            redoStack.push(cmd);
        }
    }

    void redo() {
        if (!redoStack.isEmpty()) {
            ShapeCommand cmd = redoStack.pop();
            cmd.execute();
            undoStack.push(cmd);
        }
    }
}

