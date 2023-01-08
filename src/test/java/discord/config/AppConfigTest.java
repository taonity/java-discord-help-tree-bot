package discord.config;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;

@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class, classes = {PropertyConfig.class})
@ExtendWith(SpringExtension.class)
class AppConfigTest {
    @Value("${discord.token}")
    String discordToken;



    @Test
    void gatewayDiscordClient() throws InterruptedException {
        final var gatewayDiscordClient = DiscordClientBuilder.create(discordToken).build()
                .gateway()
                .login()
                .block();

        final var member = gatewayDiscordClient.getMemberById(Snowflake.of(448934652992946176L), Snowflake.of(383277523561086979L)).block();
        System.out.println(member.getPresence().block());

        final var messageSpecs = MessageCreateSpec.builder()
                .addFile("file-name.txt", new ByteArrayInputStream("text in file".getBytes()))
                .addEmbed(EmbedCreateSpec.builder().title("test").build())
                .build();

        gatewayDiscordClient.getGuildById(Snowflake.of("448934652992946176")).blockOptional()
                .map(guild -> guild.getChannelById(Snowflake.of("1041419228051415200")))
                .map(Mono::block)
                .map(guildChannel -> (MessageChannel) guildChannel)
                .get()
                .createMessage(messageSpecs)
                .block();
    }
}