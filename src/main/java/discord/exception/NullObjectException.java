package discord.exception;

import discord.localisation.LogMessage;

public class NullObjectException extends RuntimeException {
    public NullObjectException(LogMessage logMessage) {
        super(logMessage.name());
    }
}
