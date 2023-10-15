package org.taonity.helpbot.discord.logging.exception.main;

import org.taonity.helpbot.discord.logging.LogMessage;

public class UnexpectedGiteaApiException extends MainGuildAwareException {
    public UnexpectedGiteaApiException(LogMessage logMessage, Throwable cause) {
        super(logMessage, cause);
    }
}
