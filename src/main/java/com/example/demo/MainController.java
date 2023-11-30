package com.example.demo;

import com.gluonhq.charm.glisten.control.CardPane;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import org.bouncycastle.oer.its.BitmapSsp;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    private static final String mongoUrl = "mongodb://localhost:27017/";
    public TextField searchField;
    @FXML
    private TextField addEmail;
    @FXML
    private TextField addPasswd;
    @FXML
    private TextField addUrl;
    @FXML
    private CardPane cardPane;
    private static final String SECRET_KEY = "mySecretKey";
    private static final String SALT = "mySalt";
    private static final String INIT_VECTOR = "myInitVector1234";

    public static String encrypt(String input) throws Exception {
        SecretKey secretKey = generateSecretKey();
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(INIT_VECTOR.getBytes(StandardCharsets.UTF_8));

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
        byte[] encryptedBytes = cipher.doFinal(input.getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static String decrypt(String encryptedInput) throws Exception {
        SecretKey secretKey = generateSecretKey();
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(INIT_VECTOR.getBytes(StandardCharsets.UTF_8));

        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedInput));

        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    private static SecretKey generateSecretKey() throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(SECRET_KEY.toCharArray(), SALT.getBytes(StandardCharsets.UTF_8), 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        getPasswds();
    }
//
//    public void addLabelToCardPane(String labelText) {
//        Label label = new Label(labelText);
//        label.setStyle("-fx-padding: 10;");
//        cardPane.getItems().add(label);
//    }

    public void getPasswds(){
        try (MongoClient mongoClient = MongoClients.create(mongoUrl)) {
            String currentUser = SessionManager.getInstance().getCurrentUser();
            MongoDatabase passwordsdb = mongoClient.getDatabase("passwords");
            MongoCollection<Document> passwdCollection = passwordsdb.getCollection("passwords");
            Document userDocument = passwdCollection.find(Filters.eq("username", currentUser)).first();
            List<Document> passwds = userDocument.getList("passwords",Document.class);
            for (Document passwd: passwds){
                addCard(cardPane, passwd.get("email").toString(), passwd.get("url").toString(),decrypt(passwd.get("passwd").toString()));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public void onAdd(ActionEvent actionEvent) {
        try (MongoClient mongoClient = MongoClients.create(mongoUrl)) {
            String currentUser = SessionManager.getInstance().getCurrentUser();
            MongoDatabase passwordsdb = mongoClient.getDatabase("passwords");
            MongoCollection<Document> passwdCollection = passwordsdb.getCollection("passwords");
            Document userDocument = passwdCollection.find(Filters.eq("username", currentUser)).first();
            if (userDocument != null){
                List<Document> passwds = userDocument.getList("passwords",Document.class);
                Document creds = new Document();
                creds.append("url", addUrl.getText());
                creds.append("email",addEmail.getText());
                creds.append("passwd", encrypt(addPasswd.getText())
                );
                passwds.add(creds);
                // Update the document in the collection
                Bson filter = Filters.eq("_id", userDocument.getObjectId("_id"));
                Bson update = Updates.set("passwords", passwds);
                UpdateResult updateResult = passwdCollection.updateOne(filter, update);
                if (updateResult.getModifiedCount() > 0) {
                    // Document updated successfully
                    System.out.println("success");
                } else {
                    // Failed to update the document
                    System.out.println("try again");
                }
                addCard(cardPane,addEmail.getText(), addUrl.getText(), addPasswd.getText());
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private void copyToClipboard(String text) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        clipboard.setContent(content);
    }
    public void addCard(CardPane cardPane , String userText, String urlText, String paswdText){
        VBox root = new VBox();
        root.setPrefHeight(166.0);
        root.setPrefWidth(361.0);
        root.setStyle("-fx-padding: 10px; -fx-spacing: 20px;");

        Label label = new Label();
        label.setText(urlText);
        label.setAlignment(Pos.CENTER);
        label.setContentDisplay(javafx.scene.control.ContentDisplay.CENTER);
        label.setPrefHeight(18.0);
        label.setPrefWidth(398.0);
        label.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        VBox innerContainer = new VBox();
        innerContainer.setPrefHeight(200.0);
        innerContainer.setPrefWidth(100.0);
        innerContainer.setSpacing(5.0);
        VBox.setVgrow(innerContainer, Priority.ALWAYS);

        GridPane gridPane1 = new GridPane();
        gridPane1.setMaxWidth(Double.MAX_VALUE);
        gridPane1.setHgap(5.0);

        ColumnConstraints columnConstraints1 = new ColumnConstraints();
        columnConstraints1.setHgrow(Priority.ALWAYS);
        gridPane1.getColumnConstraints().addAll(columnConstraints1);

        TextField emailField = new TextField();
        emailField.setText(userText);
        emailField.setEditable(false);
        emailField.setMaxHeight(Double.MAX_VALUE);
        emailField.setStyle("-fx-background-color: #D3D3D3;");
        emailField.setFont(new javafx.scene.text.Font("Bauhaus 93", 12.0));

        Button copy = new Button();
        copy.setOnAction(event -> {
            String emailText = emailField.getText();
            copyToClipboard(emailText);
        });
        copy.setMinWidth(39.0);
        copy.setMnemonicParsing(false);
        copy.setPrefHeight(48.0);
        copy.setPrefWidth(54.0);
        copy.getStyleClass().add("add-button");
        copy.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        ImageView imageView1 = new ImageView();
        imageView1.setFitHeight(43.0);
        imageView1.setFitWidth(137.0);
        imageView1.setPickOnBounds(true);
        imageView1.setPreserveRatio(true);
        Image image1 = new Image(getClass().getResource("copy.png").toExternalForm());
        imageView1.setImage(image1);

        copy.setGraphic(imageView1);

        gridPane1.addRow(0, emailField, copy);
        GridPane.setHgrow(emailField, Priority.ALWAYS);
        GridPane.setHalignment(copy, HPos.CENTER);

        HBox hbox2 = new HBox();
        hbox2.setSpacing(5.0);
        TextField passwdFieldtxt = new TextField();
        passwdFieldtxt.setVisible(false);
        passwdFieldtxt.setText(paswdText);
        passwdFieldtxt.setEditable(false);
        passwdFieldtxt.setMaxHeight(Double.MAX_VALUE);
        passwdFieldtxt.setStyle("-fx-background-color: #D3D3D3;");
        PasswordField passwdField = new PasswordField();
        CheckBox toggle = new CheckBox();
        toggle.setOnAction(event -> {
            boolean isSelected = toggle.isSelected();
            if (!isSelected) {
                passwdField.setVisible(true);
                passwdFieldtxt.setVisible(false);
            } else {
                passwdField.setVisible(false);
                passwdFieldtxt.setVisible(true);
            }
        });
        passwdField.setText(paswdText);
        passwdField.setEditable(false);
        passwdField.setMaxHeight(Double.MAX_VALUE);
        passwdField.setStyle("-fx-background-color: #D3D3D3;");
        HBox.setHgrow(passwdField, Priority.ALWAYS);

        Button copy2 = new Button();
        copy2.setOnAction(event -> {
            String passwordText = passwdField.getText();
            copyToClipboard(passwordText);
        });
        copy2.setMinWidth(53.0);
        copy2.setMnemonicParsing(false);
        copy2.setPrefHeight(48.0);
        copy2.setPrefWidth(53.0);
        copy2.getStyleClass().add("add-button");
        copy2.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        ImageView imageView2 = new ImageView();
        imageView2.setFitHeight(43.0);
        imageView2.setFitWidth(53.0);
        imageView2.setPickOnBounds(true);
        imageView2.setPreserveRatio(true);
        Image image2 = new Image(getClass().getResource("copy.png").toExternalForm());
        imageView2.setImage(image2);

        copy2.setGraphic(imageView2);

        hbox2.getChildren().addAll(passwdField,passwdFieldtxt,toggle,copy2);
        HBox.setHgrow(hbox2, Priority.ALWAYS);

        innerContainer.getChildren().addAll(gridPane1, hbox2);

        root.getChildren().addAll(label, innerContainer);
        cardPane.getItems().add(root);
    }

    public void search(KeyEvent keyEvent) {
        String searchText = searchField.getText().toLowerCase();
        ObservableList<Node> items = cardPane.getItems();

        List<Node> visibleItems = new ArrayList<>();
        for (Node item : items) {
                VBox vbox = (VBox) item;
                ObservableList<Node> children = vbox.getChildren();
                Label url = (Label) children.get(0);
                VBox hbox = (VBox) children.get(1);
                GridPane fields = (GridPane) hbox.getChildren().get(0);
                TextField user = (TextField) fields.getChildren().get(0);
                boolean isMatch = searchText.isEmpty() || url.getText().toLowerCase().contains(searchText) || user.getText().toLowerCase().contains(searchText);
                item.setVisible(isMatch);
                if (isMatch) {
                    visibleItems.add(item);
                }

        }

        items.removeAll(visibleItems);
        items.addAll(0, visibleItems);
    }

    private int getVisibleItemsInsertIndex(ObservableList<Node> items) {
        int insertIndex = 0;
        for (Node item : items) {
            if (!item.isVisible()) {
                break;
            }
            insertIndex++;
        }
        return insertIndex;
    }
}