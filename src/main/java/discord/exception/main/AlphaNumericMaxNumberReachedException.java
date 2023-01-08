package discord.exception.main;

import discord.localisation.LogMessage;

public class AlphaNumericMaxNumberReachedException extends MainGuildAwareException {
    public AlphaNumericMaxNumberReachedException(LogMessage logMessage) {
        super(logMessage);
    }
}
