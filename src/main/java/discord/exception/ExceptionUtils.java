package discord.exception;

import discord.logging.LogMessage;

public class ExceptionUtils {
    private static final String MESSAGE_FORMAT = "%s\n%s";

    public static String fromLogWithMessage(LogMessage logMessage, String message) {
        return String.format(MESSAGE_FORMAT, logMessage.name(), message);
    }
}
