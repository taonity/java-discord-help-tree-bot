package discord.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import discord.config.PropertyConfig;
import discord.exception.GiteaApiException;
import discord.model.GuildSettings;
import discord.repository.GuildSettingsRepository;
import discord.utils.CommitState;
import discord.utils.GitTestManager;
import discord4j.core.GatewayDiscordClient;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@EntityScan("discord.model")
@EnableJpaRepositories(basePackages = {"discord.repository"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@ContextConfiguration(
        initializers = ConfigDataApplicationContextInitializer.class,
        classes = {
            GiteaApiService.class,
            GitApiService.class,
            PropertyConfig.class,
            GiteaUserService.class,
            GitTestManager.class
        })
public class GiteaUserServiceGetDialogRootTest {

    @Autowired
    GiteaUserService giteaUserService;

    @Autowired
    GitTestManager gitManager;

    @MockBean
    GuildSettingsRepository guildSettingsRepository;

    @MockBean
    GatewayDiscordClient gatewayDiscordClient;

    @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
    MessageChannelService messageChannelService;

    @Test
    @Disabled
    public void getDialogRoot() throws InterruptedException, GiteaApiException {
        when(gatewayDiscordClient.getUserById(any())).thenReturn(Mono.empty());

        final var userName = "user_WtXF";
        final var repoName = "repo_WtXF";
        gitManager.createTestGitRepo(
                userName,
                repoName,
                List.of(
                        CommitState.VALID,
                        CommitState.VALID,
                        CommitState.VALID,
                        CommitState.VALID,
                        CommitState.VALID,
                        CommitState.VALID,
                        CommitState.VALID,
                        CommitState.VALID,
                        CommitState.VALID,
                        CommitState.VALID,
                        CommitState.VALID));

        final var guildSettings = GuildSettings.builder().id(100).guildId("123").build();

        System.out.println(giteaUserService.getDialogRoot(guildSettings));
    }
}
