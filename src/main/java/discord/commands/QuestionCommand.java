package discord.commands;

import discord.SelectMenuManager;
import discord.tree.TreeRoot;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.component.ActionRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class QuestionCommand implements SlashCommand {
    private static final Logger log = LoggerFactory.getLogger(UpdateCommand.class);

    public List<SelectMenuManager> smManagers;
    public TreeRoot treeRoot;

    public QuestionCommand(List<SelectMenuManager> smManagers, TreeRoot treeRoot) {
        this.smManagers = smManagers;
        this.treeRoot = treeRoot;
    }

    @Override
    public String getName() {
        return "question";
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
        var selectMenuManager = new SelectMenuManager(authorId, treeRoot);
        smManagers.add(selectMenuManager);
        return event.reply("Выбери язык. Choose language.").withComponents(
                ActionRow.of(selectMenuManager.getLanguageSelectMenu())
        );
    }
}
