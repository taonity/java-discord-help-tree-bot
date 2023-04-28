package discord.exception.main;

import discord.logging.LogMessage;

public class UnexpectedGiteaApiException extends MainGuildAwareException {
    public UnexpectedGiteaApiException(LogMessage logMessage, Throwable cause) {
        super(logMessage, cause);
    }
}
