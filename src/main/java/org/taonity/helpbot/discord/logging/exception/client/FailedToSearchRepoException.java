package org.taonity.helpbot.discord.logging.exception.client;

import org.taonity.helpbot.discord.logging.LogMessage;

public class FailedToSearchRepoException extends ClientGuildAwareException {
    public FailedToSearchRepoException(LogMessage logMessage, String guildId) {
        super(logMessage, guildId);
    }
}
