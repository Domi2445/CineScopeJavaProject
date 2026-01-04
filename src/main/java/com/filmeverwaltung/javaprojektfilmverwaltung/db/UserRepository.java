package com.filmeverwaltung.javaprojektfilmverwaltung.db;

import com.filmeverwaltung.javaprojektfilmverwaltung.model.User;

import java.sql.*;
import java.time.LocalDateTime;

/**
 * Einfaches Repository für die vorhandene users-Tabelle.
 */
public class UserRepository
{
    public boolean existsByUsername(String username) throws SQLException
    {
        String sql = "SELECT 1 FROM users WHERE USERNAME = ?";
        try (Connection c = DatabaseManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql))
        {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery())
            {
                return rs.next();
            }
        }
    }

    public boolean existsByEmail(String email) throws SQLException
    {
        String sql = "SELECT 1 FROM users WHERE EMAIL = ?";
        try (Connection c = DatabaseManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql))
        {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery())
            {
                return rs.next();
            }
        }
    }

    public int insertUser(User user) throws SQLException
    {
        String sql = "INSERT INTO users (USERNAME, PASSWORD_HASH, EMAIL, CREATED_AT, LAST_LOGIN, IS_ACTIVE) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, new String[]{"USER_ID"}))
        {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getEmail());
            ps.setTimestamp(4, user.getCreatedAt() != null ? Timestamp.valueOf(user.getCreatedAt()) : Timestamp.valueOf(LocalDateTime.now()));
            if (user.getLastLogin() != null)
                ps.setTimestamp(5, Timestamp.valueOf(user.getLastLogin()));
            else
                ps.setNull(5, Types.TIMESTAMP);
            ps.setInt(6, user.isActive() ? 1 : 0);

            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys())
            {
                if (generatedKeys.next())
                {
                    // Oracle gibt die ID als BigDecimal zurück
                    int id = generatedKeys.getBigDecimal(1).intValue();
                    user.setUserId(id);
                    return id;
                }
                else
                {
                    throw new SQLException("Inserting user failed, no ID obtained.");
                }
            }
        }
    }

    public User findByUsername(String username) throws SQLException
    {
        String sql = "SELECT USER_ID, USERNAME, PASSWORD_HASH, EMAIL, CREATED_AT, LAST_LOGIN, IS_ACTIVE FROM users WHERE USERNAME = ?";
        try (Connection c = DatabaseManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql))
        {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery())
            {
                if (rs.next())
                {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    private User mapRow(ResultSet rs) throws SQLException
    {
        User u = new User();
        u.setUserId(rs.getInt("USER_ID"));
        u.setUsername(rs.getString("USERNAME"));
        u.setPasswordHash(rs.getString("PASSWORD_HASH"));
        u.setEmail(rs.getString("EMAIL"));

        Timestamp t = rs.getTimestamp("CREATED_AT");
        if (t != null) u.setCreatedAt(t.toLocalDateTime());

        t = rs.getTimestamp("LAST_LOGIN");
        if (t != null) u.setLastLogin(t.toLocalDateTime());

        u.setActive(rs.getInt("IS_ACTIVE") != 0);
        return u;
    }
}

