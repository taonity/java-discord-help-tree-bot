package discord.exception;

import discord.localisation.LogMessage;

public class GiteaApiException extends Exception {
    public GiteaApiException(LogMessage logMessage, String message) {
        super(ExceptionUtils.fromLogWithMessage(logMessage, message));
    }
}
