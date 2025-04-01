package com.noahhendrickson.bot.listeners;

import com.noahhendrickson.bot.BotConfig;
import com.noahhendrickson.bot.BotContext;
import com.noahhendrickson.bot.commands.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SlashCommandListener extends ListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(SlashCommandListener.class);
    private final BotContext context;

    public SlashCommandListener(BotContext context) {
        this.context = context;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String command = event.getName();
        logger.info("Received slash command: {}", command);

        try {
            switch (command) {
                case "init" -> new InitCommand(context).run(event);
                case "clear" -> new ClearUserDataCommand(context).run(event);
                case "set" -> new SetUUIDCommand(context).run(event);
                case "cert" -> new CertificateCommand(context).run(event);
                default -> logger.warn("Unknown slash command received: {}", command);
            }
        } catch (Exception e) {
            logger.error("Error while handling slash command: {}", command, e);
            event.reply("An error occurred while executing the command.").setEphemeral(true).queue();
        }
    }

    public static void registerGuildCommands(JDA jda) {
        Logger logger = LoggerFactory.getLogger(SlashCommandListener.class);

        Guild guild = jda.getGuildById(BotConfig.BEACH_BUNNY_SERVER_ID);
        if (guild == null) {
            logger.error("Guild not found with ID: {}", BotConfig.BEACH_BUNNY_SERVER_ID);
            return;
        }

        logger.info("Registering slash commands for guild: {}", guild.getName());

        List<CommandData> commands = List.of(
                Commands.slash("cert", "Generates a certificate for the user that runs the command.")
                        .addOption(OptionType.USER, "user", "The user you want to generate the cert for.", false)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS)),
                Commands.slash("init", "Sets up the test panel in this channel")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS)),
                Commands.slash("clear", "Clear specific data")
                        .addSubcommands(
                                new SubcommandData("all", "Clear everything"),
                                new SubcommandData("user", "Clear data for a specific user")
                                        .addOption(OptionType.USER, "user", "The user to clear data for", true)
                        )
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS)),
                Commands.slash("set", "Sets the UUID for the start Embed.")
                        .addOption(OptionType.STRING, "uuid", "The UUID to set", true)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS))
        );

        guild.updateCommands().addCommands(commands).queue(
                success -> logger.info("Successfully registered guild commands."),
                failure -> logger.error("Failed to register guild commands.", failure)
        );
    }
}
