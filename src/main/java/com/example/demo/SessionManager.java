package com.example.demo;

public class SessionManager {
    private static SessionManager instance;
    private String currentUser;

    private SessionManager() {
        // Private constructor to enforce singleton behavior
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setCurrentUser(String username) {
        currentUser = username;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public void logout() {
        currentUser = null;
    }
}