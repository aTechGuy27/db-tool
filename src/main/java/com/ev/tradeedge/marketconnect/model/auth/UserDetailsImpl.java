package com.ev.tradeedge.marketconnect.model.auth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserDetailsImpl implements UserDetails {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Long id;
    private final String username;
    private final String password;
    private final boolean isAdmin;

    public UserDetailsImpl(Long id, String username, String password, boolean isAdmin) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.isAdmin = isAdmin;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return isAdmin ? 
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")) : 
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    public Long getId() {
        return id;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}