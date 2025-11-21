package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;
  private final BCryptPasswordEncoder passwordEncoder;

  public User registerUser(String username, String rawPassword, String fullname, boolean admin) {
    User user = User.builder()
      .username(username)
      .password(passwordEncoder.encode(rawPassword))
      .fullname(fullname)
      .role(admin ? "ROLE_ADMIN" : "ROLE_USER")
      .build();
    return userRepository.save(user);
  }
}
