package discord.handler.command;

import discord.exception.EmptyOptionalException;
import discord.handler.EventPredicates;
import discord.localisation.LogMessage;
import discord.localisation.SimpleMessage;
import discord.services.GiteaUserService;
import discord.services.SelectMenuService;
import discord.structure.ChannelRole;
import discord.structure.CommandName;
import discord.structure.EmbedBuilder;
import discord.structure.EmbedType;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

import static discord.localisation.LocalizedMessage.CHOOSE_LANGUAGE_MESSAGE;
import static discord.structure.CommandName.DIALOG;

@Component
@RequiredArgsConstructor
public class DialogCommand extends AbstractSlashCommand {
    @Getter
    private final CommandName command = DIALOG;

    private final EventPredicates eventPredicates;
    private final GiteaUserService giteaUserService;

    @Value("${gitea.protocol}://${gitea.address}:${gitea.port}/user/login")
    private String loginUrl;

    @Value("${gitea.protocol}://${gitea.address}:${gitea.port}/%s/%s/src/branch/main/%s")
    private String dialogUrlFormat;

    private String buildDialogUrl(String guildId) {
        final var userName = String.format(GiteaUserService.USER_NAME_FORMAT, guildId);
        final var repoName = String.format(GiteaUserService.REPO_NAME_FORMAT, guildId);
        return String.format(dialogUrlFormat,userName, repoName, GiteaUserService.FILE_NAME);
    }

    @Override
    public boolean filter(ChatInputInteractionEvent event) {
        return Stream.of(event)
                .filter(eventPredicates::filterBot)
                .filter(this::filterByCommand)
                .filter(eventPredicates::filterByModeratorRole)
                .count() == 1;
    }

    @Override
    public void handle(ChatInputInteractionEvent event) {
        final var guildId = event.getInteraction().getGuildId()
                .map(Snowflake::asString)
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20037));

        final var userEditOption = giteaUserService.resetPassword(guildId);

        event.reply().withEmbeds(EmbedBuilder.buildSimpleMessage(
                SimpleMessage.GITEA_USER_CREDS_MESSAGE_FORMAT.format(
                        loginUrl,
                        buildDialogUrl(guildId),
                        userEditOption.getLoginName(),
                        userEditOption.getPassword()
                ),
                EmbedType.SIMPLE_MESSAGE_EMBED_TYPE
        )).withEphemeral(true).subscribe();
    }
}
