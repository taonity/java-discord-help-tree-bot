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
        if(args.length != 2) {
            throw new IllegalArgumentException("Missing arguments");
        }
        final String token = args[0];
        final String helpTreePath = args[1];
        final DiscordClient client = DiscordClient.create(token);
        final GatewayDiscordClient gateway = client.login().block();
        final long appId = 935566371599826995L;
        //final long guildId = 448934652992946176L;
        //final long botChannelId = 935576925773127710L;
        final long guildId = 886293575128739860L;
        final long botChannelId = 938734627261141012L;

        gateway.on(ReadyEvent.class).subscribe(event -> {
            System.out.println("But have started!");

            final Guild guild = gateway.getGuildById(Snowflake.of(guildId)).block();
            final GuildChannel guildChannel = guild.getChannelById(Snowflake.of(botChannelId)).block();

            MessageChannel messageChannel = (MessageChannel) guildChannel;

            UserManager userManager = new UserManager(gateway, messageChannel, appId, guildId, helpTreePath);

        });

        gateway.on(ButtonInteractionEvent.class).subscribe(event -> {

        });


        gateway.onDisconnect().block();
    }
}
