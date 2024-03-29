package org.taonity.helpbot.discord.event.command.positive.channelrole;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.spec.EmbedCreateSpec;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
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
import org.taonity.helpbot.discord.event.command.AbstractPositiveSlashCommand;
import org.taonity.helpbot.discord.event.command.EventPredicates;
import org.taonity.helpbot.discord.localisation.SimpleMessage;
import org.taonity.helpbot.discord.logging.LogMessage;
import org.taonity.helpbot.discord.logging.exception.main.EmptyOptionalException;
import org.taonity.helpbot.discord.mdc.OnCompleteSignalListenerBuilder;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "discord")
public class ChannelRoleCommand extends AbstractPositiveSlashCommand {
    private static final String CHANNEL_ROLE_OPTION = "role";

    @Getter
    private final CommandName command = CommandName.CHANNELROLE;

    private final MessageChannelService channelService;
    private final EventPredicates eventPredicates;

    @Override
    public final List<Function<ChatInputInteractionEvent, Mono<Boolean>>> getFilterPredicates() {
        return Arrays.asList(eventPredicates::filterBot, this::filterByCommand, eventPredicates::filterByModeratorRole);
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        final var channelType = getChannelRoleFromEvent(event);
        Mono<EmbedCreateSpec> embedCreateSpecMono;

        if (channelType.isPresent()) {
            final var channelId = event.getInteraction().getChannelId().asString();
            final var guildId = event.getInteraction()
                    .getGuildId()
                    .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20060));
            embedCreateSpecMono = channelService
                    .updateChannelById(guildId.asString(), channelType.get(), channelId)
                    .thenReturn(EmbedBuilder.buildSimpleMessage(
                            SimpleMessage.SUCCESS_CHANNEL_UPDATE_MESSAGE.getMessage(),
                            EmbedType.SIMPLE_MESSAGE_EMBED_TYPE))
                    .tap(OnCompleteSignalListenerBuilder.of(() -> log.info(
                            "Command successfully defined role {} for channel {}",
                            channelType.get().getRoleName(),
                            event.getInteraction().getChannelId().asString())));
        } else {
            embedCreateSpecMono = Mono.just(EmbedBuilder.buildSimpleMessage(
                            SimpleMessage.FAIL_CHANNEL_UPDATE_MESSAGE.getMessage(),
                            EmbedType.SIMPLE_MESSAGE_EMBED_TYPE))
                    .tap(OnCompleteSignalListenerBuilder.of(() -> log.info(
                            "Command failed with empty role for channel {}",
                            event.getInteraction().getChannelId().asString())));
        }
        return embedCreateSpecMono.flatMap(
                embedCreateSpec -> event.reply().withEmbeds(embedCreateSpec).withEphemeral(true));
    }

    private Optional<ChannelRole> getChannelRoleFromEvent(ChatInputInteractionEvent event) {
        String channelRole = event.getOption(CHANNEL_ROLE_OPTION)
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString)
                .orElse("");
        return ChannelRole.nullableValueOf(channelRole);
    }
}
