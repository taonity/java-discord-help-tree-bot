package discord.exception;

import discord.localisation.LogMessage;
import lombok.Getter;

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
