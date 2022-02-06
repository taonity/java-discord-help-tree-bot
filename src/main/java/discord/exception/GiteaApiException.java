package discord.exception;

import discord.logging.LogMessage;

public class GiteaApiException extends Exception {
    public GiteaApiException(LogMessage logMessage, String message) {
        super(ExceptionUtils.fromLogWithMessage(logMessage, message));
    }
}
