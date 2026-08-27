package org.least_res_gps.ui;

import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import javafx.application.Application;
import javafx.embed.swing.SwingNode;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.map.awt.graphics.AwtGraphicFactory;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        GraphicFactory graphicFactory = AwtGraphicFactory.INSTANCE;

        Label status = new Label("Mapsforge ready: " + graphicFactory.getClass().getSimpleName());
        SwingNode mapArea = new SwingNode();
        javax.swing.SwingUtilities.invokeLater(() -> {
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(new JLabel("Map view placeholder"), BorderLayout.CENTER);
            mapArea.setContent(panel);
        });

        BorderPane root = new BorderPane();
        root.setTop(status);
        root.setCenter(mapArea);

        primaryStage.setTitle("Least Resistance GPS");
        primaryStage.setScene(new Scene(root, 300, 500));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}