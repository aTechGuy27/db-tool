package com.ev.tradeedge.marketconnect.service;


import java.util.Map;
import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ev.tradeedge.marketconnect.model.auth.UserDetailsImpl;
import com.ev.tradeedge.marketconnect.repository.UserRepository;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Map<String, Object>> userOpt = userRepository.findByUsername(username);
        
        if (userOpt.isEmpty()) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        
        Map<String, Object> user = userOpt.get();
        
        return new UserDetailsImpl(
            ((Number) user.get("user_id")).longValue(),
            user.get("username").toString(),
            user.get("password").toString(),
            (Boolean) user.get("is_admin")
        );
    }
}