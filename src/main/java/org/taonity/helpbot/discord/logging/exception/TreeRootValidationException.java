package org.taonity.helpbot.discord.logging.exception;

import org.taonity.helpbot.discord.logging.LogMessage;

public class TreeRootValidationException extends AbstractLogWithMessageException {
    public TreeRootValidationException(LogMessage logMessage, String message) {
        super(logMessage, message);
    }
}
