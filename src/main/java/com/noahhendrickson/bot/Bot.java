package com.noahhendrickson.bot;

import com.noahhendrickson.bot.listeners.ButtonClickListener;
import com.noahhendrickson.bot.listeners.MessageListener;
import com.noahhendrickson.bot.listeners.SlashCommandListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bot {

    private static final Logger logger = LoggerFactory.getLogger(Bot.class);
    private final BotContext context = new BotContext();

    public void start(String token) throws InterruptedException {
        logger.info("Starting Discord bot...");

        JDABuilder builder = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                .enableCache(CacheFlag.MEMBER_OVERRIDES)
                .setActivity(Activity.playing("Prom Queen | v1.0.1"));

        logger.debug("Registering event listeners...");
        builder.addEventListeners(
                new SlashCommandListener(context),
                new ButtonClickListener(context),
                new MessageListener(context)
        );

        try {
            logger.info("Building JDA instance...");
            JDA jda = builder.build();
            jda.awaitReady();
            logger.info("JDA is ready!");

            logger.info("Registering guild slash commands...");
            SlashCommandListener.registerGuildCommands(jda);
            logger.info("Guild commands registered.");
        } catch (Exception e) {
            logger.error("Failed to initialize JDA", e);
            throw e;
        }
    }

    public static void main(String[] args) throws Exception {
        logger.info("Launching bot...");
        new Bot().start(BotConfig.BOT_TOKEN);
    }
}
