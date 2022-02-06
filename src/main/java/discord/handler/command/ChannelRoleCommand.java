package discord.handler.command;

import static discord.localisation.SimpleMessage.FAIL_CHANNEL_UPDATE_MESSAGE;
import static discord.localisation.SimpleMessage.SUCCESS_CHANNEL_UPDATE_MESSAGE;
import static discord.structure.CommandName.CHANNELROLE;

import discord.exception.main.EmptyOptionalException;
import discord.handler.EventPredicates;
import discord.logging.LogMessage;
import discord.services.MessageChannelService;
import discord.structure.ChannelRole;
import discord.structure.CommandName;
import discord.structure.EmbedBuilder;
import discord.structure.EmbedType;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.entity.Member;
import discord4j.core.spec.EmbedCreateSpec;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "discord")
public class ChannelRoleCommand extends AbstractSlashCommand {
    private static final String CHANNEL_ROLE_OPTION = "role";

    @Getter
    private final CommandName command = CHANNELROLE;

    public final MessageChannelService channelService;
    private final EventPredicates eventPredicates;

    @Override
    public boolean filter(ChatInputInteractionEvent event) {
        return Stream.of(event)
                        .filter(eventPredicates::filterBot)
                        .filter(this::filterByCommand)
                        .filter(eventPredicates::filterByModeratorRole)
                        .count()
                == 1;
    }

    @Override
    public void handle(ChatInputInteractionEvent event) {
        final var channelType = getChannelRoleFromEvent(event);
        EmbedCreateSpec embedCreateSpec;

        if (channelType.isPresent()) {
            final var channelId = event.getInteraction().getChannelId().asString();
            final var guildId = event.getInteraction()
                    .getGuildId()
                    .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20060));
            channelService.updateChannelById(guildId.asString(), channelType.get(), channelId);

            embedCreateSpec = EmbedBuilder.buildSimpleMessage(
                    SUCCESS_CHANNEL_UPDATE_MESSAGE.getMessage(), EmbedType.SIMPLE_MESSAGE_EMBED_TYPE);

            log.info(
                    "Command {} successfully defined role {} for channel {} by user {} in guild {}",
                    command.getCommandName(),
                    channelType.get().getRoleName(),
                    event.getInteraction().getChannelId().asString(),
                    event.getInteraction()
                            .getMember()
                            .map(Member::getId)
                            .map(Snowflake::asString)
                            .orElse("NULL"),
                    event.getInteraction().getGuildId().map(Snowflake::asString).orElse("NULL"));
        } else {
            embedCreateSpec = EmbedBuilder.buildSimpleMessage(
                    FAIL_CHANNEL_UPDATE_MESSAGE.getMessage(), EmbedType.SIMPLE_MESSAGE_EMBED_TYPE);

            log.info(
                    "Command {} failed with empty role for channel {} by user {} in guild {}",
                    command.getCommandName(),
                    event.getInteraction().getChannelId().asString(),
                    event.getInteraction()
                            .getMember()
                            .map(Member::getId)
                            .map(Snowflake::asString)
                            .orElse("NULL"),
                    event.getInteraction().getGuildId().map(Snowflake::asString).orElse("NULL"));
        }
        event.reply().withEmbeds(embedCreateSpec).withEphemeral(true).subscribe();
    }

    private Optional<ChannelRole> getChannelRoleFromEvent(ChatInputInteractionEvent event) {
        String channelRole = event.getOption(CHANNEL_ROLE_OPTION)
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString)
                .orElse("");
        return ChannelRole.nullableValueOf(channelRole);
    }
}
