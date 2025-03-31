package com.noahhendrickson.bot;

import com.noahhendrickson.bot.questions.QuestionRepository;
import com.noahhendrickson.bot.session.TestRecordStore;
import com.noahhendrickson.bot.session.TestSessionManager;
import com.noahhendrickson.bot.ui.ButtonManager;

public class BotContext {

    private final ButtonManager buttonManager;
    private final TestSessionManager sessionManager;
    private final QuestionRepository questionRepository;
    private final TestRecordStore recordStore;

    public BotContext() {
        this.buttonManager = new ButtonManager();
        this.sessionManager = new TestSessionManager();
        this.questionRepository = new QuestionRepository("questions.json");
        this.recordStore = new TestRecordStore();
    }

    public ButtonManager getButtonManager() {
        return buttonManager;
    }

    public TestSessionManager getSessionManager() {
        return sessionManager;
    }

    public QuestionRepository getQuestionRepository() {
        return questionRepository;
    }

    public TestRecordStore getRecordStore() {
        return recordStore;
    }
}
