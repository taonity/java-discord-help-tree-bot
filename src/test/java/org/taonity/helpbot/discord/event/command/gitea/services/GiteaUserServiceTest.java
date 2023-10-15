package org.taonity.helpbot.discord.event.command.gitea.services;

import static org.mockito.Mockito.when;

import discord4j.core.GatewayDiscordClient;
import java.util.Optional;
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
import org.taonity.helpbot.config.PropertyConfig;
import org.taonity.helpbot.discord.GuildSettings;
import org.taonity.helpbot.discord.GuildSettingsRepository;
import org.taonity.helpbot.discord.MessageChannelService;
import org.taonity.helpbot.discord.logging.exception.GiteaApiException;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@EntityScan("discord.model")
@EnableJpaRepositories(basePackages = {"discord.repository"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@ContextConfiguration(
        initializers = ConfigDataApplicationContextInitializer.class,
        classes = {GiteaApiService.class, PropertyConfig.class, GiteaUserService.class, GitApiService.class})
public class GiteaUserServiceTest {

    @Autowired
    GiteaUserService giteaUserService;

    @MockBean
    GuildSettingsRepository guildSettingsRepository;

    @MockBean
    GatewayDiscordClient gatewayDiscordClient;

    @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
    MessageChannelService messageChannelService;

    @Test
    @Disabled
    public void testCreateUser() throws GiteaApiException {
        giteaUserService.createUser(200);
    }

    @Test
    @Disabled
    public void testDeleteUser() throws GiteaApiException {
        String guildId = "448934652992946176";
        int giteaUserId = 9;
        var guildSetting = GuildSettings.builder()
                .guildId(guildId)
                .giteaUserId(giteaUserId)
                .id(100)
                .build();
        when(guildSettingsRepository.findGuildSettingByGuildId(guildId)).thenReturn(Optional.of(guildSetting));
        giteaUserService.deleteUser(guildId);
    }
}
