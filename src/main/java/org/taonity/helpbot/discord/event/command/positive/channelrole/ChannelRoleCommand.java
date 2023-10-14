package org.taonity.helpbot.discord.event.command.positive.channelrole;

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
import org.taonity.helpbot.discord.ChannelRole;
import org.taonity.helpbot.discord.CommandName;
import org.taonity.helpbot.discord.MessageChannelService;
import org.taonity.helpbot.discord.embed.EmbedBuilder;
import org.taonity.helpbot.discord.embed.EmbedType;
import org.taonity.helpbot.discord.event.command.AbstractSlashCommand;
import org.taonity.helpbot.discord.event.command.EventPredicates;
import org.taonity.helpbot.discord.localisation.SimpleMessage;
import org.taonity.helpbot.discord.logging.LogMessage;
import org.taonity.helpbot.discord.logging.exception.main.EmptyOptionalException;

@Slf4j
@Component
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "discord")
public class ChannelRoleCommand extends AbstractSlashCommand {
    private static final String CHANNEL_ROLE_OPTION = "role";

    @Getter
    private final CommandName command = CommandName.CHANNELROLE;

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
                    SimpleMessage.SUCCESS_CHANNEL_UPDATE_MESSAGE.getMessage(), EmbedType.SIMPLE_MESSAGE_EMBED_TYPE);

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
                    SimpleMessage.FAIL_CHANNEL_UPDATE_MESSAGE.getMessage(), EmbedType.SIMPLE_MESSAGE_EMBED_TYPE);

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
