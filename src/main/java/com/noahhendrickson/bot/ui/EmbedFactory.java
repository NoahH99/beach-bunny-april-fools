package com.noahhendrickson.bot.ui;

import com.noahhendrickson.bot.questions.Question;
import com.noahhendrickson.bot.session.TestSession;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.Map;

public class EmbedFactory {

    public static MessageEmbed startExamEmbed(Color color, String buttonId) {
        return new EmbedBuilder()
                .setTitle("Start your exam!")
                .setDescription("""
                        Click the button below to begin your test.
                        You will receive 5 short-answer questions in a private thread.
                        Your answers will be scored automatically.""")
                .setColor(color)
                .setFooter(buttonId)
                .build();
    }

    public static MessageEmbed questionEmbed(Question question, int questionNumber, Color color) {
        return new EmbedBuilder()
                .setTitle("Question " + questionNumber)
                .setDescription(question.getQuestionText())
                .setColor(color)
                .setFooter("Please reply in this thread.")
                .build();
    }

    public static MessageEmbed reviewEmbed(TestSession session) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Review Your Answers")
                .setFooter("Here is a summary of your responses. Click submit when you're ready.")
                .setColor(session.getUser().getColor());

        int i = 1;
        for (Map.Entry<Question, String> entry : session.getAnswers().entrySet()) {
            embed.addField("Q" + i + ": " + entry.getKey().getQuestionText(), "Your Answer: `" + entry.getValue() + "`", false);
            i++;
        }

        return embed.build();
    }
}
