package com.example.demo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.util.ArrayList;

public class ConfirmationCodeController {
    public String email;
    public String passwd;
    public int code;
    @FXML
    public TextField confirmationCodeField;
    public Button confirmButton;

    public void validateCode(ActionEvent actionEvent) {
        if(Integer.parseInt(confirmationCodeField.getText())== code){
            try (MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017/")) {

                MongoDatabase passwordsdb = mongoClient.getDatabase("passwords");
                MongoCollection<Document> passwdCollection = passwordsdb.getCollection("passwords");
                Document credentials = new Document("_id", new ObjectId());
                credentials.append("username", email)
                        .append("Main-password",new Document("hash", CreateAccountController.hashPasswd(passwd)))
                        .append("passwords", new ArrayList<>());
                passwdCollection.insertOne(credentials);

            }
            try {
                // Load the new FXML file
                FXMLLoader loader = new FXMLLoader(getClass().getResource("loginPage.fxml"));
                Parent root = loader.load();

                // Get the current scene and window
                Scene currentScene = confirmButton.getScene();
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
}
