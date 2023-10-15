package org.taonity.helpbot.discord.logging.exception;

import org.taonity.helpbot.discord.logging.LogMessage;

public class GiteaApiException extends Exception {
    public GiteaApiException(LogMessage logMessage, String message) {
        super(ExceptionUtils.fromLogWithMessage(logMessage, message));
    }
}
