package com.noahhendrickson.bot.commands;

import com.noahhendrickson.bot.BotContext;
import com.noahhendrickson.bot.session.TestRecordStore;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClearCompletedUsersCommand extends AbstractCommand {

    private static final Logger logger = LoggerFactory.getLogger(ClearCompletedUsersCommand.class);

    public ClearCompletedUsersCommand(BotContext context) {
        super(context);
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        String subcommand = event.getSubcommandName();
        if (subcommand == null) {
            logger.warn("ClearCompletedUsersCommand invoked without a subcommand by user {}", event.getUser().getIdLong());
            return;
        }

        logger.info("ClearCompletedUsersCommand invoked with subcommand '{}' by user {}", subcommand, event.getUser().getIdLong());

        switch (subcommand) {
            case "all" -> clearAllUsers(event);
            case "user" -> clearUser(event, event.getOption("user").getAsUser());
            default -> logger.warn("Unknown subcommand '{}' in ClearCompletedUsersCommand", subcommand);
        }
    }

    private void clearAllUsers(SlashCommandInteractionEvent event) {
        getContext().getRecordStore().clearAll();
        logger.info("All users cleared from test records by {}", event.getUser().getIdLong());
        event.reply("All users will now be able to take the test again.").setEphemeral(true).queue();
    }

    private void clearUser(SlashCommandInteractionEvent event, User user) {
        TestRecordStore recordStore = getContext().getRecordStore();
        long targetId = user.getIdLong();

        if (!recordStore.hasTakenTest(targetId)) {
            logger.info("Attempt to clear user {} who hasn't taken the test (invoked by {})", targetId, event.getUser().getIdLong());
            event.reply(user.getAsMention() + " has not taken the test yet.").setEphemeral(true).queue();
            return;
        }

        recordStore.removeUser(targetId);
        logger.info("User {} cleared from test records by {}", targetId, event.getUser().getIdLong());

        event.reply(user.getAsMention() + " will now be able to take the test again.").setEphemeral(true).queue();
    }
}
