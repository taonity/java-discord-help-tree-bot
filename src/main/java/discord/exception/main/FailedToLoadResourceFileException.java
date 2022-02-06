package discord.exception.main;

import discord.logging.LogMessage;

public class FailedToLoadResourceFileException extends MainGuildAwareException {
    public FailedToLoadResourceFileException(LogMessage logMessage) {
        super(logMessage);
    }
}
