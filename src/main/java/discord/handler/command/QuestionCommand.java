package discord.handler.command;

import discord.exception.EmptyOptionalException;
import discord.handler.EventPredicates;
import discord.localisation.LogMessage;
import discord.services.MessageChannelService;
import discord.services.SelectMenuService;
import discord.structure.CommandName;
import discord.model.GuildSettings;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.component.ActionRow;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

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
    private final GuildSettings guildSettings;
    private final EventPredicates eventPredicates;
    private final MessageChannelService messageChannelService;

    @Override
    public boolean filter(ChatInputInteractionEvent event) {
        return Stream.of(event)
                .filter(this::filterByCommand)
                .filter(e -> eventPredicates.filterByChannelId(e, guildSettings.getHelpChannelId()))
                .count() == 1;
    }

    @Override
    public void handle(ChatInputInteractionEvent event) {
        final var interactionAuthor = event.getInteraction().getMember();
        if(interactionAuthor.isEmpty()) {
            throw new EmptyOptionalException(LogMessage.ALERT_20010, messageChannelService);
        }
        final Snowflake authorId = interactionAuthor.get().getId();
        var selectMenuManager = selectMenuService.initNewManager(authorId);

        event.reply(CHOOSE_LANGUAGE_MESSAGE.getMerged()).withComponents(
                ActionRow.of(selectMenuManager.createLanguageSelectMenu())
        ).subscribe();
    }
}
