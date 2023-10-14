package org.taonity.helpbot.discord.logging.exception.main;

import org.taonity.helpbot.discord.logging.LogMessage;

public class MainInterruptedException extends MainGuildAwareException {
    public MainInterruptedException(LogMessage logMessage, Throwable cause) {
        super(logMessage, cause);
    }
}
