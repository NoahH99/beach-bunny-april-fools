package com.noahhendrickson.bot.session;

import com.noahhendrickson.bot.questions.Question;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TestSessionManager {

    private static final Logger logger = LoggerFactory.getLogger(TestSessionManager.class);

    private final Map<Long, TestSession> activeSessions = new ConcurrentHashMap<>();

    public TestSession startSession(Member user, ThreadChannel thread, List<Question> questions) {
        long userId = user.getIdLong();
        TestSession session = new TestSession(user, thread, questions);
        activeSessions.put(userId, session);
        logger.info("Started test session for user {} (thread: {})", userId, thread.getId());
        return session;
    }

    public TestSession getSession(long userId) {
        return activeSessions.get(userId);
    }

    public void endSession(long userId) {
        if (activeSessions.remove(userId) != null) {
            logger.info("Ended test session for user {}", userId);
        } else {
            logger.debug("Attempted to end session for user {}, but no session was active.", userId);
        }
    }

    public boolean isInSession(long userId) {
        boolean inSession = activeSessions.containsKey(userId);
        logger.debug("User {} is{} in an active session", userId, inSession ? "" : " not");
        return inSession;
    }

    public void clearAll() {
        activeSessions.clear();
        logger.info("All sessions cleared from test records");
    }
}
