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
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import javafx.stage.Stage;
import javafx.stage.Window;
import org.bson.Document;
import org.bson.types.Binary;
import org.bson.types.ObjectId;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;

import javax.mail.MessagingException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.regex.Pattern;


public class CreateAccountController {
    @FXML
    private ImageView sorry;
    @FXML
    private Label sorryLabel;
    @FXML
    private Hyperlink loginLink;
    @FXML
    private Label invalidLabel;
    @FXML
    private Label userLabel;
    @FXML
    private Label passwdLabel;
    @FXML
    public Button CreateAccountButton;
    @FXML
    private TextField username;
    @FXML
    private TextField password;

    @FXML
    protected void CreateAccount(ActionEvent actionEvent) {
        if(username.getText().isEmpty() && password.getText().isEmpty()){
            username.setText("must not be empty");
            username.setStyle("-fx-text-fill: red;");
            password.setText("must not be empty");
            password.setStyle("-fx-text-fill: red;");
            return;
        }
        else if(password.getText().isEmpty()){
            password.setText("must not be empty");
            password.setStyle("-fx-text-fill: red;");
            return;
        } else if (username.getText().isEmpty()) {
            username.setText("must not be empty");
            username.setStyle("-fx-text-fill: red;");
            return;
        }else{
            if (!isEmailValid(username.getText())){
                invalidLabel.setStyle("visibility: true;-fx-alignment: center; -fx-border-color: black; -fx-border-radius: 3; -fx-label-padding: 3;");
                return;
            }
            if (!isPasswordStrong(password.getText())){
                passwdLabel.setStyle("visibility: true;");
                return;
            }
            try {

                if(isUsernameTaken(username.getText())){
                    sorry.setStyle("visibility: true;");
                    sorryLabel.setStyle("visibility: true;");
                    loginLink.setStyle("visibility: true;");
                    return;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        switchtoConfirm(username.getText(), password.getText());

    }
    public static String hashPasswd(String passwd) {
        // Hash the salted password
        SCryptPasswordEncoder encoder = new SCryptPasswordEncoder(16384, 8, 4, 32, 64);

        return encoder.encode(passwd);
    }
    @FXML
    protected void clear(MouseEvent mouseEvent) {
        if (password.getText().equals("must not be empty")){
            password.setText("");
            password.setStyle("-fx-text-fill: black;");
        }
        if (username.getText().equals("must not be empty")){
            username.setText("");
            username.setStyle("-fx-text-fill: black;");
        }
    }

    public static boolean isPasswordStrong(String password) {
        // Check password length
        if (password.length() < 8) {
            return false;
        }

        // Check for at least one uppercase letter, one lowercase letter, and one digit
        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;

        for (char ch : password.toCharArray()) {
            if (Character.isUpperCase(ch)) {
                hasUppercase = true;
            } else if (Character.isLowerCase(ch)) {
                hasLowercase = true;
            } else if (Character.isDigit(ch)) {
                hasDigit = true;
            }
        }

        // Check if all criteria are met
        return hasUppercase && hasLowercase && hasDigit;
    }

    public void hideLabel(KeyEvent keyEvent) {
        if (keyEvent.getTarget() == username){
            invalidLabel.setStyle("visibility: false");
            sorry.setStyle("visibility: false;");
            sorryLabel.setStyle("visibility: false;");
            loginLink.setStyle("visibility: false;");
        }
        if (keyEvent.getTarget() == password){
            passwdLabel.setStyle("visibility: false");
        }
    }
    private boolean isUsernameTaken(String username) {
        try (MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017/")) {
            MongoDatabase passwordsdb = mongoClient.getDatabase("passwords");
            MongoCollection<Document> passwdCollection = passwordsdb.getCollection("passwords");

            Document query = new Document("username", username);
            long count = passwdCollection.countDocuments(query);
            return count > 0;
        }

    }

    private boolean isEmailValid(String email) {
        // Regular expression pattern for email validation
        String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

        // Create a Pattern object with the email regex
        Pattern pattern = Pattern.compile(emailRegex);

        // Check if the email matches the pattern
        return pattern.matcher(email).matches();
    }
    @FXML
    protected void switchtoLogin(ActionEvent actionEvent) {
        try {
            // Load the new FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("loginPage.fxml"));
            Parent root = loader.load();

            // Get the current scene and window
            Scene currentScene = CreateAccountButton.getScene();
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
    protected void switchtoConfirm(String toEmailAddres, String passwd){
        try {
            // Load the new FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("confirm-view.fxml"));
            Parent root = loader.load();
            ConfirmationCodeController confirmationCodeController = loader.getController();
            int code =  generateRandomNumber();
            confirmationCodeController.code = code;
            confirmationCodeController.email = toEmailAddres;
            confirmationCodeController.passwd = passwd;
            Gmailer gmailer = new Gmailer();
            String subject = "Confirm Your Email";
            String message = "Please confirm your email for your new Meta account by entering the following confirmation code.\n\nConfirmation code: "+code+"\n\nIf you did not request a confirmation code, you can disregard this message.";

            gmailer.sendMail(subject, message, toEmailAddres);
            // Get the current scene and window
            Scene currentScene = CreateAccountButton.getScene();
            Window currentWindow = currentScene.getWindow();

            // Create a new scene with the loaded FXML file
            Scene newScene = new Scene(root);

            // Set the new scene as the scene for the current stage
            Stage currentStage = (Stage) currentWindow;
            currentStage.setScene(newScene);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }
    public static int generateRandomNumber() {
        Random random = new Random();
        return random.nextInt(90000) + 10000;
    }

}