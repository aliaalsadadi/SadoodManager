package com.example.demo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.bson.Document;
import org.bson.types.Binary;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;


public class LoginController {

    @FXML
    private Button noAccountButton;
    @FXML
    private TextField password;
    @FXML
    private TextField username;
    @FXML
    protected void onLoginButtonClick() {
        String enteredPassword = password.getText();
        try (MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017/")) {
            MongoDatabase passwordsdb = mongoClient.getDatabase("passwords");
            MongoCollection<Document> passwdCollection = passwordsdb.getCollection("passwords");

            // Retrieve the document for the user
            Document userDocument = passwdCollection.find(Filters.eq("username", username.getText())).first();
            if (userDocument != null) {
                // Retrieve the stored hash and salt
                Document mainPassword = (Document) userDocument.get("Main-password");
                String storedHash = mainPassword.getString("hash");

                SCryptPasswordEncoder encoder = new SCryptPasswordEncoder(16384, 8, 4, 32, 64);
               if (encoder.matches( enteredPassword,storedHash)){
                   SessionManager.getInstance().setCurrentUser(username.getText());
                   switchtoMain();
               }
            } else {
                // User not found
                // Handle user not found
                System.out.println("User not found!");
            }
        } catch (Exception e) {
            // Handle exception
            e.printStackTrace();
        }
    }
    @FXML
    protected void onNoAccount(){
        try {
            // Load the new FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("create-view.fxml"));
            Parent root = loader.load();

            // Get the current scene and window
            Scene currentScene = noAccountButton.getScene();
            Window currentWindow = currentScene.getWindow();

            // Create a new scene with the loaded FXML file
            Scene newScene = new Scene(root);

            // Set the new scene as the scene for the current stage
            Stage currentStage = (Stage) currentWindow;
            currentStage.setScene(newScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void switchtoMain(){
        try {
            // Load the new FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("main-view.fxml"));
            Parent root = loader.load();

            // Get the current scene and window
            Scene currentScene = noAccountButton.getScene();
            Window currentWindow = currentScene.getWindow();

            // Create a new scene with the loaded FXML file
            Scene newScene = new Scene(root);

            // Set the new scene as the scene for the current stage
            Stage currentStage = (Stage) currentWindow;
            currentStage.setScene(newScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
