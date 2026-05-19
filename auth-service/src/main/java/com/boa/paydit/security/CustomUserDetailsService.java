package com.boa.paydit.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boa.paydit.entity.UserEntity;
import com.boa.paydit.repository.UserRepository;

@Service
public class CustomUserDetailsService
implements UserDetailsService {

    private final UserRepository repository;

    public CustomUserDetailsService(
        UserRepository repository) {

        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(
        String username)
    throws UsernameNotFoundException {

        UserEntity user = repository
            .findByUsername(username)
            .orElseThrow(
                () -> new UsernameNotFoundException(
                    "User not found: " + username));

        List<GrantedAuthority> authorities =
            new ArrayList<>();

        if (user.getRole() != null
            && !user.getRole().isBlank()) {

            authorities.add(
                new SimpleGrantedAuthority(
                    "ROLE_" + user.getRole()));
        }

        return User.withUsername(user.getUsername())
            .password(user.getPassword())
            .authorities(authorities)
            .build();
    }
}
