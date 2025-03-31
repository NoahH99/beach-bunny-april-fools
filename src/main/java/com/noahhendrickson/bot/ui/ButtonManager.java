package com.noahhendrickson.bot.ui;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class ButtonManager {

    private static final Logger logger = LoggerFactory.getLogger(ButtonManager.class);
    private final Map<String, Consumer<ButtonInteractionEvent>> handlers = new HashMap<>();

    public String register(Consumer<ButtonInteractionEvent> handler) {
        String id = UUID.randomUUID().toString();
        handlers.put(id, handler);
        logger.debug("Registered new button handler with ID: {}", id);
        return id;
    }

    public void register(String id, Consumer<ButtonInteractionEvent> handler) {
        handlers.put(id, handler);
        logger.debug("Registered button handler with custom ID: {}", id);
    }

    public void handle(ButtonInteractionEvent event) {
        String id = event.getComponentId();
        Consumer<ButtonInteractionEvent> handler = handlers.get(id);

        if (handler != null) {
            logger.info("Handling button interaction with ID: {} from user: {}", id, event.getUser().getIdLong());
            handler.accept(event);
        } else {
            logger.warn("No handler found for button ID: {}", id);
            event.reply("This button no longer works.").setEphemeral(true).queue();
        }
    }
}
