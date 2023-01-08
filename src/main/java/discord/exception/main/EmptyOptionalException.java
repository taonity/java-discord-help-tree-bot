package discord.exception.main;

import discord.localisation.LogMessage;

public class EmptyOptionalException extends MainGuildAwareException {
    public EmptyOptionalException(LogMessage logMessage) {
        super(logMessage);
    }
}
