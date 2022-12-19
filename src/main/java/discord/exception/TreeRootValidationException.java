package discord.exception;

import discord.localisation.LogMessage;

public class TreeRootValidationException extends AbstractLogWithMessageException {
    public TreeRootValidationException(LogMessage logMessage, String message) {
        super(logMessage, message);
    }
}
