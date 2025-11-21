package com.example.quiz.controller;

import com.example.quiz.model.Quiz;
import com.example.quiz.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class HomeController {
  private final QuizService quizService;

  @GetMapping({"/", "/dashboard"})
  public String dashboard(Model model, Authentication auth) {
    model.addAttribute("quizzes", quizService.listAll());
    model.addAttribute("username", auth != null ? auth.getName() : null);
    return "dashboard";
  }

  @GetMapping("/quiz/{id}")
  public String viewQuiz(@PathVariable Long id, Model model) {
    Quiz quiz = quizService.findById(id).orElseThrow();
    model.addAttribute("quiz", quiz);
    return "quiz";
  }
}
