package org.taonity.helpbot.discord.logging.exception.main;

import org.taonity.helpbot.discord.logging.LogMessage;

public class AlphaNumericMaxNumberReachedException extends MainGuildAwareException {
    public AlphaNumericMaxNumberReachedException(LogMessage logMessage) {
        super(logMessage);
    }
}
