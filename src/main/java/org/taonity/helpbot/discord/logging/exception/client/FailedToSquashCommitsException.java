package org.taonity.helpbot.discord.logging.exception.client;

import org.taonity.helpbot.discord.logging.LogMessage;

public class FailedToSquashCommitsException extends ClientGuildAwareException {
    public FailedToSquashCommitsException(LogMessage logMessage, String guildId) {
        super(logMessage, guildId);
    }
}
