package com.noahhendrickson.bot;

public class BotConfig {

    public static final String BOT_TOKEN = parseString("BOT_TOKEN");
    public static final String OPENAI_API_KEY = parseString("OPENAI_API_KEY");

    public static final long BEACH_BUNNY_SERVER_ID = parseLong("SERVER_ID");
    public static final long BEACH_BUNNY_ANNOUNCEMENT_CHANNEL_ID = parseLong("ANNOUNCEMENT_CHANNEL_ID");

    public static final boolean USE_OPENAI_GRADING = parseBoolean("USE_OPENAI_GRADING");

    private static String parseString(String envVar) {
        String env = System.getenv(envVar);

        if (env == null || env.isEmpty())
            throw new IllegalStateException("Missing required environment variable: " + envVar);

        return env;
    }

    private static long parseLong(String envVar) {
        String value = parseString(envVar);
        return Long.parseLong(value);
    }

    private static boolean parseBoolean(String envVar) {
        String value = parseString(envVar);
        return Boolean.parseBoolean(value);
    }
}