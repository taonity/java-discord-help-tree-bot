package org.taonity.helpbot.discord.logging.exception.client;

import org.taonity.helpbot.discord.logging.LogMessage;

public class ModeratorRoleNotFoundException extends ClientGuildAwareException {
    public ModeratorRoleNotFoundException(LogMessage logMessage, String guildId) {
        super(logMessage, guildId);
    }
}
