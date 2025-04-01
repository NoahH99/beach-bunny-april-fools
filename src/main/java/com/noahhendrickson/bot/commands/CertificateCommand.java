package com.noahhendrickson.bot.commands;

import com.noahhendrickson.bot.BotContext;
import com.noahhendrickson.bot.util.CertificateGenerator;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.FileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class CertificateCommand extends AbstractCommand {

    private static final Logger logger = LoggerFactory.getLogger(CertificateCommand.class);

    public CertificateCommand(BotContext context) {
        super(context);
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        OptionMapping option = event.getOption("user");
        User user = option != null ? option.getAsUser() : event.getUser();

        String username = user.getName();

        logger.info("Certificate command invoked by {} for user {}", event.getUser().getIdLong(), username);

        try {
            File certificate = CertificateGenerator.generateCertificate(username);

            event.reply("🎓 Here is your certificate, " + username + "!")
                    .addFiles(FileUpload.fromData(certificate))
                    .queue();

            logger.info("Successfully sent certificate to {}", username);

        } catch (Exception e) {
            logger.error("Failed to generate or send certificate for {}", username, e);
            event.reply("<:tickNo:697249739250335775> Failed to generate certificate for " + username + ".").setEphemeral(true).queue();
        }
    }
}
