package discord;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class SpringApp {

    @Autowired
    private ApplicationArguments args;

    public static void main(String[] args) {
        new SpringApplicationBuilder(SpringApp.class)
                .build()
                .run(args);
    }

    /*public GatewayDiscordClient gatewayDiscordClient() {
        return DiscordClientBuilder.create()
    }*/
}
