package discord.exception;

import discord.localisation.LogMessage;

public class GiteaApiException extends AbstractLogWithMessageException {
    public GiteaApiException(LogMessage logMessage, String message) {
        super(logMessage, message);
    }
}
