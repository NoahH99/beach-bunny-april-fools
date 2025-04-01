package com.noahhendrickson.bot.commands;

import com.noahhendrickson.bot.BotContext;
import com.noahhendrickson.bot.session.TestRecordStore;
import com.noahhendrickson.bot.session.TestSessionManager;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClearUserDataCommand extends AbstractCommand {

    private static final Logger logger = LoggerFactory.getLogger(ClearUserDataCommand.class);

    public ClearUserDataCommand(BotContext context) {
        super(context);
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        String subcommand = event.getSubcommandName();
        if (subcommand == null) {
            logger.warn("ClearUserDataCommand invoked without a subcommand by user {}", event.getUser().getIdLong());
            return;
        }

        logger.info("ClearUserDataCommand invoked with subcommand '{}' by user {}", subcommand, event.getUser().getIdLong());

        switch (subcommand) {
            case "all" -> clearAll(event);
            case "user" -> clearUser(event, event.getOption("user").getAsUser());
            default -> logger.warn("Unknown subcommand '{}' in ClearUserDataCommand", subcommand);
        }
    }

    private void clearAll(SlashCommandInteractionEvent event) {
        getContext().getRecordStore().clearAll();
        logger.info("All users cleared from test records by {}", event.getUser().getIdLong());
        getContext().getSessionManager().clearAll();
        logger.info("All users cleared from test sessions by {}", event.getUser().getIdLong());
        event.reply("All users will now be able to take the test again.").setEphemeral(true).queue();
    }

    private void clearUser(SlashCommandInteractionEvent event, User user) {
        TestRecordStore recordStore = getContext().getRecordStore();
        TestSessionManager sessionManager = getContext().getSessionManager();
        long targetId = user.getIdLong();

        if (recordStore.hasTakenTest(targetId)) {
            recordStore.removeUser(targetId);
            logger.info("User {} cleared from test records by {}", targetId, event.getUser().getIdLong());
            event.reply("User " + user.getAsMention() + " cleared from test records by " + event.getUser().getIdLong()).setEphemeral(true).queue();
            return;
        }

        if (sessionManager.isInSession(targetId)) {
            sessionManager.endSession(targetId);
            logger.info("User {} cleared from test session by {}", targetId, event.getUser().getIdLong());
            event.reply("User " + user.getAsMention() + " cleared from test session by " + event.getUser().getIdLong()).setEphemeral(true).queue();
            return;
        }

        logger.info("Attempt to clear user {} who isn't in a test session right now (invoked by {})", targetId, event.getUser().getIdLong());
        logger.info("Attempt to clear user {} who hasn't taken a test (invoked by {})", targetId, event.getUser().getIdLong());

        event.reply( user.getAsMention() + ", has not taken a test nor are they in a session right now.").setEphemeral(true).queue();
    }
}
