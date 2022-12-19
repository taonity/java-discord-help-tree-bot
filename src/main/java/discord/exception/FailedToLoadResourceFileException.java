package discord.exception;

import discord.localisation.LogMessage;

public class FailedToLoadResourceFileException extends RuntimeException {
    public FailedToLoadResourceFileException(LogMessage logMessage) {
        super(logMessage.name());
    }
}
