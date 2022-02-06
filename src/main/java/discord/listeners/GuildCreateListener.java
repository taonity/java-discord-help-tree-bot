package discord.listeners;

import discord.exception.main.EmptyOptionalException;
import discord.logging.LogMessage;
import discord.repository.GuildSettingsRepository;
import discord.services.GuildDataService;
import discord.services.MessageChannelService;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.object.audit.ActionType;
import discord4j.core.spec.AuditLogQuerySpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static discord.localisation.SimpleMessage.ON_GUILD_JOIN_INSTRUCTIONS;
import static java.util.Objects.isNull;

@Slf4j
@Component
@RequiredArgsConstructor
public class GuildCreateListener implements DiscordEventListener<GuildCreateEvent> {

    private final GuildSettingsRepository guildSettingsRepository;
    private final GuildDataService guildDataService;
    private final MessageChannelService messageChannelService;

    @Override
    @Transactional
    public void handle(GuildCreateEvent event) {
        final var guildId = event.getGuild().getId().asString();
        final var guildIsNotPresent =
                guildSettingsRepository.findGuildSettingByGuildId(guildId).isEmpty();

        if (guildIsNotPresent) {
            guildDataService.removeIfLeftInDiscord();
            guildDataService.create(guildId);
            sendInstructionMessage(event, guildId);


            log.info("New guild {} was initialised", guildId);
        } else {
            // means that bot didn't really join the guild
            log.info("Existing guild {} was initialised", guildId);
        }
    }

    private static void sendInstructionMessage(GuildCreateEvent event, String guildId) {
        final var auditLogQuerySpec = AuditLogQuerySpec.builder()
                .actionType(ActionType.BOT_ADD)
                .limit(1)
                .build();
        final var botAddedAuditLog = event.getGuild()
                .getAuditLog(auditLogQuerySpec)
                .collectList()
                .blockOptional()
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20084))
                .get(0);

        if (isNull(botAddedAuditLog)) {
            throw new EmptyOptionalException(LogMessage.ALERT_20085);
        } else {
            final var auditLogEntry = botAddedAuditLog.getEntries().get(0);
            if (isNull(auditLogEntry)) {
                throw new EmptyOptionalException(LogMessage.ALERT_20086);
            } else {
                auditLogEntry.getResponsibleUser()
                        .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20087))
                        .getPrivateChannel()
                        .blockOptional()
                        .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20088))
                        .createMessage(ON_GUILD_JOIN_INSTRUCTIONS.getMessage())
                        .subscribe();
                log.info("Instructions for new guild {} was sent", guildId);
            }
        }
    }
}
