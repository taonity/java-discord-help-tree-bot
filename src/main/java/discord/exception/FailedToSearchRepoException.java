package discord.exception;

import discord.localisation.LogMessage;

public class FailedToSearchRepoException extends RuntimeException {
    public FailedToSearchRepoException(LogMessage logMessage) {
        super(logMessage.name());
    }
}
