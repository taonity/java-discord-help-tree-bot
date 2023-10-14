package org.taonity.helpbot.discord.event.command;

import org.taonity.helpbot.discord.logging.LogMessage;
import org.taonity.helpbot.discord.logging.exception.main.AlphaNumericMaxNumberReachedException;

public class AlphaNumericGenerator {
    private static final String CHARACTERS_STRING = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int CHARACTERS_LENGTH = CHARACTERS_STRING.length();
    private static final char[] CHARACTERS_ARRAY = new char[CHARACTERS_LENGTH];

    private static final int NUMBER_AREA = 10_000_000;
    private static final int MAX_NUMBER = 50_000;

    static {
        CHARACTERS_STRING.getChars(0, CHARACTERS_LENGTH, CHARACTERS_ARRAY, 0);
    }

    private static String numberToAlphaNumeric(int number) {
        final var stringBuilder = new StringBuilder();

        while (number != 0) {
            stringBuilder.append(CHARACTERS_ARRAY[number % CHARACTERS_LENGTH]);
            number = Math.floorDiv(number, CHARACTERS_LENGTH);
        }

        if (stringBuilder.length() == 0) {
            stringBuilder.append(CHARACTERS_ARRAY[0]);
        }

        return stringBuilder.toString();
    }

    public static String generateFourCharFromNumber(int number) {
        if (number > MAX_NUMBER || number < 0) {
            throw new AlphaNumericMaxNumberReachedException(LogMessage.ALERT_20079);
        }
        return numberToAlphaNumeric(number + NUMBER_AREA);
    }
}
