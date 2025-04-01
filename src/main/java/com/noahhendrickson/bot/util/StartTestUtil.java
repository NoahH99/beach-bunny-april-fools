package com.noahhendrickson.bot.util;

import com.noahhendrickson.bot.BotContext;
import com.noahhendrickson.bot.questions.Question;
import com.noahhendrickson.bot.session.TestSession;
import com.noahhendrickson.bot.ui.EmbedFactory;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.managers.channel.concrete.ThreadChannelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class StartTestUtil {

    private static final Logger logger = LoggerFactory.getLogger(StartTestUtil.class);

    public static void handleStartButtonInteraction(BotContext context, ButtonInteractionEvent event) {
        long userId = event.getUser().getIdLong();
        String username = event.getUser().getName();

        if (context.getRecordStore().hasTakenTest(userId)) {
            logger.info("User {} ({}) attempted to retake the test.", username, userId);
            event.reply("You've already taken the test. You can only take it once.").setEphemeral(true).queue();
            return;
        }

        if (context.getSessionManager().isInSession(userId)) {
            logger.info("User {} ({}) tried to start a test but is already in session.", username, userId);
            event.reply("You've already started the test.").setEphemeral(true).queue();
            return;
        }

        logger.info("User {} ({}) is starting a new test session.", username, userId);
        event.deferReply(true).queue(hook -> hook.deleteOriginal().queue());

        event.getChannel().asTextChannel().createThreadChannel(username + "-" + System.currentTimeMillis(), true)
                .setInvitable(false)
                .queue(thread -> {
                    ThreadChannelManager manager = thread.getManager();

                    manager.setSlowmode(3).queue();

                    Member member = event.getMember();
                    if (member == null) {
                        logger.warn("Member was null when starting thread for user {}", userId);
                        return;
                    }

                    logger.debug("Created thread {} for user {}", thread.getId(), userId);
                    thread.addThreadMember(member).queue();

                    List<Question> questions = context.getQuestionRepository().getRandomQuestions(5);
                    TestSession session = context.getSessionManager().startSession(member, thread, questions);

                    thread.sendMessage("Starting your test...").delay(3, TimeUnit.SECONDS).queue(msg -> {
                        msg.delete().queueAfter(1, TimeUnit.SECONDS);
                        sendFirstQuestion(session);
                    });
                }, error -> logger.error("Failed to create thread for user {}", userId, error));
    }

    public static void sendFirstQuestion(TestSession session) {
        logger.debug("Sending first question to user {}", session.getUser().getIdLong());

        Question question = session.getCurrentQuestion();
        MessageEmbed embed = EmbedFactory.questionEmbed(
                question,
                session.getCurrentQuestionIndex(),
                session.getUser().getColor()
        );

        session.sendEmbed(embed);
    }
}
