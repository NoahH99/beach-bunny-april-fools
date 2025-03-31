package com.noahhendrickson.bot.commands;

import com.noahhendrickson.bot.BotContext;
import com.noahhendrickson.bot.ui.EmbedFactory;
import com.noahhendrickson.bot.util.StartTestUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InitCommand extends AbstractCommand {

    private static final Logger logger = LoggerFactory.getLogger(InitCommand.class);

    public InitCommand(BotContext context) {
        super(context);
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        long userId = event.getUser().getIdLong();

        if (guild == null) {
            logger.warn("Init command invoked without a guild (user {}).", userId);
            return;
        }

        if (event.getChannelType() != ChannelType.TEXT) {
            logger.info("User {} tried to run /init in a non-text channel.", userId);
            event.reply("This command must be run in a text channel.").queue();
            return;
        }

        logger.info("Init command run by user {} in guild {} (channel {})", userId, guild.getId(), event.getChannel().getId());

        String buttonId = getContext().getButtonManager().register(
                hook -> StartTestUtil.handleStartButtonInteraction(getContext(), hook)
        );

        logger.debug("Registered start button with ID: {}", buttonId);

        MessageEmbed embed = EmbedFactory.startExamEmbed(guild.getSelfMember().getColor(), buttonId);
        Button startButton = Button.primary(buttonId, "Start Exam");

        event.deferReply(true).queue(hook -> {
            event.getChannel().sendMessageEmbeds(embed)
                    .setActionRow(startButton)
                    .queue(
                            success -> logger.info("Start panel successfully sent by user {}", userId),
                            error -> logger.error("Failed to send start panel", error)
                    );
            hook.deleteOriginal().queue();
        });
    }
}
