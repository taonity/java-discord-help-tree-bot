package discord.exception;

import discord.localisation.LogMessage;

public abstract class MainGuildAwareException extends LogMessageException {
    public MainGuildAwareException(LogMessage logMessage) {
        super(logMessage);
    }
}
