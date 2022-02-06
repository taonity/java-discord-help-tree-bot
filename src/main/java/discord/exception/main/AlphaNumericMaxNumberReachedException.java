package discord.exception.main;

import discord.logging.LogMessage;

public class AlphaNumericMaxNumberReachedException extends MainGuildAwareException {
    public AlphaNumericMaxNumberReachedException(LogMessage logMessage) {
        super(logMessage);
    }
}
