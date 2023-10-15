package org.taonity.helpbot.discord.logging.exception;

import org.taonity.helpbot.discord.logging.LogMessage;

public class YamlProcessingException extends AbstractLogWithMessageException {
    public YamlProcessingException(LogMessage logMessage, String message) {
        super(logMessage, message);
    }
}
