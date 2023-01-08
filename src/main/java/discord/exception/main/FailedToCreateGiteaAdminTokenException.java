package discord.exception.main;

import discord.localisation.LogMessage;

public class FailedToCreateGiteaAdminTokenException extends MainGuildAwareException {
    public FailedToCreateGiteaAdminTokenException(LogMessage logMessage, Throwable cause) {
        super(logMessage, cause);
    }
}
