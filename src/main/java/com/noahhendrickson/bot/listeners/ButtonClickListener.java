package com.noahhendrickson.bot.listeners;

import com.noahhendrickson.bot.BotContext;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ButtonClickListener extends ListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ButtonClickListener.class);
    private final BotContext context;

    public ButtonClickListener(BotContext context) {
        this.context = context;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String buttonId = event.getComponentId();
        long userId = event.getUser().getIdLong();

        logger.info("Button clicked: '{}' by user: {}", buttonId, userId);

        try {
            context.getButtonManager().handle(event);
        } catch (Exception e) {
            logger.error("Error while handling button interaction for ID: {}", buttonId, e);
            event.reply("Something went wrong while handling that button.").setEphemeral(true).queue();
        }
    }
}
