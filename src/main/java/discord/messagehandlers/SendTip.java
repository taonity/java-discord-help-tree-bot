package discord.messagehandlers;

import discord.Notificator;
import discord.commands.UpdateCommand;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class SendTip implements MessageHandler{
    private static final Logger log = LoggerFactory.getLogger(UpdateCommand.class);
    private final Notificator notificator = new Notificator();


    MessageChannel messageChannel;

    public SendTip(MessageChannel messageChannel) {
        this.messageChannel = messageChannel;
    }

    @Override
    public boolean condition(MessageCreateEvent event) {
        final var messageAuthor = event.getMessage().getAuthor();
        if (messageAuthor.isEmpty()) {
            log.info("Error! messageAuthor in MessageCreationEvent is empty.");
            return false;
        }

        return notificator.isTime() && event.getMessage().getContent().length() > 6 &&
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
        messageChannel.createMessage(embed).block();
        log.info("8");
    }
}
