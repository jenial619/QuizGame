package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AuthController {
  private final UserService userService;

  @GetMapping("/login")
  public String login() { return "login"; }

  @GetMapping("/register")
  public String showRegistration(Model model) {
    model.addAttribute("user", new User());
    return "register";
  }

  @PostMapping("/register")
  public String register(@ModelAttribute User user, Model model) {
    userService.registerUser(user.getUsername(), user.getPassword(), user.getFullname(), false);
    return "redirect:/login?registered";
  }
}
