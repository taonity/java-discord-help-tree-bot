package discord.exception.client;

import discord.logging.LogMessage;

public class ModeratorRoleNotFoundException extends ClientGuildAwareException {
    public ModeratorRoleNotFoundException(LogMessage logMessage, String guildId) {
        super(logMessage, guildId);
    }
}
