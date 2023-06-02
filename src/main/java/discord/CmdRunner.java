package discord;

import discord.repository.GuildSettingsRepository;
import discord.services.GiteaApiService;
import discord.services.MessageChannelService;
import discord4j.core.GatewayDiscordClient;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "discord")
@RequiredArgsConstructor
public class CmdRunner implements CommandLineRunner {

    @Setter
    private Map<String, String> githubUsers;

    private final GuildSettingsRepository guildSettingsRepository;

    private final MessageChannelService messageChannelService;

    private final GiteaApiService giteaApiService;
    private final GatewayDiscordClient gatewayDiscordClient;

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        // throw new FailedToSearchRepoException(LogMessage.ALERT_20017, "448934652992946176");

    }
}
