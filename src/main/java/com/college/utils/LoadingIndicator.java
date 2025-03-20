package com.college.utils;

import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.scene.Node;
import javafx.scene.Parent;
import java.lang.reflect.Method;
import java.util.List;

public class LoadingIndicator {
    private static StackPane createOverlay() {
        ProgressIndicator progress = new ProgressIndicator();
        progress.setMaxSize(100, 100);

        StackPane overlay = new StackPane(progress);
        overlay.setBackground(new Background(new BackgroundFill(
            Color.rgb(0, 0, 0, 0.3), null, null
        )));
        overlay.setVisible(false);

        return overlay;
    }

    public static void wrap(Region content) {
        StackPane overlay = createOverlay();
        Parent parent = content.getParent();
        
        if (parent instanceof StackPane stackPane) {
            stackPane.getChildren().add(overlay);
        } else {
            StackPane wrapper = new StackPane();
            wrapper.getChildren().addAll(content, overlay);
            
            if (parent != null) {
                int index = parent.getChildrenUnmodifiable().indexOf(content);
                if (index >= 0) {
                    try {
                        Method getChildrenMethod = Parent.class.getDeclaredMethod("getChildren");
                        getChildrenMethod.setAccessible(true);
                        @SuppressWarnings("unchecked")
                        List<Node> children = (List<Node>) getChildrenMethod.invoke(parent);
                        children.set(index, wrapper);
                    } catch (Exception e) {
                        System.err.println("Failed to wrap content: " + e.getMessage());
                    }
                }
            }
        }
    }

    public static void show(Node content) {
        StackPane overlay = findOverlay(content);
        if (overlay != null) {
            overlay.setVisible(true);
        }
    }

    public static void hide(Node content) {
        StackPane overlay = findOverlay(content);
        if (overlay != null) {
            overlay.setVisible(false);
        }
    }

    private static StackPane findOverlay(Node content) {
        Parent parent = content.getParent();
        if (parent instanceof StackPane stackPane) {
            return (StackPane) stackPane.getChildren().stream()
                .filter(node -> node instanceof StackPane && node != content)
                .findFirst()
                .orElse(null);
        }
        return null;
    }
}
