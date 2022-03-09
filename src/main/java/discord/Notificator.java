package discord;

import discord.localisation.LocalizedText;

import java.time.Instant;

public class Notificator {
    private long lastNotificationTime;
    private final long oneHour = 60 * 60;
    private final LocalizedText notificationText = new LocalizedText(
            "If you have any questions or need help - write `/question`.",
            "Если у тебя возникли вопросы или тебе нужна помощь - напиши `/question`."
    );

    public Notificator() {
        updateTime();
    }

    public boolean isTime() {
        return lastNotificationTime + oneHour < Instant.now().getEpochSecond();
    }

    public LocalizedText getNotificationText() {
        updateTime();
        return notificationText;
    }

    private void updateTime() {
        lastNotificationTime = Instant.now().getEpochSecond();
    }
}
