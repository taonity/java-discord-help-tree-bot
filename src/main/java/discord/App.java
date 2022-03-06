package discord;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.*;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.MessageChannel;

public class App {
    public static void main(final String[] args) {
        if(args.length != 1) {
            throw new IllegalArgumentException("Missing argument");
        }
        var configFilePath = args[0];
        var configsManager = new ConfigsManager(configFilePath);
        final var configs = configsManager.getConfigs();
        final DiscordClient client = DiscordClient.create(configs.getToken());
        final GatewayDiscordClient gateway = client.login().block();
        if(gateway == null) {
            throw new NullPointerException("gateway is null");
        }
        final var applicationInfo = gateway.getApplicationInfo().block();
        if(applicationInfo == null) {
            throw new NullPointerException("applicationInfo is null");
        }
        final var appId = applicationInfo.getId().asLong();

        gateway.on(ReadyEvent.class).subscribe(event -> {
            System.out.println("But have started!");


            final Guild guild = gateway.getGuildById(Snowflake.of(configs.getGuildId())).block();
            if(guild == null) {
                throw new NullPointerException("guild is null");
            }
            final GuildChannel guildChannel = guild.getChannelById(Snowflake.of(configs.getChannelId())).block();

            MessageChannel messageChannel = (MessageChannel) guildChannel;

            UserManager userManager = new UserManager(
                    gateway,
                    messageChannel,
                    appId,
                    Long.parseLong(configs.getGuildId()),
                    configs.getTreePath(),
                    configs.getUserWhiteList());

        });

        gateway.on(ButtonInteractionEvent.class).subscribe(event -> {

        });


        gateway.onDisconnect().block();
    }
}
