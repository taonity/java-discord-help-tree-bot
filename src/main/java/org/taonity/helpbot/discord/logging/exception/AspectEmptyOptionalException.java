package org.taonity.helpbot.discord.logging.exception;

import org.taonity.helpbot.discord.logging.LogMessage;

public class AspectEmptyOptionalException extends LogMessageException {
    public AspectEmptyOptionalException(LogMessage logMessage, Throwable cause) {
        super(logMessage, cause);
    }
}
