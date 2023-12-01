package com.example.demo;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Draft;
import com.google.api.services.gmail.model.Message;
import org.apache.commons.codec.binary.Base64;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;




public class Gmailer {
    private final Gmail service;
    public Gmailer() throws GeneralSecurityException, IOException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        GsonFactory JsonFactory = GsonFactory.getDefaultInstance();
        service = new Gmail.Builder(HTTP_TRANSPORT, JsonFactory, getCredentials(HTTP_TRANSPORT, JsonFactory))
                .setApplicationName("test mailer")
                .build();
    }
    private static final String fromEmailAddress = "example@gmail.com"; //if facing errors -> setup the gmail api first in google cloud console
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT, GsonFactory jsonFactory)
            throws IOException {
        // Load client secrets.

        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(jsonFactory, new InputStreamReader(Gmailer.class.getResourceAsStream("client_secret.json")));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, jsonFactory, clientSecrets, Set.of(GmailScopes.GMAIL_SEND,GmailScopes.GMAIL_COMPOSE))
                .setDataStoreFactory(new FileDataStoreFactory(Paths.get("tokens").toFile()))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        //returns an authorized Credential object.
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
    public void sendMail(String subject, String msg, String toEmailAddress) throws GeneralSecurityException, IOException, MessagingException {


        // Encode as MIME message
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage email = new MimeMessage(session);
        email.setFrom(new InternetAddress(fromEmailAddress));
        email.addRecipient(javax.mail.Message.RecipientType.TO,
                new InternetAddress(toEmailAddress));
        email.setSubject(subject);

        email.setText(msg);

        // Encode and wrap the MIME message into a gmail message
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        email.writeTo(buffer);
        byte[] rawMessageBytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeBase64URLSafeString(rawMessageBytes);
        Message message = new Message();
        message.setRaw(encodedEmail);

        try {
            message = service.users().messages().send("me", message).execute();

        } catch (GoogleJsonResponseException e) {
            // TODO(developer) - handle error appropriately
            GoogleJsonError error = e.getDetails();
            if (error.getCode() == 403) {
                System.err.println("Unable to create draft: " + e.getDetails());
            } else {
                throw e;
            }
        }
    }

//    public static void main(String[] args) throws GeneralSecurityException, IOException, MessagingException {
//        Gmailer gmailer = new Gmailer();
//        String subject = "Confirm Your Email";
//        String message = "Please confirm your email for your new Meta account by entering the following confirmation code.\n\nConfirmation code: 057233\n\nIf you did not request a confirmation code, you can disregard this message.";
//
//        gmailer.sendMail(subject, message);
//    }
}
