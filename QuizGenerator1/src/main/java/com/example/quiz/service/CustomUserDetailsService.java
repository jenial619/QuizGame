package com.example.quiz.service;

import com.example.quiz.model.User;
import com.example.quiz.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import java.util.List;

public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  public CustomUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  // A constructor used by SecurityConfig bean above when repo injection isn't automatic
  public CustomUserDetailsService() {
    this.userRepository = null; // Will be replaced by Spring-managed instance in real environment
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    if (userRepository == null) {
      throw new UsernameNotFoundException("UserRepository not injected");
    }
    User u = userRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    return new org.springframework.security.core.userdetails.User(
      u.getUsername(),
      u.getPassword(),
      List.of(new SimpleGrantedAuthority(u.getRole()))
    );
  }
}
