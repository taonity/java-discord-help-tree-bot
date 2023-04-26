package discord.exception;

import discord.logging.LogMessage;

public class NoCommitsException extends Exception {
    public NoCommitsException(LogMessage logMessage) {
        super(logMessage.name());
    }
}
