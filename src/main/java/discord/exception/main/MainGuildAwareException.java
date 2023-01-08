package discord.exception.main;

import discord.exception.LogMessageException;
import discord.localisation.LogMessage;

public abstract class MainGuildAwareException extends LogMessageException {
    public MainGuildAwareException(LogMessage logMessage) {
        super(logMessage);
    }

    public MainGuildAwareException(LogMessage logMessage, Throwable cause) {
        super(logMessage, cause);
    }
}
