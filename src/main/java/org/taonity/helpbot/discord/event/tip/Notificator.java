package org.taonity.helpbot.discord.event.tip;

import static org.taonity.helpbot.discord.localisation.LocalizedMessage.HELP_ADVICE_GENERATOR_MESSAGE;

import java.time.Instant;
import reactor.core.publisher.Mono;

public class Notificator {
    private long lastNotificationTime;
    private static final long ONE_HOUR = 60 * 60;

    public Notificator() {
        updateTime();
    }

    public Mono<Boolean> isTime() {
        return Mono.just(lastNotificationTime + ONE_HOUR < Instant.now().getEpochSecond());
    }

    public String getNotificationText() {
        updateTime();
        return HELP_ADVICE_GENERATOR_MESSAGE.getMerged("%s\n%s");
    }

    private void updateTime() {
        lastNotificationTime = Instant.now().getEpochSecond();
    }
}
