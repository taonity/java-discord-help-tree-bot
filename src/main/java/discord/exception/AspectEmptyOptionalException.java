package discord.exception;

import discord.localisation.LogMessage;

public class AspectEmptyOptionalException extends LogMessageException {
    public AspectEmptyOptionalException(LogMessage logMessage, Throwable cause) {
        super(logMessage, cause);
    }
}
