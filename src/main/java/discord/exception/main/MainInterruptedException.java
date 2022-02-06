package discord.exception.main;

import discord.logging.LogMessage;

public class MainInterruptedException extends MainGuildAwareException {
    public MainInterruptedException(LogMessage logMessage, Throwable cause) {
        super(logMessage, cause);
    }
}
