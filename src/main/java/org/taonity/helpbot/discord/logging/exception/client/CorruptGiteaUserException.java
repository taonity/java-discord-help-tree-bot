package org.taonity.helpbot.discord.logging.exception.client;

import org.taonity.helpbot.discord.logging.LogMessage;

public class CorruptGiteaUserException extends ClientGuildAwareException {
    public CorruptGiteaUserException(LogMessage logMessage, String guildId) {
        super(logMessage, guildId);
    }

    public CorruptGiteaUserException(LogMessage logMessage, String guildId, Throwable cause) {
        super(logMessage, guildId, cause);
    }
}
