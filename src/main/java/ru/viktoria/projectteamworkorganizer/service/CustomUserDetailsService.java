package ru.viktoria.projectteamworkorganizer.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.viktoria.projectteamworkorganizer.entity.User;
import ru.viktoria.projectteamworkorganizer.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("Пользователь не найден");
        }

        User user = userOptional.get();

        List<GrantedAuthority> authorities = new ArrayList<>();

        if (user.getSystemRole() != null) {
            String roleName = "ROLE_" + user.getSystemRole().getType().name();
            GrantedAuthority authority = new SimpleGrantedAuthority(roleName);
            authorities.add(authority);
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .disabled(user.isDeleted())
                .build();
    }
}
