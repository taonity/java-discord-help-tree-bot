package discord.exception;

import discord.localisation.LogMessage;

public class ExceptionUtils {
    private final static String MESSAGE_FORMAT = "%s\n%s";

    public static String fromLogWithMessage(LogMessage logMessage, String message) {
        return String.format(MESSAGE_FORMAT, logMessage.name(), message);
    }
}
