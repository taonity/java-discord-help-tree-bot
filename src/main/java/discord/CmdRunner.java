package discord;

import discord.exception.EmptyOptionalException;
import discord.exception.FailedToSearchRepoException;
import discord.localisation.LogMessage;
import discord.repository.GuildSettingsRepository;
import discord.services.GiteaApiService;
import discord.services.MessageChannelService;
import discord.structure.ChannelRole;
import discord.structure.EmbedBuilder;
import discord.structure.EmbedType;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static discord.structure.EmbedBuilder.LOG_ATTACHMENT_FILE_NAME;

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
    private final GatewayDiscordClient gatewayDiscordClient;


    @Override
    @Transactional
    public void run(String... args) throws Exception {

        //throw new FailedToSearchRepoException(LogMessage.ALERT_20017, "448934652992946176");

    }
}
