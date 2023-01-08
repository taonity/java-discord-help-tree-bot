package discord.services;

import discord.config.PropertyConfig;
import discord.repository.GuildSettingsRepository;
import discord.utils.CommitState;
import discord.utils.GitTestManager;
import discord4j.core.GatewayDiscordClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class, classes = {
        GiteaApiService.class, GitApiService.class, PropertyConfig.class, GiteaUserService.class, GitTestManager.class})
@RunWith(SpringRunner.class)
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
    public void aaa() throws InterruptedException {
        when(gatewayDiscordClient.getUserById(any())).thenReturn(Mono.empty());

        final var userName = "user_123";
        final var repoName = "repo_123";
        gitManager.createTestGitRepo(userName, repoName, List.of(
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
                CommitState.VALID
        ));

        //System.out.println(giteaUserService.getDialogRoot(3));

    }

    @Test
    public void aaa1() throws InterruptedException {
        //System.out.println(giteaUserService.getDialogRoot(3));
    }
}