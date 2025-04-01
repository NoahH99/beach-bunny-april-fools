package com.noahhendrickson.bot.openai;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.noahhendrickson.bot.BotConfig;
import com.noahhendrickson.bot.questions.Question;
import com.noahhendrickson.bot.session.TestSession;
import com.noahhendrickson.bot.util.CertificateGenerator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.FileUpload;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class OpenAIGrader {

    private static final Logger logger = LoggerFactory.getLogger(OpenAIGrader.class);
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new GsonBuilder().create();

    private final TextChannel announceChannel;

    public OpenAIGrader(TextChannel announceChannel) {
        this.announceChannel = announceChannel;
    }

    public void gradeAndAnnounce(TestSession session) {
        Member user = session.getUser();
        String username = user.getEffectiveName();
        logger.info("Grading session for user {}", username);

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Test Results: " + username)
                .setThumbnail(user.getAvatarUrl())
                .setColor(user.getColor());

        int totalScore = 0;
        for (Map.Entry<Question, String> entry : session.getAnswers().entrySet()) {
            String question = entry.getKey().getQuestionText();
            String correct = entry.getKey().getCorrectAnswer();
            String userAnswer = entry.getValue();

            int score = 0;
            if (BotConfig.USE_OPENAI_GRADING) {
                int adjustedGradeAnswer = gradeAnswer(question, correct, userAnswer) * 2;

                score = adjustedGradeAnswer > 80 ? Math.min(adjustedGradeAnswer + 10, 100) : Math.max(adjustedGradeAnswer, 0);

                logger.debug("Scored {} for question: '{}'", score, question);
            }

            totalScore += score;
            embed.addField("Question: " + question, "Answer: " + userAnswer, false);
        }

        if (BotConfig.USE_OPENAI_GRADING)
            embed.setFooter("Total Score: " + totalScore + "/100");
        else
            embed.setFooter("No grade given because grading is disabled.");

        announceChannel.sendMessage(user.getAsMention())
                .addEmbeds(embed.build())
                .queue();

        if (totalScore >= 93) {
            logger.info("User {} scored {}. Generating certificate.", username, totalScore);
            handleCert(user);
        } else {
            logger.info("User {} scored {}. No certificate awarded.", username, totalScore);
        }
    }

    private int gradeAnswer(String question, String correctAnswer, String userAnswer) {
        try {
            JsonObject requestBody = getJsonObject(question, correctAnswer, userAnswer);
            Request request = new Request.Builder()
                    .url(API_URL)
                    .header("Authorization", "Bearer " + BotConfig.OPENAI_API_KEY)
                    .post(RequestBody.create(gson.toJson(requestBody), MediaType.get("application/json")))
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    logger.error("OpenAI API returned error: {}", response);
                    throw new IOException("OpenAI failed: " + response);
                }

                String body = response.body().string();
                JsonObject json = gson.fromJson(body, JsonObject.class);
                String content = json
                        .getAsJsonArray("choices")
                        .get(0).getAsJsonObject()
                        .getAsJsonObject("message")
                        .get("content").getAsString();

                String digitsOnly = content.replaceAll("[^0-9]", "");
                int parsedScore = Integer.parseInt(digitsOnly);
                logger.debug("Parsed OpenAI score: {}", parsedScore);
                return parsedScore;
            }

        } catch (Exception e) {
            logger.error("Failed to grade answer using OpenAI", e);
            return 0;
        }
    }

    @NotNull
    private static JsonObject getJsonObject(String question, String correctAnswer, String userAnswer) {
        JsonArray messages = new JsonArray();

        JsonObject systemMsg = new JsonObject();
        systemMsg.addProperty("role", "system");
        systemMsg.addProperty("content", "You are a lenient exam grader. That means you should give the user the benefit of the doubt. Compare the user's answer to the correct answer. Respond with a single number from 1 to 10. Do not include anything else.");
        messages.add(systemMsg);

        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", String.format(
                "Question: %s\nCorrect Answer: %s\nUser Answer: %s\nScore (1-10):",
                question, correctAnswer, userAnswer));
        messages.add(userMsg);

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", "gpt-4o-mini");
        requestBody.addProperty("temperature", 0);
        requestBody.add("messages", messages);
        return requestBody;
    }

    private void handleCert(Member user) {
        try {
            String username = user.getUser().getGlobalName();
            File certificate = CertificateGenerator.generateCertificate(username);
            logger.debug("Certificate generated for user {}", username);

            user.getUser().openPrivateChannel().queue(
                    privateChannel -> {
                        logger.info("Sending certificate to {} via DM", username);
                        privateChannel.sendMessage("🎓 Here is your certificate, " + username + "!")
                                .addFiles(FileUpload.fromData(certificate))
                                .queue();
                    },
                    failure -> {
                        logger.warn("Could not send certificate via DM to {}. Falling back to announcement channel.", username);
                        announceChannel.sendMessage("🎓 Here is your certificate, " + username + "!")
                                .addFiles(FileUpload.fromData(certificate))
                                .queue();
                    }
            );

        } catch (Exception e) {
            logger.error("Failed to generate or send certificate", e);
        }
    }
}
