package org.taonity.helpbot.discord.logging.exception.main;

import org.taonity.helpbot.discord.logging.LogMessage;

public class FailedToCreateGiteaAdminTokenException extends MainGuildAwareException {
    public FailedToCreateGiteaAdminTokenException(LogMessage logMessage, Throwable cause) {
        super(logMessage, cause);
    }
}
