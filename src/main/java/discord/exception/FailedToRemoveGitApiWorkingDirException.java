package discord.exception;

import discord.localisation.LogMessage;

public class FailedToRemoveGitApiWorkingDirException extends MainGuildAwareException {
    public FailedToRemoveGitApiWorkingDirException(LogMessage logMessage) {
        super(logMessage);
    }
}
