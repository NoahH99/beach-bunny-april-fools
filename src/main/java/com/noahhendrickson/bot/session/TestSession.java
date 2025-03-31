package com.noahhendrickson.bot.session;

import com.noahhendrickson.bot.questions.Question;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TestSession {

    private static final Logger logger = LoggerFactory.getLogger(TestSession.class);

    private final Member user;
    private final ThreadChannel thread;
    private final List<Question> questions;
    private Message questionMessage;
    private final Map<Question, String> answers = new LinkedHashMap<>();
    private int currentIndex = 0;

    public TestSession(Member user, ThreadChannel thread, List<Question> questions) {
        this.user = user;
        this.thread = thread;
        this.questions = questions.subList(0, Math.min(5, questions.size()));

        logger.info("Started new test session for user {} with {} questions in thread {}",
                user.getIdLong(), this.questions.size(), thread.getId());
    }

    public Question getCurrentQuestion() {
        return questions.get(currentIndex);
    }

    public void recordAnswer(String response) {
        Question currentQuestion = getCurrentQuestion();
        answers.put(currentQuestion, response);
        logger.info("Recorded answer for user {} (Q{}): {}", user.getIdLong(), currentIndex + 1, response);
        currentIndex++;
    }

    public boolean isComplete() {
        boolean complete = currentIndex >= questions.size();
        if (complete) {
            logger.info("Test session for user {} is now complete.", user.getIdLong());
        }
        return complete;
    }

    public Member getUser() {
        return user;
    }

    public ThreadChannel getThread() {
        return thread;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public Message getQuestionMessage() {
        return questionMessage;
    }

    public void setQuestionMessage(Message questionMessage) {
        this.questionMessage = questionMessage;
        logger.debug("Set question message for user {} in thread {}", user.getIdLong(), thread.getId());
    }

    public Map<Question, String> getAnswers() {
        return answers;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public int getCurrentQuestionIndex() {
        return currentIndex + 1;
    }

    public void sendEmbed(MessageEmbed embed) {
        if (questionMessage != null) {
            logger.debug("Editing question embed for user {} (Q{})", user.getIdLong(), getCurrentQuestionIndex());
            questionMessage.editMessageEmbeds(embed).queue();
        } else {
            logger.debug("Sending new question embed for user {} (Q{})", user.getIdLong(), getCurrentQuestionIndex());
            thread.sendMessageEmbeds(embed).queue(this::setQuestionMessage);
        }
    }
}
