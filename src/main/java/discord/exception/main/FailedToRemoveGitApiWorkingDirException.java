package discord.exception.main;

import discord.logging.LogMessage;

public class FailedToRemoveGitApiWorkingDirException extends MainGuildAwareException {
    public FailedToRemoveGitApiWorkingDirException(LogMessage logMessage) {
        super(logMessage);
    }
}
