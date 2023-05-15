package discord.automation.config;

import discord.exception.main.EmptyOptionalException;
import discord.logging.LogMessage;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;

@Configuration
public class DiscordAutomationBotConfig {
    @Value("${discord.automation.token}")
    String discordToken;

    @Value("${discord.automation.channelId}")
    String automationChannelId;

    @Value("${discord.automation.guildId}")
    String automationGuildId;

    @Bean
    public GatewayDiscordClient gatewayDiscordClient() {
        return DiscordClientBuilder.create(discordToken).build()
                .gateway()
                .setInitialPresence(ignore -> ClientPresence.online(ClientActivity.listening("automation")))
                .login()
                .blockOptional()
                .orElseThrow(() -> new RuntimeException("Failed to login with automation bot"));
    }

    @Bean
    public Guild guild(GatewayDiscordClient automationClient) {
        return automationClient.getGuildById(Snowflake.of(automationGuildId)).blockOptional()
                .orElseThrow(() -> new RuntimeException("Failed to init automation guild"));
    }

    @Bean
    public MessageChannel messageChannel(Guild guild) {
        return guild.getChannelById(Snowflake.of(automationChannelId))
                .ofType(MessageChannel.class)
                .blockOptional()
                .orElseThrow(() -> new RuntimeException("Failed to init automation channel"));
    }
}
