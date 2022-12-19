package discord.exception;

import discord.localisation.LogMessage;

public class TreeRootNoValidCommitsException extends RuntimeException {
    public TreeRootNoValidCommitsException(LogMessage logMessage) {
        super(logMessage.name());
    }
}
