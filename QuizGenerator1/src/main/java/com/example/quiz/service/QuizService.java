package com.example.quiz.service;

import com.example.quiz.model.*;
import com.example.quiz.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    private final WebClient webClient = WebClient.builder().build();

    public Quiz save(Quiz quiz) {
        if (quiz.getQuestions() != null) {
            quiz.getQuestions().forEach(q -> q.setQuiz(quiz));
            quiz.getQuestions().forEach(q ->
                    q.getOptions().forEach(opt -> opt.setQuestion(q)));
        }
        return quizRepository.save(quiz);
    }

    public List<Quiz> listAll() {
        return quizRepository.findAll();
    }

    public Optional<Quiz> findById(Long id) {
        return quizRepository.findById(id);
    }

    public Quiz generateQuizFromTopic(String topicTitle, String topicDetails) {

        String prompt = String.format(
                "Create a short multiple-choice quiz (5 questions) about: %s\n" +
                "Details: %s\n" +
                "Provide JSON with keys: title, description, questions[] where each question has text " +
                "and options[] where each option has text and correct (true/false).",
                topicTitle, topicDetails
        );

        Map<String, Object> requestBody = Map.of(
                "prompt", prompt,
                "max_tokens", 800
        );

        Mono<String> responseMono = webClient.post()
                .uri(geminiApiUrl)
                .header("Authorization", "Bearer " + geminiApiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class);

        String rawResponse = responseMono.block();

        if (rawResponse == null || rawResponse.isBlank()) {
            return fallbackQuiz(topicTitle, topicDetails);
        }

        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            
            // FIX: Cast parsed JSON to a proper typed map
            Map<String, Object> parsed =
                    mapper.readValue(rawResponse, mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));

            // Safe usage of getOrDefault
            String title = (String) parsed.getOrDefault("title", "Quiz: " + topicTitle);
            String desc = (String) parsed.getOrDefault("description", topicDetails);

            List<Map<String, Object>> qlist =
                    (List<Map<String, Object>>) parsed.getOrDefault("questions", Collections.emptyList());

            List<Question> questions = qlist.stream().map(qm -> {

                Question q = new Question();
                q.setText((String) qm.getOrDefault("text", ""));

                List<Map<String, Object>> opts =
                        (List<Map<String, Object>>) qm.getOrDefault("options", Collections.emptyList());

                List<Option> options = opts.stream().map(om -> {
                    Option o = new Option();
                    o.setText((String) om.getOrDefault("text", ""));
                    o.setCorrect(Boolean.TRUE.equals(om.get("correct")));
                    return o;
                }).collect(Collectors.toList());

                q.setOptions(options);
                return q;

            }).collect(Collectors.toList());

            Quiz quiz = new Quiz();
            quiz.setTitle(title);
            quiz.setDescription(desc);
            quiz.setQuestions(questions);

            return save(quiz);

        } catch (Exception ex) {
            ex.printStackTrace();
            return fallbackQuiz(topicTitle, topicDetails);
        }
    }

    private Quiz fallbackQuiz(String topicTitle, String topicDetails) {
        Quiz q = new Quiz();
        q.setTitle("Sample Quiz: " + topicTitle);
        q.setDescription(topicDetails);

        List<Question> questions = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Question qu = new Question();
            qu.setText("Sample question " + i + " about " + topicTitle + "?");

            List<Option> opts = new ArrayList<>();
            opts.add(Option.builder().text("Option A").correct(i % 4 == 0).build());
            opts.add(Option.builder().text("Option B").correct(i % 4 == 1).build());
            opts.add(Option.builder().text("Option C").correct(i % 4 == 2).build());
            opts.add(Option.builder().text("Option D").correct(i % 4 == 3).build());

            qu.setOptions(opts);
            questions.add(qu);
        }

        q.setQuestions(questions);
        return save(q);
    }
}
