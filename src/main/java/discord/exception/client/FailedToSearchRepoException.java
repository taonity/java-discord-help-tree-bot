package discord.exception.client;

import discord.localisation.LogMessage;

public class FailedToSearchRepoException extends ClientGuildAwareException {
    public FailedToSearchRepoException(LogMessage logMessage, String guildId) {
        super(logMessage, guildId);
    }
}
