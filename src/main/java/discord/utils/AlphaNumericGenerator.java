package discord.utils;

import discord.exception.main.AlphaNumericMaxNumberReachedException;
import discord.localisation.LogMessage;

public class AlphaNumericGenerator {
    private final static String CHARACTERS_STRING = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final static int CHARACTERS_LENGTH = CHARACTERS_STRING.length();
    private final static char[] CHARACTERS_ARRAY = new char[CHARACTERS_LENGTH];

    private final static int NUMBER_AREA = 10_000_000;
    private final static int MAX_NUMBER = 50_000;

    static {
        CHARACTERS_STRING.getChars(0, CHARACTERS_LENGTH, CHARACTERS_ARRAY,0);
    }


    private static String numberToAlphaNumeric(int number) {
        final var stringBuilder = new StringBuilder();

        while (number != 0) {
            stringBuilder.append(CHARACTERS_ARRAY[number % CHARACTERS_LENGTH]);
            number = Math.floorDiv(number, CHARACTERS_LENGTH);
        }

        if(stringBuilder.length() == 0) {
            stringBuilder.append(CHARACTERS_ARRAY[0]);
        }

        return stringBuilder.toString();
    }

    public static String generateFourCharFromNumber(int number) {
        if(number > MAX_NUMBER || number < 0) {
            throw new AlphaNumericMaxNumberReachedException(LogMessage.ALERT_20079);
        }
        return numberToAlphaNumeric(number + NUMBER_AREA);
    }
}
