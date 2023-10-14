package org.taonity.helpbot.discord.logging.exception;

import org.taonity.helpbot.discord.logging.LogMessage;

public class NoCommitsException extends RuntimeException {
    public NoCommitsException(LogMessage logMessage) {
        super(logMessage.name());
    }
}
