package discord.handler.command;

import static discord.localisation.LocalizedMessage.CHOOSE_LANGUAGE_MESSAGE;
import static discord.structure.CommandName.QUESTION;

import discord.exception.main.EmptyOptionalException;
import discord.handler.EventPredicates;
import discord.logging.LogMessage;
import discord.services.SelectMenuService;
import discord.structure.ChannelRole;
import discord.structure.CommandName;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuestionCommand extends AbstractSlashCommand {
    @Getter
    private final CommandName command = QUESTION;

    private final SelectMenuService selectMenuService;
    private final EventPredicates eventPredicates;

    @Override
    public boolean filter(ChatInputInteractionEvent event) {
        return Stream.of(event)
                        .filter(eventPredicates::filterBot)
                        .filter(this::filterByCommand)
                        .filter(eventPredicates::filterIfChannelsExistInSettings)
                        .filter(e -> eventPredicates.filterByChannelRole(e, ChannelRole.HELP))
                        .count()
                == 1;
    }

    @Override
    public void handle(ChatInputInteractionEvent event) {
        final var guildId = event.getInteraction()
                .getGuildId()
                .map(Snowflake::asString)
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20037));

        final var selectMenuManager = event.getInteraction()
                .getMember()
                .map(User::getId)
                .map(userId -> selectMenuService.initNewManager(userId, guildId))
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20010));

        event.reply(CHOOSE_LANGUAGE_MESSAGE.getMerged())
                .withComponents(ActionRow.of(selectMenuManager.createLanguageSelectMenu()))
                .subscribe();

        log.info(
                "Command {} successfully created first select menu from user {} in guild {}",
                command.getCommandName(),
                event.getInteraction()
                        .getMember()
                        .map(Member::getId)
                        .map(Snowflake::asString)
                        .orElse("NULL"),
                event.getInteraction().getGuildId().map(Snowflake::asString).orElse("NULL"));
    }
}
