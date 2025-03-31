package com.noahhendrickson.bot.commands;

import com.noahhendrickson.bot.BotContext;

public abstract class AbstractCommand implements Command {

    private final BotContext context;

    public AbstractCommand(BotContext context) {
        this.context = context;
    }

    public BotContext getContext() {
        return context;
    }
}
