package org.taonity.helpbot.discord.event.joinleave;

import static java.util.Objects.isNull;
import static org.taonity.helpbot.discord.localisation.SimpleMessage.ON_GUILD_JOIN_INSTRUCTIONS;
import static org.taonity.helpbot.discord.mdc.ContextRegistryMdcKeyRegister.GUILD_ID_MDC_KEY;

import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.object.audit.ActionType;
import discord4j.core.object.audit.AuditLogEntry;
import discord4j.core.object.audit.AuditLogPart;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.core.spec.AuditLogQuerySpec;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.taonity.helpbot.discord.GuildSettingsRepository;
import org.taonity.helpbot.discord.event.DiscordEventListener;
import org.taonity.helpbot.discord.event.joinleave.service.GuildDataService;
import org.taonity.helpbot.discord.logging.LogMessage;
import org.taonity.helpbot.discord.logging.exception.main.EmptyOptionalException;
import org.taonity.helpbot.discord.mdc.OnCompleteSignalListenerBuilder;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

@Slf4j
@Component
@RequiredArgsConstructor
public class GuildCreateListener implements DiscordEventListener<GuildCreateEvent> {

    private final GuildSettingsRepository guildSettingsRepository;
    private final GuildDataService guildDataService;

    @Override
    public ContextView getContextView(GuildCreateEvent event) {
        return Context.of(GUILD_ID_MDC_KEY, event.getGuild().getId().asString());
    }

    @Override
    @Transactional
    public Mono<Void> handle(GuildCreateEvent event) {
        final var guildId = event.getGuild().getId().asString();
        return Mono.just(guildId)
                .flatMap(guildSettingsRepository::findGuildSettingByGuildId)
                .switchIfEmpty(guildDataService
                        .removeIfLeftInDiscord()
                        .then(guildDataService.create(guildId))
                        .then(sendInstructionMessage(event))
                        .tap(OnCompleteSignalListenerBuilder.of(() -> log.info("New guild was initialised")))
                        .then(Mono.empty()))
                .flatMap(guildSettings -> {
                    // means that bot didn't really join the guild
                    return Mono.<Void>empty()
                            .tap(OnCompleteSignalListenerBuilder.of(() -> log.info("Existing guild was initialised")));
                })
                .then();
    }

    private static Mono<Message> sendInstructionMessage(GuildCreateEvent event) {
        return getAuditLog(event).flatMap(auditLogParts -> {
            final var auditLogPart = auditLogParts.get(0);
            if (isNull(auditLogPart)) {
                return Mono.error(new EmptyOptionalException(LogMessage.ALERT_20084));
            } else {
                final var auditLogEntry = auditLogPart.getEntries().get(0);
                if (isNull(auditLogEntry)) {
                    return Mono.error(new EmptyOptionalException(LogMessage.ALERT_20084));
                } else {
                    return getPrivateChannel(auditLogEntry)
                            .flatMap(privateChannel ->
                                    privateChannel.createMessage(ON_GUILD_JOIN_INSTRUCTIONS.getMessage()))
                            .tap(OnCompleteSignalListenerBuilder.of(
                                    () -> log.info("Instructions for new guild was sent")));
                }
            }
        });
    }

    private static Mono<List<AuditLogPart>> getAuditLog(GuildCreateEvent event) {
        final var auditLogQuerySpec = AuditLogQuerySpec.builder()
                .actionType(ActionType.BOT_ADD)
                .limit(1)
                .build();
        return event.getGuild().getAuditLog(auditLogQuerySpec).collectList();
    }

    private static Mono<PrivateChannel> getPrivateChannel(AuditLogEntry auditLogEntry) {
        return auditLogEntry
                .getResponsibleUser()
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20087))
                .getPrivateChannel();
    }
}
