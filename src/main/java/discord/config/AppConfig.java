package discord.config;

import discord.utils.SelectMenuManager;
import discord.repository.GuildSettingsRepository;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.rest.RestClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class
AppConfig {
    @Value("${discord.guildId}")
    String discordGuildId;

    @Value("${discord.token}")
    String discordToken;

    private final GuildSettingsRepository guildSettingsRepository;

    @Bean
    public GatewayDiscordClient gatewayDiscordClient() {
        return DiscordClientBuilder.create(discordToken).build()
                .gateway()
                .setInitialPresence(ignore -> ClientPresence.online(ClientActivity.listening("to /question")))
                .login()
                .block();
    }

    @Bean
    public List<SelectMenuManager> smManagers() {
        return new ArrayList<>();
    }

    @Bean
    Guild guild(GatewayDiscordClient client) {
        return client.getGuildById(Snowflake.of(discordGuildId)).block();
    }

    @Bean
    public RestClient discordRestClient(GatewayDiscordClient client) {
        return client.getRestClient();
    }
}
