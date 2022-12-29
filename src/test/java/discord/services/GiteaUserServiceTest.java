package discord.services;

import discord.config.PropertyConfig;
import discord.exception.GiteaApiException;
import discord.model.GuildSettings;
import discord.repository.GuildSettingsRepository;
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

import java.util.HashSet;
import java.util.Optional;

import static discord.utils.AlphaNumericGenerator.generateFourCharFromNumber;
import static org.assertj.core.api.Assertions.assertThat;
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
    public void testCreateUser() throws GiteaApiException {
        giteaUserService.createUser(100);
    }

    @Test
    public void testDeleteUser() throws GiteaApiException {
        String guildId = "448934652992946176";
        int giteaUserId = 38;
        var guildSetting = GuildSettings.builder()
                .guildId(guildId)
                .giteaUserId(giteaUserId)
                .id(100)
                .build();
        when(guildSettingsRepository.findGuildSettingByGuildId(guildId)).thenReturn(Optional.of(guildSetting));
        giteaUserService.deleteUser(guildId);
    }

    @Test
    public void aaa() {
        when(gatewayDiscordClient.getUserById(any())).thenReturn(Mono.empty());
        //when(messageChannelService.getChannel(ChannelRole.LOG).createMessage(any(MessageCreateSpec.class))).thenReturn(Mono.empty());
        //System.out.println(giteaUserService.getDialogRoot(3));

    }

    public String g(int number) {
        String chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int charsLength = chars.length();
        char[] charsArray = new char[chars.length()];
        chars.getChars(0, charsLength, charsArray,0);
        StringBuilder stringBuilder = new StringBuilder();
        while (number != 0) {
            stringBuilder.append(charsArray[number % charsLength]);
            number = Math.floorDiv(number, charsLength);
        }
        if(stringBuilder.length() == 0) {
            stringBuilder.append(charsArray[0]);
        }
        return stringBuilder.toString();
    }

    @Test
    public void a() {
        System.out.println(generateFourCharFromNumber(1));
        System.out.println(generateFourCharFromNumber(50000));
        final var set = new HashSet<String>();

        for(var i = 0; i < 500000; i++) {
            set.add(g(i));
        }

        assertThat(set.size()).isEqualTo(500000);
    }
}