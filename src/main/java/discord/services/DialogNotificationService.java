package discord.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "discord")
public class DialogNotificationService {

    private final DiscordMessageService discordMessageService;

    @Getter
    @Setter
    private Map<String,String> githubUsers;

    public void sendNotification(String githubLogin, String message) {
        final String discordUserId = getDiscordUserIdByGithubLogin(githubLogin);

        discordMessageService.sendMessageToUser(discordUserId, message);
    }

    private String getDiscordUserIdByGithubLogin(String githubLogin) {
        return githubUsers.get(githubLogin);
    }
}
