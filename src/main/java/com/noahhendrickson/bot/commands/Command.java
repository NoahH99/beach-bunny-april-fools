package com.noahhendrickson.bot.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public interface Command {

    void run(SlashCommandInteractionEvent event);

}
