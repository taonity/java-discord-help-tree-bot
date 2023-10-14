package org.taonity.helpbot.discord.logging.exception.client;

import lombok.Getter;
import org.taonity.helpbot.discord.logging.LogMessage;
import org.taonity.helpbot.discord.logging.exception.LogMessageException;

public abstract class ClientGuildAwareException extends LogMessageException {
    @Getter
    private final String guildId;

    public ClientGuildAwareException(LogMessage logMessage, String guildId) {
        super(logMessage);
        this.guildId = guildId;
    }

    public ClientGuildAwareException(LogMessage logMessage, String guildId, Throwable cause) {
        super(logMessage, cause);
        this.guildId = guildId;
    }
}
