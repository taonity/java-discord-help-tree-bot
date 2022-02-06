package discord.exception.main;

import discord.logging.LogMessage;

public class FailedToCreateNewRootException extends MainGuildAwareException {
    public FailedToCreateNewRootException(LogMessage logMessage) {
        super(logMessage);
    }
}
