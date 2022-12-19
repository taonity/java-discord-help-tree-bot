package discord.exception;

import discord.localisation.LogMessage;

public class FailedToSquashCommitsException extends RuntimeException {
    public FailedToSquashCommitsException(LogMessage logMessage) {
        super(logMessage.name());
    }
}
