package discord.exception;

import discord.logging.LogMessage;

public class NoCommitsException extends RuntimeException {
    public NoCommitsException(LogMessage logMessage) {
        super(logMessage.name());
    }
}
