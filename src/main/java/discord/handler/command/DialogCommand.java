package discord.handler.command;

import static discord.structure.CommandName.DIALOG;

import discord.dto.gitea.api.EditUserOption;
import discord.exception.GiteaApiException;
import discord.exception.client.CorruptGiteaUserException;
import discord.exception.main.EmptyOptionalException;
import discord.handler.EventPredicates;
import discord.localisation.SimpleMessage;
import discord.logging.LogMessage;
import discord.model.GuildSettings;
import discord.repository.GuildSettingsRepository;
import discord.services.GiteaUserService;
import discord.structure.CommandName;
import discord.structure.EmbedBuilder;
import discord.structure.EmbedType;
import discord.utils.AlphaNumericGenerator;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.Member;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DialogCommand extends AbstractSlashCommand {
    @Getter
    private final CommandName command = DIALOG;

    private final EventPredicates eventPredicates;
    private final GiteaUserService giteaUserService;
    private final GuildSettingsRepository guildSettingsRepository;

    @Value("${gitea.public.url}/user/login")
    private String loginUrl;

    @Value("${gitea.public.url}/%s/%s/src/branch/main/%s")
    private String dialogUrlFormat;

    private String buildDialogUrl(String guildId) {
        final var giteaUserAlphaNumeric = guildSettingsRepository
                .findGuildSettingByGuildId(guildId)
                .map(GuildSettings::getId)
                .map(AlphaNumericGenerator::generateFourCharFromNumber)
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20070));

        final var userName = String.format(GiteaUserService.USER_NAME_FORMAT, giteaUserAlphaNumeric);
        final var repoName = String.format(GiteaUserService.REPO_NAME_FORMAT, giteaUserAlphaNumeric);
        return String.format(dialogUrlFormat, userName, repoName, GiteaUserService.FILE_NAME);
    }

    @Override
    public boolean filter(ChatInputInteractionEvent event) {
        return Stream.of(event)
                        .filter(eventPredicates::filterBot)
                        .filter(this::filterByCommand)
                        .filter(eventPredicates::filterIfChannelsExistInSettings)
                        .filter(eventPredicates::filterByModeratorRole)
                        .count()
                == 1;
    }

    @Override
    public void handle(ChatInputInteractionEvent event) {
        final var guildId = event.getInteraction()
                .getGuildId()
                .map(Snowflake::asString)
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20037));

        final EditUserOption userEditOption;
        try {
            userEditOption = giteaUserService.resetPassword(guildId);
        } catch (GiteaApiException e) {
            throw new CorruptGiteaUserException(LogMessage.ALERT_20053, guildId, e);
        }

        event.reply()
                .withEmbeds(EmbedBuilder.buildSimpleMessage(
                        SimpleMessage.GITEA_USER_CREDS_MESSAGE_FORMAT.format(
                                loginUrl,
                                buildDialogUrl(guildId),
                                userEditOption.getLoginName(),
                                userEditOption.getPassword()),
                        EmbedType.SIMPLE_MESSAGE_EMBED_TYPE))
                .withEphemeral(true)
                .subscribe();

        log.info(
                "Command {} successfully executed by user {} in guild {}",
                command.getCommandName(),
                event.getInteraction()
                        .getMember()
                        .map(Member::getId)
                        .map(Snowflake::asString)
                        .orElse("NULL"),
                event.getInteraction().getGuildId().map(Snowflake::asString).orElse("NULL"));
    }
}
