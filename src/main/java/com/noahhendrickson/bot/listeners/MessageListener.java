package com.noahhendrickson.bot.listeners;

import com.noahhendrickson.bot.BotConfig;
import com.noahhendrickson.bot.BotContext;
import com.noahhendrickson.bot.openai.OpenAIGrader;
import com.noahhendrickson.bot.questions.Question;
import com.noahhendrickson.bot.session.TestSession;
import com.noahhendrickson.bot.ui.EmbedFactory;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class MessageListener extends ListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(MessageListener.class);
    private final BotContext context;

    public MessageListener(BotContext context) {
        this.context = context;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message message = event.getMessage();

        if (!event.isFromThread() || message.getAuthor().isBot()) return;

        long userId = message.getAuthor().getIdLong();
        TestSession session = context.getSessionManager().getSession(userId);

        if (session == null || !event.getChannel().equals(session.getThread())) return;

        logger.info("Recording answer from user {} in thread {}", userId, session.getThread().getId());
        session.recordAnswer(message.getContentDisplay());
        message.delete().queue();

        session.getThread().sendMessage("<:success:697249279458279485> Answer recorded.")
                .delay(3, TimeUnit.SECONDS)
                .flatMap(Message::delete)
                .queue();

        if (session.isComplete()) {
            logger.info("Session for user {} is complete. Sending review embed.", userId);
            sendReviewEmbed(session);
        } else {
            sendNextQuestion(session);
        }
    }

    private void sendNextQuestion(TestSession session) {
        logger.debug("Sending next question to user {}", session.getUser().getIdLong());
        MessageEmbed embed = EmbedFactory.questionEmbed(
                session.getCurrentQuestion(),
                session.getCurrentQuestionIndex(),
                session.getUser().getColor()
        );

        session.sendEmbed(embed);
    }

    private void sendReviewEmbed(TestSession session) {
        long userId = session.getUser().getIdLong();
        logger.debug("Sending review embed to user {}", userId);
        MessageEmbed embed = EmbedFactory.reviewEmbed(session);

        String submitId = context.getButtonManager().register(e -> handleSubmit(e, session));
        String restartId = context.getButtonManager().register(e -> handleRestart(e, session));

        ActionRow actionRow = ActionRow.of(
                Button.primary(submitId, "Submit"),
                Button.danger(restartId, "Restart")
        );

        Message questionMessage = session.getQuestionMessage();
        if (questionMessage != null) {
            logger.debug("Editing existing question message with review embed for user {}", userId);
            questionMessage.editMessageEmbeds(embed)
                    .setComponents(actionRow)
                    .queue();
        } else {
            logger.debug("Sending new review embed message for user {}", userId);
            session.getThread().sendMessageEmbeds(embed)
                    .setComponents(actionRow)
                    .queue(session::setQuestionMessage);
        }
    }

    private void handleSubmit(ButtonInteractionEvent e, TestSession session) {
        long userId = session.getUser().getIdLong();
        logger.info("User {} submitted their test. Grading and announcing results.", userId);
        e.deferEdit().queue();

        context.getRecordStore().markAsCompleted(userId);
        context.getSessionManager().endSession(userId);

        Guild guild = e.getGuild();
        if (guild == null) {
            logger.warn("Could not find guild for user {} during submit.", userId);
            return;
        }

        TextChannel announcementChannel = guild.getTextChannelById(BotConfig.BEACH_BUNNY_ANNOUNCEMENT_CHANNEL_ID);
        if (announcementChannel == null) {
            logger.warn("Announcement channel not found for guild {}.", guild.getId());
            return;
        }

        session.getThread().sendMessage("Grading your response... this thread will delete in 1 minute.").queue();
        new OpenAIGrader(announcementChannel).gradeAndAnnounce(session);

        session.getThread().delete().queueAfter(75, TimeUnit.SECONDS);
    }

    private void handleRestart(ButtonInteractionEvent e, TestSession session) {
        long userId = session.getUser().getIdLong();
        logger.info("User {} restarted their test session.", userId);
        e.deferEdit().queue();

        List<Question> originalQuestions = session.getQuestions();
        session.getQuestionMessage().delete().queue();
        session.setQuestionMessage(null);

        context.getSessionManager().endSession(userId);
        context.getSessionManager().startSession(session.getUser(), session.getThread(), originalQuestions);

        sendNextQuestion(context.getSessionManager().getSession(userId));
    }
}
