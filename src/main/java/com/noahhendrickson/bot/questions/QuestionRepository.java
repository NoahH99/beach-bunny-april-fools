package com.noahhendrickson.bot.questions;

import com.google.gson.reflect.TypeToken;
import com.noahhendrickson.bot.util.DataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuestionRepository {

    private static final Logger logger = LoggerFactory.getLogger(QuestionRepository.class);
    private final List<Question> allQuestions;

    public QuestionRepository(String resourcePath) {
        Type type = new TypeToken<List<Question>>() {
        }.getType();
        this.allQuestions = new DataStore().loadFromResources(resourcePath, type, new ArrayList<>());

        if (allQuestions.isEmpty()) {
            logger.warn("No questions loaded from resource: {}", resourcePath);
        } else {
            logger.info("Loaded {} questions from resource: {}", allQuestions.size(), resourcePath);
        }
    }

    public List<Question> getRandomQuestions(int amount) {
        logger.debug("Selecting {} random questions", amount);
        List<Question> copy = new ArrayList<>(allQuestions);
        Collections.shuffle(copy);
        return copy.subList(0, Math.min(amount, copy.size()));
    }

    public boolean isEmpty() {
        return allQuestions.isEmpty();
    }

    public List<Question> getAllQuestions() {
        return Collections.unmodifiableList(allQuestions);
    }
}
