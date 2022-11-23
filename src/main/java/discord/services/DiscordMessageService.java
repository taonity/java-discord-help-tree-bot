package discord.services;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static discord.localisation.LogMessage.ALERT_20001;
import static discord.localisation.LogMessage.ALERT_20002;
import static java.util.Objects.isNull;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordMessageService {
    private final GatewayDiscordClient client;

    public void sendMessageToUser(final String userId, final String message) {
        sendMessageToUser(Snowflake.of(userId), message);
    }

    public void sendMessageToUser(final Snowflake userId, final String message) {
        var user = client.getUserById(userId).block();
        if(isNull(user)) {
            log.warn(ALERT_20001.toString());
            return;
        }

        var privateChannel = user.getPrivateChannel().block();
        if(isNull(privateChannel)) {
            log.warn(ALERT_20002.toString());
            return;
        }

        privateChannel.createMessage(message).block();
    }
}
