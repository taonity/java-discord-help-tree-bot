package discord.commands;

import discord.utils.SelectMenuManager;
import discord.model.GuildSettings;
import discord.tree.TreeRoot;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.component.ActionRow;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

import static discord.commands.CommandName.QUESTION;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuestionCommand extends SlashCommand {
    @Getter
    private final CommandName command = QUESTION;

    public final List<SelectMenuManager> smManagers;
    public final TreeRoot treeRoot;
    public final GuildSettings guildSettings;

    @Override
    public boolean filter(ChatInputInteractionEvent event) {
        return filterByCommand(event) &&
                filterByChannelId(event, guildSettings.getHelpChannelId());
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        final var interactionAuthor = event.getInteraction().getMember();
        if(interactionAuthor.isEmpty()) {
            log.info("Error! interactionAuthor in ApplicationCommandInteractionEvent is empty.");
            return Mono.empty();
        }
        Snowflake authorId = interactionAuthor.get().getId();
        smManagers.removeIf(SelectMenuManager::isDead);
        smManagers.removeIf(manager -> authorId.equals(manager.getUserId()));
        var selectMenuManager = new SelectMenuManager(treeRoot, authorId);
        smManagers.add(selectMenuManager);
        return event.reply("Выбери язык. Choose language.").withComponents(
                ActionRow.of(selectMenuManager.getLanguageSelectMenu())
        );
    }
}
