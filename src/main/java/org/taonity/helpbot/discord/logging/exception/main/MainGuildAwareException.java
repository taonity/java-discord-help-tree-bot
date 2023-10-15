package org.taonity.helpbot.discord.logging.exception.main;

import org.taonity.helpbot.discord.logging.LogMessage;
import org.taonity.helpbot.discord.logging.exception.LogMessageException;

public abstract class MainGuildAwareException extends LogMessageException {
    public MainGuildAwareException(LogMessage logMessage) {
        super(logMessage);
    }

    public MainGuildAwareException(LogMessage logMessage, Throwable cause) {
        super(logMessage, cause);
    }
}
