package discord.exception;

import discord.logging.LogMessage;

public abstract class AbstractLogWithMessageException extends Exception {
    private static final String MESSAGE_FORMAT = "%s\n%s";

    public AbstractLogWithMessageException(LogMessage logMessage, String message) {
        super(String.format(MESSAGE_FORMAT, logMessage.name(), message));
    }
}
