package com.eugene.book_service.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "QUESTION-SERVICE")
public interface QuizFeign {
    //@PostMapping("question/getQuestions")
    //public ResponseEntity<List<QuestionWrapper>> getQuestionsById(@RequestBody List<Integer> questionsIds);

    @GetMapping("question/generate")
    public ResponseEntity<List<Integer>> getQuestionsForQuiz(@RequestParam String category, @RequestParam int nbrQuestions);

    //@PostMapping("question/calculateScore")
    //public ResponseEntity<Integer> calculateScore(@RequestBody List<Response> responses);
}
