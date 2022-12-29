package discord.exception;

import discord.localisation.LogMessage;

public class AlphaNumericMaxNumberReachedException extends MainGuildAwareException {
    public AlphaNumericMaxNumberReachedException(LogMessage logMessage) {
        super(logMessage);
    }
}
