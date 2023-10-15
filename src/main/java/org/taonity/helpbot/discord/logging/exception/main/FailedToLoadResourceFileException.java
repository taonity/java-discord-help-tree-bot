package org.taonity.helpbot.discord.logging.exception.main;

import org.taonity.helpbot.discord.logging.LogMessage;

public class FailedToLoadResourceFileException extends MainGuildAwareException {
    public FailedToLoadResourceFileException(LogMessage logMessage) {
        super(logMessage);
    }
}
