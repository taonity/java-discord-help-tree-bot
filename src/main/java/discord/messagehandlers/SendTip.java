package discord.messagehandlers;

import discord.Notificator;
import discord.commands.ChannelRole;
import discord.services.MessageChannelService;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class SendTip implements MessageHandler{
    private static final Logger log = LoggerFactory.getLogger(SendTip.class);
    private final Notificator notificator = new Notificator();

    private final MessageChannelService channelService;

    @Override
    public boolean condition(MessageCreateEvent event) {
        final var messageAuthor = event.getMessage().getAuthor();
        if (messageAuthor.isEmpty()) {
            log.info("Error! messageAuthor in MessageCreationEvent is empty.");
            return false;
        }

        final var currentChannel = event.getMessage().getChannel().block();
        if(currentChannel == null) {
            log.info("Error! currentMessage in MessageCreateEvent is null.");
            return false;
        }

        return currentChannel.getId().equals(channelService.getChannel(ChannelRole.HELP).getId()) &&
                notificator.isTime() && event.getMessage().getContent().length() > 6 &&
                !messageAuthor.get().isBot();
    }

    @Override
    public void handle(MessageCreateEvent event) {
        log.info("5");
        log.info("6");
        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .color(Color.CYAN)
                .title("Tip. Подсказка.")
                .description(notificator.getNotificationText().toString())
                .footer("This tip is triggered by random message every 1 hour", "")
                .build();
        log.info("7");
        channelService.getChannel(ChannelRole.HELP).createMessage(embed).block();
        log.info("8");
    }
}
