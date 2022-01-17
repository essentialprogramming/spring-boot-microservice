package com.api.controller;

import com.api.config.Anonymous;
import com.util.enums.Language;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


@Tag(description = "Test API", name = "Test purpose only")
@RestController
public class WelcomeController {

    private static final Logger LOG = LoggerFactory.getLogger(WelcomeController.class);

/*
    @Autowired
    public WelcomeController(Language language) {
        this.language = language;
        LOG.info("Starting..");
    }


    //........................................................................................................................
    //Test purpose only
    @SneakyThrows
    @GetMapping(value = "test/{name}",  produces = {"application/json"})
    @Operation(summary = "Test purpose only")
    public ResponseEntity<Language> test(@RequestHeader("Accept-Language") String lang, @PathVariable("name") String name) {

        return new ResponseEntity<>(test(), HttpStatus.OK);
    }

    private Locale test() throws IOException {
        return locale;
    }
*/

    @GetMapping(value = "/questions")
    @ResponseBody
    @Anonymous
    public List<QuestionJSON> getQuestionsByQuiz() {

        QuestionJSON question = QuestionJSON.builder()
                .question("Why are you here ?")
                .answers(Arrays.asList("Don't know", "Looking around", "I want to build an awesome app"))
                .correctAnswer("c")
                .build();

        QuestionJSON question2 = QuestionJSON.builder()
                .question("What's next ?")
                .answers(Arrays.asList("Leave this page", "Get a coffee", "Build an awesome app"))
                .correctAnswer("c")
                .build();


        return Arrays.asList(question, question2);

    }

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionJSON {

        private int id;
        private String question;
        private String correctAnswer;
        private String quiz;
        private List<String> answers;
    }
}
