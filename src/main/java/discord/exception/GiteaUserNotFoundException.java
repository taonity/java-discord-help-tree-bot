package discord.exception;

import discord.localisation.LogMessage;

public class GiteaUserNotFoundException extends ClientGuildAwareException {
    public GiteaUserNotFoundException(LogMessage logMessage, String guildId) {
        super(logMessage, guildId);
    }
}
