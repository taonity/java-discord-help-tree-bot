package discord.exception;

import discord.logging.LogMessage;

public class TreeRootValidationException extends AbstractLogWithMessageException {
    public TreeRootValidationException(LogMessage logMessage, String message) {
        super(logMessage, message);
    }
}
