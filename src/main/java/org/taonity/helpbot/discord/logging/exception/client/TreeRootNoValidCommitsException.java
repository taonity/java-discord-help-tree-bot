package org.taonity.helpbot.discord.logging.exception.client;

import org.taonity.helpbot.discord.logging.LogMessage;

public class TreeRootNoValidCommitsException extends ClientGuildAwareException {
    public TreeRootNoValidCommitsException(LogMessage logMessage, String guildId) {
        super(logMessage, guildId);
    }
}
