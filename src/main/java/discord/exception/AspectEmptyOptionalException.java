package discord.exception;

import discord.logging.LogMessage;

public class AspectEmptyOptionalException extends LogMessageException {
    public AspectEmptyOptionalException(LogMessage logMessage, Throwable cause) {
        super(logMessage, cause);
    }
}
