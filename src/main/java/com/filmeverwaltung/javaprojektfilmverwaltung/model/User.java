package com.filmeverwaltung.javaprojektfilmverwaltung.model;

import java.time.LocalDateTime;

/**
 * Model-Klasse für einen Benutzer — angepasst an die DB-Spalten.
 */
public class User
{
    private int userId;
    private String username;
    private String passwordHash;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private boolean active;

    public User()
    {
    }

    public User(int userId, String username, String passwordHash, String email, LocalDateTime createdAt, LocalDateTime lastLogin, boolean active)
    {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
        this.active = active;
    }

    public User(String username, String passwordHash)
    {
        this.username = username;
        this.passwordHash = passwordHash;
        this.active = true;
        this.createdAt = LocalDateTime.now();
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

    public String getPasswordHash()
    {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash)
    {
        this.passwordHash = passwordHash;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public LocalDateTime getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt)
    {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastLogin()
    {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin)
    {
        this.lastLogin = lastLogin;
    }

    public boolean isActive()
    {
        return active;
    }

    public void setActive(boolean active)
    {
        this.active = active;
    }

    @Override
    public String toString()
    {
        return "User{" + "userId=" + userId + ", username='" + username + '\'' + ", email='" + email + '\'' + ", createdAt=" + createdAt + ", lastLogin=" + lastLogin + ", active=" + active + '}';
    }
}
