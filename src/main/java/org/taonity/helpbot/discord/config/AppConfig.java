package org.taonity.helpbot.discord.config;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.rest.RestClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.taonity.helpbot.discord.logging.LogMessage;
import org.taonity.helpbot.discord.logging.exception.main.EmptyOptionalException;

@Configuration
@RequiredArgsConstructor
public class AppConfig {

    @Value("${discord.token}")
    String discordToken;

    @Bean
    public GatewayDiscordClient gatewayDiscordClient() {
        return DiscordClientBuilder.create(discordToken)
                .build()
                .gateway()
                .setInitialPresence(ignore -> ClientPresence.online(ClientActivity.listening("/question")))
                .login()
                .blockOptional()
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20040));
    }

    @Bean
    public RestClient discordRestClient(GatewayDiscordClient client) {
        return client.getRestClient();
    }
}
