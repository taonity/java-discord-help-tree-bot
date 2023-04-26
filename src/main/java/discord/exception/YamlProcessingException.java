package discord.exception;

import discord.logging.LogMessage;

public class YamlProcessingException extends AbstractLogWithMessageException {
    public YamlProcessingException(LogMessage logMessage, String message) {
        super(logMessage, message);
    }
}
