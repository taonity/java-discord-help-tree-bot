package discord.exception;

import discord.localisation.LogMessage;

public abstract class LogMessageException extends RuntimeException {
    public LogMessageException(LogMessage logMessage) {
        super(logMessage.name());
    }

    public LogMessageException(LogMessage logMessage, Throwable cause) {
        super(logMessage.name(), cause);
    }
}
