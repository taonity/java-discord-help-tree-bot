package discord.exception.client;

import discord.localisation.LogMessage;

public class TreeRootNoValidCommitsException extends ClientGuildAwareException {
    public TreeRootNoValidCommitsException(LogMessage logMessage, String guildId) {
        super(logMessage, guildId);
    }
}
