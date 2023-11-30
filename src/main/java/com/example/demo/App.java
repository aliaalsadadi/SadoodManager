package com.example.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;

public class App extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Load the FXML file
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("loginPage.fxml"));
        VBox root = fxmlLoader.load();
        // Create an ImageView for the logo
        Image image = new Image(App.class.getResource("logo.png").toExternalForm());

        stage.getIcons().add(image);

        // Create the scene
        Scene scene = new Scene(root, 640, 640);
        // Set the stage properties
        stage.setTitle("SadoodManager");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}