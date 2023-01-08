package discord.exception;

import discord.localisation.LogMessage;

public class NoCommitsException extends Exception {
    public NoCommitsException(LogMessage logMessage) {
        super(logMessage.name());
    }
}
