package org.taonity.helpbot.discord.logging.exception.main;

import org.taonity.helpbot.discord.logging.LogMessage;

public class FailedToRemoveGitApiWorkingDirException extends MainGuildAwareException {
    public FailedToRemoveGitApiWorkingDirException(LogMessage logMessage) {
        super(logMessage);
    }
}
