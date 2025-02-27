package org.example.repository;

import org.springframework.stereotype.Repository;
import org.example.dto.Question;

import java.util.*;

@Repository
public class DiscussionRepository {

    private final Map<String, Deque<Question>> userQuestions = new HashMap<>();

    public void addQuestion(String userName, String question) {
        Question dto = new Question();
        dto.setQuestion(question);
        userQuestions.computeIfAbsent(userName, k -> new LinkedList<>()).add(dto);
    }

    public void addAnswer(String userName, String answer) {
        if (userQuestions.containsKey(userName)) {
            Question question = userQuestions.get(userName).peekLast();
            if (question != null) {
                question.setAnswer(answer);
            } else {
                throw new IllegalStateException("There is no book query awaiting an answer for user " + userName);
            }
        } else {
            throw new IllegalStateException("There is no active book recommendation session for user " + userName);
        }
    }

    public Deque<Question> finishInterview(String userName) {
        return userQuestions.remove(userName);
    }

    public int getUserQuestions(String userName) {
        return userQuestions.get(userName).size();
    }
}