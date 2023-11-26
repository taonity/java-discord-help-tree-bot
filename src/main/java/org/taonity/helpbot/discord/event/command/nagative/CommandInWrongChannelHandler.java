package org.taonity.helpbot.discord.event.command.nagative;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.InteractionApplicationCommandCallbackReplyMono;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.taonity.helpbot.discord.ChannelRole;
import org.taonity.helpbot.discord.CommandName;
import org.taonity.helpbot.discord.MessageChannelService;
import org.taonity.helpbot.discord.embed.EmbedBuilder;
import org.taonity.helpbot.discord.embed.EmbedType;
import org.taonity.helpbot.discord.event.command.AbstractNegativeSlashCommand;
import org.taonity.helpbot.discord.event.command.EventPredicates;
import org.taonity.helpbot.discord.localisation.SimpleMessage;
import org.taonity.helpbot.discord.mdc.OnCompleteSignalListenerBuilder;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommandInWrongChannelHandler extends AbstractNegativeSlashCommand {

    @Getter
    private final List<CommandName> commands = Collections.singletonList(CommandName.QUESTION);

    private final EventPredicates eventPredicates;
    private final MessageChannelService messageChannelService;

    @Override
    public final List<Function<ChatInputInteractionEvent, Mono<Boolean>>> getFilterPredicates() {
        return Arrays.asList(
                eventPredicates::filterBot,
                this::filterByCommands,
                e -> eventPredicates.filterIfChannelExistsInSettings(e, ChannelRole.HELP),
                e -> eventPredicates.filterByChannelRole(e, ChannelRole.HELP).map(pass -> !pass));
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        return event.getInteraction()
                .getGuild()
                .flatMap(guild -> messageChannelService.getChannel(guild, ChannelRole.HELP))
                .flatMap(guildChannel -> sendEmbed(event, guildChannel))
                .tap(OnCompleteSignalListenerBuilder.of(() -> log.info(
                        "Command failed in wrong channel {}",
                        event.getInteraction().getChannelId().asString())));
    }

    private static InteractionApplicationCommandCallbackReplyMono sendEmbed(
            ChatInputInteractionEvent event, MessageChannel guildChannel) {
        return event.reply()
                .withEmbeds(EmbedBuilder.buildSimpleMessage(
                        getMessageString(guildChannel), EmbedType.SIMPLE_MESSAGE_EMBED_TYPE))
                .withEphemeral(true);
    }

    private static String getMessageString(MessageChannel guildChannel) {
        return String.format(
                SimpleMessage.COMMAND_IN_WRONG_CHANNEL_MESSAGE_FORMAT.getMessage(), guildChannel.getMention());
    }
}
