package discord.exception;

import discord.localisation.LogMessage;

public class NoCommitsException extends RuntimeException {
    public NoCommitsException(LogMessage logMessage) {
        super(logMessage.name());
    }
}
