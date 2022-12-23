package discord;

import discord.exception.FailedToSearchRepoException;
import discord.localisation.LogMessage;
import discord.repository.GuildSettingsRepository;
import discord.services.GiteaApiService;
import discord.services.MessageChannelService;
import discord.structure.ChannelRole;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "discord")
@RequiredArgsConstructor
public class CmdRunner implements CommandLineRunner {

    @Setter
    private Map<String,String> githubUsers;

    private final GuildSettingsRepository guildSettingsRepository;

    private final MessageChannelService messageChannelService;

    private final GiteaApiService giteaApiService;


    @Override
    @Transactional
    public void run(String... args) throws Exception {

        throw new FailedToSearchRepoException(LogMessage.ALERT_20017, "448934652992946176");
    }
}
