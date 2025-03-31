package com.noahhendrickson.bot.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Objects;

public class DataStore {

    private static final Logger logger = LoggerFactory.getLogger(DataStore.class);
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Load a JSON file from the classpath (e.g. resources folder).
     */
    public <T> T loadFromResources(String resourcePath, Type type, T defaultValue) {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(resourcePath);
             Reader reader = new InputStreamReader(Objects.requireNonNull(stream))) {

            logger.info("Loading data from resource: {}", resourcePath);
            return gson.fromJson(reader, type);

        } catch (Exception e) {
            logger.error("Failed to load data from resource: {}", resourcePath, e);
            return defaultValue;
        }
    }

    /**
     * Load a JSON file from a file path (for persistent data).
     */
    public <T> T loadFromFile(String filePath, Type type, T defaultValue) {
        try (Reader reader = new FileReader(filePath)) {
            logger.info("Loading data from file: {}", filePath);
            return gson.fromJson(reader, type);
        } catch (IOException e) {
            logger.warn("File not found or failed to load: {}. Using default value.", filePath);
            return defaultValue;
        }
    }

    /**
     * Save a Java object to a file.
     */
    public void saveToFile(String filePath, Object data) {
        try (Writer writer = new FileWriter(filePath)) {
            gson.toJson(data, writer);
            logger.info("Saved data to file: {}", filePath);
        } catch (IOException e) {
            logger.error("Failed to save data to file: {}", filePath, e);
        }
    }
}
