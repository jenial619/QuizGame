package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.service.QuizService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
  private final QuizService quizService;

  @GetMapping("")
  public String adminHome() {
    return "redirect:/admin/quizzes";
  }

  @GetMapping("/quizzes")
  public String quizzes(Model model) {
    model.addAttribute("quizzes", quizService.listAll());
    return "admin/quizzes";
  }

  @GetMapping("/create")
  public String createForm() {
    return "admin/create";
  }

  @PostMapping("/generate")
  public String generate(@RequestParam String title, @RequestParam String details) {
    quizService.generateQuizFromTopic(title, details);
    return "redirect:/admin/quizzes";
  }
}
