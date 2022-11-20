package discord.messagehandlers;

import discord.utils.SelectMenuManager;
import discord.UserStatus;
import discord.commands.ChannelRole;
import discord.services.MessageChannelService;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.spec.MessageCreateSpec;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RespondOnQuestion implements MessageHandler {
    private static final Logger log = LoggerFactory.getLogger(RespondOnQuestion.class);
    
    private final MessageChannelService channelService;
    private final List<SelectMenuManager> smManagers;
    private final GatewayDiscordClient client;

    @Override
    public boolean condition(MessageCreateEvent event) {
        // TODO: a lot of repeated code
        final var currentChannel = event.getMessage().getChannel().block();
        if(currentChannel == null) {
            log.info("Error! currentMessage in MessageCreateEvent is null.");
            return false;
        }
        final var messageAuthor = event.getMessage().getAuthor();
        if (messageAuthor.isEmpty()) {
            log.info("Error! messageAuthor in MessageCreationEvent is empty.");
            return false;
        }
        Snowflake authorId = messageAuthor.get().getId();

        SelectMenuManager smManager = smManagers.stream()
                .filter(manager -> authorId.equals(manager.getUserId()))
                .findAny()
                .orElse(null);
        if (smManager == null) {
            return false;
        }

        return currentChannel.getId().equals(channelService.getChannel(ChannelRole.HELP).getId()) &&
                !messageAuthor.get().isBot() &&
                smManager.getUserStatus() == UserStatus.WRITES_MESSAGE;
    }

    @Override
    public void handle(MessageCreateEvent event) {
        final var messageAuthor = event.getMessage().getAuthor();
        if (messageAuthor.isEmpty()) {
            log.info("Error! messageAuthor in MessageCreationEvent is empty.");
            return;
        }

        Snowflake authorId = messageAuthor.get().getId();

        SelectMenuManager smManager = smManagers.stream()
                .filter(manager -> authorId.equals(manager.getUserId()))
                .findAny()
                .orElse(null);
        if (smManager == null) {
            return;
        }
        String targetId = smManager.getTreeWalker().getCurrentNode().getChildText()
                .get(0).getTargetId();
        var targetUser = client.getUserById(Snowflake.of(targetId)).block();
        if (targetUser == null) {
            log.info("Error! targetUser in MessageCreateEvent is null.");
            return;
        }
        channelService.getChannel(ChannelRole.HELP).createMessage(MessageCreateSpec.builder()
                .messageReference(event.getMessage().getId())
                .content(targetUser.getMention())
                .build()
        ).block();
        smManagers.remove(smManager);
    }
}
