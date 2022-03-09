package discord;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.rest.RestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class SpringApp {

    @Autowired
    private Configs configs;

    public static void main(String[] args) {
        new SpringApplicationBuilder(SpringApp.class)
                .build()
                .run(args);
    }

    @Bean
    public GatewayDiscordClient gatewayDiscordClient() {
        return DiscordClientBuilder.create(configs.getToken()).build()
                .gateway()
                .setInitialPresence(ignore -> ClientPresence.online(ClientActivity.listening("to /question")))
                .login()
                .block();
    }

    @Bean
    public List<SelectMenuManager> smManagers() {
        return new ArrayList<>();
    }

    @Bean
    Guild guild(GatewayDiscordClient client) {
        return client.getGuildById(Snowflake.of(configs.getGuildId())).block();
    }

    @Bean
    public MessageChannel messageChannel(Guild guild) {
        return (MessageChannel) guild.getChannelById(Snowflake.of(configs.getChannelId())).block();
    }

    @Bean
    public RestClient discordRestClient(GatewayDiscordClient client) {
        return client.getRestClient();
    }
}
