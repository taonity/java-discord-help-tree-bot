package discord.exception.client;

import discord.logging.LogMessage;

public class FailedToSquashCommitsException extends ClientGuildAwareException {
    public FailedToSquashCommitsException(LogMessage logMessage, String guildId) {
        super(logMessage, guildId);
    }
}
