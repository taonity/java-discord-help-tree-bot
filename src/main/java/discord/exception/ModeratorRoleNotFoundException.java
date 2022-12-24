package discord.exception;

import discord.localisation.LogMessage;

public class ModeratorRoleNotFoundException extends ClientGuildAwareException {
    public ModeratorRoleNotFoundException(LogMessage logMessage, String guildId) {
        super(logMessage, guildId);
    }
}
