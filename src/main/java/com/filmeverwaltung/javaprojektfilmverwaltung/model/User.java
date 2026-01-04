package com.filmeverwaltung.javaprojektfilmverwaltung.model;

/**
 * Model-Klasse für einen Benutzer
 */
public class User
{
    private int userId;
    private String username;
    private String password;
    private String language;
    private boolean darkMode;

    public User()
    {
    }

    public User(int userId, String username, String password, String language, boolean darkMode)
    {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.language = language;
        this.darkMode = darkMode;
    }

    public User(String username, String password)
    {
        this.username = username;
        this.password = password;
        this.language = "de";
        this.darkMode = false;
    }

    // Getter und Setter
    public int getUserId()
    {
        return userId;
    }

    public void setUserId(int userId)
    {
        this.userId = userId;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getLanguage()
    {
        return language;
    }

    public void setLanguage(String language)
    {
        this.language = language;
    }

    public boolean isDarkMode()
    {
        return darkMode;
    }

    public void setDarkMode(boolean darkMode)
    {
        this.darkMode = darkMode;
    }

    @Override
    public String toString()
    {
        return "User{" + "userId=" + userId + ", username='" + username + '\'' + ", language='" + language + '\'' + ", darkMode=" + darkMode + '}';
    }
}

