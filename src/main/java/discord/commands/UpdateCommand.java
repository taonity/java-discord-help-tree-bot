package discord.commands;

import discord.Configs;
import discord.tree.TreeRoot;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class UpdateCommand implements SlashCommand {

    private static final Logger log = LoggerFactory.getLogger(UpdateCommand.class);

    TreeRoot treeRoot;
    Configs configs;

    public UpdateCommand(TreeRoot treeRoot, Configs configs) {
        this.treeRoot = treeRoot;
        this.configs = configs;
    }

    @Override
    public String getName() {
        return "update";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        final var interactionAuthor = event.getInteraction().getMember();
        if(interactionAuthor.isEmpty()) {
            log.info("Error! interactionAuthor in ApplicationCommandInteractionEvent is empty.");
            return Mono.empty();
        }
        if(!configs.getUserWhiteList().contains(interactionAuthor.get().getId().asLong())) {
            return event.reply("You can not use this command.")
                    .withEphemeral(true);
        }
        var errorMessage = treeRoot.verifyFile(event.getClient());
        if(errorMessage == null) {
            return event.reply("Success");
        } else {
            return event.reply("```"+errorMessage+"```");
        }
    }
}
