package discord.exception.client;

import discord.logging.LogMessage;

public class FailedToSearchRepoException extends ClientGuildAwareException {
    public FailedToSearchRepoException(LogMessage logMessage, String guildId) {
        super(logMessage, guildId);
    }
}
