package com.filmeverwaltung.javaprojektfilmverwaltung.util;

/**
 * Singleton Session Manager zur Verwaltung des Login-Status
 */
public class SessionManager
{
    private static SessionManager instance;
    private int currentUserId = -1;
    private String currentUsername = null;
    private boolean isLoggedIn = false;

    private SessionManager()
    {
    }

    public static synchronized SessionManager getInstance()
    {
        if (instance == null)
        {
            instance = new SessionManager();
        }
        return instance;
    }

    public void login(int userId, String username)
    {
        this.currentUserId = userId;
        this.currentUsername = username;
        this.isLoggedIn = true;
    }

    public void logout()
    {
        this.currentUserId = -1;
        this.currentUsername = null;
        this.isLoggedIn = false;
    }

    public boolean isLoggedIn()
    {
        return isLoggedIn;
    }

    public int getCurrentUserId()
    {
        return currentUserId;
    }

    public String getCurrentUsername()
    {
        return currentUsername;
    }
}

