package discord.exception.client;

import discord.logging.LogMessage;

public class TreeRootNoValidCommitsException extends ClientGuildAwareException {
    public TreeRootNoValidCommitsException(LogMessage logMessage, String guildId) {
        super(logMessage, guildId);
    }
}
