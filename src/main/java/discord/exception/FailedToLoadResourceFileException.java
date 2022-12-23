package discord.exception;

import discord.localisation.LogMessage;

public class FailedToLoadResourceFileException extends MainGuildAwareException {
    public FailedToLoadResourceFileException(LogMessage logMessage) {
        super(logMessage);
    }
}
