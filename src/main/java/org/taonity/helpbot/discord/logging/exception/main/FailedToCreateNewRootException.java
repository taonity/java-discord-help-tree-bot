package org.taonity.helpbot.discord.logging.exception.main;

import org.taonity.helpbot.discord.logging.LogMessage;

public class FailedToCreateNewRootException extends MainGuildAwareException {
    public FailedToCreateNewRootException(LogMessage logMessage) {
        super(logMessage);
    }
}
