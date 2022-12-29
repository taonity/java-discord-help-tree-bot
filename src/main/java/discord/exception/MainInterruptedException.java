package discord.exception;

import discord.localisation.LogMessage;

public class MainInterruptedException extends ClientGuildAwareException {
    public MainInterruptedException(LogMessage logMessage, String guildId, Throwable cause) {
        super(logMessage, guildId, cause);
    }
}
