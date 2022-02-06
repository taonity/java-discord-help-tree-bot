package discord.exception.main;

import discord.logging.LogMessage;

public class FailedToCreateGiteaAdminTokenException extends MainGuildAwareException {
    public FailedToCreateGiteaAdminTokenException(LogMessage logMessage, Throwable cause) {
        super(logMessage, cause);
    }
}
