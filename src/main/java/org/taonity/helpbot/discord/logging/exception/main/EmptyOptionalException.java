package org.taonity.helpbot.discord.logging.exception.main;

import org.taonity.helpbot.discord.logging.LogMessage;

public class EmptyOptionalException extends MainGuildAwareException {
    public EmptyOptionalException(LogMessage logMessage) {
        super(logMessage);
    }
}
