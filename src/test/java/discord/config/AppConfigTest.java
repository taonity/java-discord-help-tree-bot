package discord.config;

import discord.exception.FailedToSearchRepoException;
import discord.exception.handling.LoggingAspect;
import discord.localisation.LogMessage;
import discord.services.GitApiService;
import discord.utils.YamlPropertySourceFactory;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class, classes = {PropertyConfig.class})
@PropertySource(value = "classpath:/secret.yaml", factory = YamlPropertySourceFactory.class)
@ExtendWith(SpringExtension.class)
class AppConfigTest {
    @Value("${discord.token}")
    String discordToken;

    @Autowired
    LoggingAspect loggingAspect;


    @Test
    void gatewayDiscordClient() throws InterruptedException {
        final var gatewayDiscordClient = DiscordClientBuilder.create(discordToken).build()
                .gateway()
                .setInitialPresence(ignore -> ClientPresence.online(ClientActivity.listening("to /question")))
                .login()
                .block();

        //throw new FailedToSearchRepoException(LogMessage.ALERT_20017, "448934652992946176");

        loggingAspect.logAfterThrowingAllMethods(new FailedToSearchRepoException(LogMessage.ALERT_20017, "448934652992946176"));
        //System.out.println(gatewayDiscordClient.getGuilds().cache().block);
    }
}