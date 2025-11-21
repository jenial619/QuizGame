package com.example.quiz.controller;

import com.example.quiz.model.*;
import com.example.quiz.repository.QuizRepository;
import com.example.quiz.repository.UserRepository;
import com.example.quiz.repository.AttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AttemptController {
  private final QuizRepository quizRepository;
  private final UserRepository userRepository;
  private final AttemptRepository attemptRepository;

  @PostMapping("/quiz/{id}/submit")
  public String submit(@PathVariable Long id, @RequestParam Map<String,String> form, Authentication auth) {

      Quiz quiz = quizRepository.findById(id).orElseThrow();

      final int[] correct = {0};  // FIX
      int total = quiz.getQuestions().size();

      for (Question q : quiz.getQuestions()) {
          String choice = form.get("q_" + q.getId());
          if (choice != null) {
              long optId = Long.parseLong(choice);

              q.getOptions().stream()
                  .filter(o -> o.getId().equals(optId))
                  .findFirst()
                  .ifPresent(o -> {
                      if (o.isCorrect()) correct[0]++;  // FIX
                  });
          }
      }

      double score = (total == 0) ? 0 : (correct[0] / (double) total) * 100;

      com.example.quiz.model.User user =
              userRepository.findByUsername(auth.getName()).orElseThrow();

      Attempt attempt = Attempt.builder()
              .user(user)
              .quiz(quiz)
              .score(score)
              .attemptedAt(LocalDateTime.now())
              .build();

      attemptRepository.save(attempt);

      return "redirect:/dashboard";
  }

}
