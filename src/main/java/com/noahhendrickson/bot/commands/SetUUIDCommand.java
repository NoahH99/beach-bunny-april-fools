package com.noahhendrickson.bot.commands;

import com.noahhendrickson.bot.BotContext;
import com.noahhendrickson.bot.util.StartTestUtil;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetUUIDCommand extends AbstractCommand {

    private static final Logger logger = LoggerFactory.getLogger(SetUUIDCommand.class);

    public SetUUIDCommand(BotContext context) {
        super(context);
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        String buttonId = event.getOption("uuid").getAsString();
        long userId = event.getUser().getIdLong();

        logger.info("SetUUIDCommand invoked by user {} with UUID: {}", userId, buttonId);

        getContext().getButtonManager().register(buttonId, hook -> {
            logger.debug("Executing start handler from manually set UUID: {}", buttonId);
            StartTestUtil.handleStartButtonInteraction(getContext(), hook);
        });

        event.reply("UUID ``" + buttonId + "`` registered as button handler successfully.").setEphemeral(true).queue();
        logger.info("UUID {} registered as button handler successfully.", buttonId);
    }
}
