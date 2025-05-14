package com.ev.tradeedge.marketconnect.repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Map<String, Object>> findByUsername(String username) {
        String sql = "SELECT * FROM public.users WHERE username = ?";
        List<Map<String, Object>> users = jdbcTemplate.queryForList(sql, username);
        
        if (users.isEmpty()) {
            return Optional.empty();
        }
        
        return Optional.of(users.get(0));
    }

    public Long save(String username, String password, String fullName, boolean isAdmin) {
        String sql = "INSERT INTO public.users (username, password, full_name, is_admin, created_at) VALUES (?, ?, ?, ?, ?)";
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            // Change this line to specify the primary key column name
            PreparedStatement ps = connection.prepareStatement(sql, new String[] {"user_id"});
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, fullName);
            ps.setBoolean(4, isAdmin);
            ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            return ps;
        }, keyHolder);
        
        // Change this line to get the key by column name
        return ((Number) keyHolder.getKeys().get("user_id")).longValue();
    }

    public void updateLastLogin(String username) {
        String sql = "UPDATE public.users SET last_login = ? WHERE username = ?";
        jdbcTemplate.update(sql, new Timestamp(System.currentTimeMillis()), username);
    }

    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM public.users WHERE username = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username);
        return count != null && count > 0;
    }
}