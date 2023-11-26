package org.taonity.helpbot.discord.event.command.nagative;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.Role;
import discord4j.core.spec.InteractionApplicationCommandCallbackReplyMono;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.taonity.helpbot.discord.ChannelRole;
import org.taonity.helpbot.discord.CommandName;
import org.taonity.helpbot.discord.embed.EmbedBuilder;
import org.taonity.helpbot.discord.embed.EmbedType;
import org.taonity.helpbot.discord.event.command.AbstractNegativeSlashCommand;
import org.taonity.helpbot.discord.event.command.EventPredicates;
import org.taonity.helpbot.discord.event.joinleave.service.GuildRoleService;
import org.taonity.helpbot.discord.localisation.SimpleMessage;
import org.taonity.helpbot.discord.logging.LogMessage;
import org.taonity.helpbot.discord.logging.exception.client.ModeratorRoleNotFoundException;
import org.taonity.helpbot.discord.logging.exception.main.EmptyOptionalException;
import org.taonity.helpbot.discord.mdc.OnCompleteSignalListenerBuilder;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class IsNotModeratorHandler extends AbstractNegativeSlashCommand {

    @Getter
    private final List<CommandName> commands = Arrays.asList(CommandName.CONFIG, CommandName.CHANNELROLE);

    private final EventPredicates eventPredicates;

    @Override
    public final List<Function<ChatInputInteractionEvent, Mono<Boolean>>> getFilterPredicates() {
        return Arrays.asList(
                eventPredicates::filterBot,
                this::filterByCommands,
                e -> eventPredicates.filterIfChannelExistsInSettings(e, ChannelRole.HELP),
                e -> eventPredicates.filterByModeratorRole(e).map(pass -> !pass));
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        final var guildId = event.getInteraction()
                .getGuildId()
                .map(Snowflake::asString)
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20071));
        return event.getInteraction()
                .getGuild()
                .flatMap(guild -> guild.getRoles().collectList())
                .flatMap(roles -> sendEmbed(event, roles, guildId))
                .tap(OnCompleteSignalListenerBuilder.of(
                        () -> log.info("Command failed to execute by non-moderator user")));
    }

    private static InteractionApplicationCommandCallbackReplyMono sendEmbed(
            ChatInputInteractionEvent event, List<Role> roles, String guildId) {
        return event.reply()
                .withEmbeds(EmbedBuilder.buildSimpleMessage(
                        buildMessageString(getMention(roles, guildId)), EmbedType.SIMPLE_MESSAGE_EMBED_TYPE))
                .withEphemeral(true);
    }

    private static String buildMessageString(String roles) {
        return String.format(SimpleMessage.MUST_BE_MODERATOR_MESSAGE_FORMAT.getMessage(), roles);
    }

    private static String getMention(List<Role> roles, String guildId) {
        return roles.stream()
                .filter(role -> role.getName().equals(GuildRoleService.MODERATOR_ROLE_NAME))
                .findFirst()
                .orElseThrow(() -> new ModeratorRoleNotFoundException(LogMessage.ALERT_20059, guildId))
                .getMention();
    }
}
