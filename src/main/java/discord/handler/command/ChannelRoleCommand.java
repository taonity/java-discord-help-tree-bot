package discord.handler.command;

import discord.handler.EventPredicates;
import discord.model.GuildSettings;
import discord.services.MessageChannelService;
import discord.structure.ChannelRole;
import discord.structure.CommandName;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static discord.localisation.SimpleMessage.FAIL_CHANNEL_UPDATE_MESSAGE;
import static discord.localisation.SimpleMessage.SUCCESS_CHANNEL_UPDATE_MESSAGE;
import static discord.structure.CommandName.CHANNELROLE;

@Component
@RequiredArgsConstructor
@ConfigurationProperties(prefix="discord")
public class ChannelRoleCommand extends AbstractSlashCommand {
    private final static String CHANNEL_ROLE_OPTION = "role";
    @Getter
    private final CommandName command = CHANNELROLE;

    @Setter
    private List<String> userWhiteList;

    public final GuildSettings guildSettings;
    public final MessageChannelService channelService;
    private final EventPredicates eventPredicates;

    @Override
    public boolean filter(ChatInputInteractionEvent event) {
        return Stream.of(event)
                .filter(this::filterByCommand)
                .filter(e -> eventPredicates.filterByChannelId(e, guildSettings.getHelpChannelId()))
                .filter(e -> eventPredicates.filterByAuthorId(event, userWhiteList))
                .count() == 1;
    }

    @Override
    public void handle(ChatInputInteractionEvent event) {
        final var channelType = getChannelRoleFromEvent(event);

        if(channelType.isPresent()) {
            String channelId = event.getInteraction().getChannelId().asString();
            channelService.updateChannelById(channelType.get(), channelId);
            event.reply(SUCCESS_CHANNEL_UPDATE_MESSAGE.getMessage()).subscribe();
        } else {
            event.reply(FAIL_CHANNEL_UPDATE_MESSAGE.getMessage()).subscribe();
        }
    }

    private Optional<ChannelRole> getChannelRoleFromEvent(ChatInputInteractionEvent event) {
        String channelRole = event.getOption(CHANNEL_ROLE_OPTION)
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString)
                .orElse("");
        return ChannelRole.nullableValueOf(channelRole);
    }
}
