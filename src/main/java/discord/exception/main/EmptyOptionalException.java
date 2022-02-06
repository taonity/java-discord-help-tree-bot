package discord.exception.main;

import discord.logging.LogMessage;

public class EmptyOptionalException extends MainGuildAwareException {
    public EmptyOptionalException(LogMessage logMessage) {
        super(logMessage);
    }
}
