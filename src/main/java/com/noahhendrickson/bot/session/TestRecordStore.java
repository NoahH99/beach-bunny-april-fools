package com.noahhendrickson.bot.session;

import com.google.gson.reflect.TypeToken;
import com.noahhendrickson.bot.util.DataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class TestRecordStore {

    private static final Logger logger = LoggerFactory.getLogger(TestRecordStore.class);
    private static final String SAVE_PATH = "data/completed-tests.json";

    private final DataStore dataStore = new DataStore();
    private final Set<Long> completedUserIds;

    public TestRecordStore() {
        Type type = new TypeToken<Set<Long>>() {}.getType();

        Set<Long> loaded = dataStore.loadFromFile(SAVE_PATH, type, Collections.emptySet());
        completedUserIds = Objects.requireNonNullElseGet(loaded, HashSet::new);

        logger.info("Loaded {} completed test records.", completedUserIds.size());
    }

    public boolean hasTakenTest(long userId) {
        return completedUserIds.contains(userId);
    }

    public void markAsCompleted(long userId) {
        if (completedUserIds.add(userId)) {
            logger.info("User {} marked as having completed the test.", userId);
            save();
        } else {
            logger.debug("User {} already marked as completed.", userId);
        }
    }

    public boolean removeUser(long userId) {
        boolean removed = completedUserIds.remove(userId);
        if (removed) {
            logger.info("User {} removed from completed test records.", userId);
            save();
        } else {
            logger.debug("Attempted to remove user {}, but they were not in the records.", userId);
        }
        return removed;
    }

    public void clearAll() {
        logger.warn("Clearing all completed test records!");
        completedUserIds.clear();
        save();
    }

    private void save() {
        logger.debug("Saving completed test records to file.");
        dataStore.saveToFile(SAVE_PATH, completedUserIds);
    }
}
