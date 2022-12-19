package discord.exception;

import discord.localisation.LogMessage;

public class EmptyOptionalException extends RuntimeException {
    public EmptyOptionalException(LogMessage logMessage) {
        super(logMessage.name());
    }
}
