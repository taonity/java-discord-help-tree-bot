package org.taonity.helpbot.discord.event.command.positive.question;

import static org.taonity.helpbot.discord.localisation.LocalizedMessage.CHOOSE_LANGUAGE_MESSAGE;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.entity.User;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.taonity.helpbot.discord.ChannelRole;
import org.taonity.helpbot.discord.CommandName;
import org.taonity.helpbot.discord.event.command.AbstractPositiveSlashCommand;
import org.taonity.helpbot.discord.event.command.EventPredicates;
import org.taonity.helpbot.discord.event.command.positive.question.selectmenu.SelectMenuService;
import org.taonity.helpbot.discord.logging.LogMessage;
import org.taonity.helpbot.discord.logging.exception.main.EmptyOptionalException;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuestionCommand extends AbstractPositiveSlashCommand {
    @Getter
    private final CommandName command = CommandName.QUESTION;

    private final SelectMenuService selectMenuService;
    private final EventPredicates eventPredicates;

    @Override
    public final List<Predicate<ChatInputInteractionEvent>> getFilterPredicates() {
        return Arrays.asList(
                eventPredicates::filterBot,
                this::filterByCommand,
                eventPredicates::filterIfChannelsExistInSettings,
                e -> eventPredicates.filterByChannelRole(e, ChannelRole.HELP)
        );
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

        log.info("Command successfully created first select menu");
    }
}
