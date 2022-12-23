package discord.handler.command;

import discord.exception.EmptyOptionalException;
import discord.exception.FailedToSearchRepoException;
import discord.handler.EventPredicates;
import discord.localisation.LogMessage;
import discord.services.SelectMenuService;
import discord.structure.ChannelRole;
import discord.structure.CommandName;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

import static discord.structure.CommandName.QUESTION;
import static discord.localisation.LocalizedMessage.CHOOSE_LANGUAGE_MESSAGE;

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
                .filter(e -> eventPredicates.filterByChannelRole(e, ChannelRole.HELP))
                .count() == 1;
    }

    @Override
    public void handle(ChatInputInteractionEvent event) {
        final var guildId = event.getInteraction().getGuildId()
                .map(Snowflake::asString)
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20037));

        final var selectMenuManager = event.getInteraction().getMember()
                .map(User::getId)
                .map(userId -> selectMenuService.initNewManager(userId, guildId))
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20010));

        event.reply(CHOOSE_LANGUAGE_MESSAGE.getMerged()).withComponents(
                ActionRow.of(selectMenuManager.createLanguageSelectMenu())
        ).subscribe();

    }
}
