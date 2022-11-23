package discord.utils;

import java.time.Instant;

import static discord.localisation.LocalizedMessage.HELP_ADVICE_GENERATOR_MESSAGE;

public class Notificator {
    private long lastNotificationTime;
    private final static long ONE_HOUR = 60 * 60;

    public Notificator() {
        updateTime();
    }

    public boolean isTime() {
        return lastNotificationTime + ONE_HOUR < Instant.now().getEpochSecond();
    }

    public String getNotificationText() {
        updateTime();
        return HELP_ADVICE_GENERATOR_MESSAGE.getMerged("%s\n%s");
    }

    private void updateTime() {
        lastNotificationTime = Instant.now().getEpochSecond();
    }
}
