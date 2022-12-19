package discord.services;

import discord.config.PropertyConfig;
import discord.model.GuildSettings;
import discord.repository.GuildSettingsRepository;
import discord.structure.ChannelRole;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.spec.MessageCreateSpec;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class, classes = {
        GiteaApiService.class, PropertyConfig.class, GiteaUserService.class, GitApiService.class})
@RunWith(SpringRunner.class)
public class GiteaUserServiceTest {

    @Autowired
    GiteaApiService giteaApiService;

    @Autowired
    GiteaUserService giteaUserService;

    @MockBean
    GuildSettingsRepository guildSettingsRepository;

    @MockBean
    GatewayDiscordClient gatewayDiscordClient;

    @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
    MessageChannelService messageChannelService;

    @Test
    public void testCreateUser() {
        giteaUserService.createUser("448934652992946176");
    }

    @Test
    public void testDeleteUser() {
        String guildId = "448934652992946176";
        int giteaUserId = 29;
        var guildSetting = GuildSettings.builder().build();
        guildSetting.setGiteaUserId(giteaUserId);
        when(guildSettingsRepository.findGuildSettingById(guildId)).thenReturn(Optional.of(guildSetting));
        giteaUserService.deleteUser(guildId);
    }

    @Test
    public void aaa() {
        when(gatewayDiscordClient.getUserById(any())).thenReturn(Mono.empty());
        //when(messageChannelService.getChannel(ChannelRole.LOG).createMessage(any(MessageCreateSpec.class))).thenReturn(Mono.empty());
        System.out.println(giteaUserService.getDialogRoot("123"));

    }

    @Test
    public void a() {

    }
}