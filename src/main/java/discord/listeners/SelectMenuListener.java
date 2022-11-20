package discord.listeners;

import discord.SelectMenuFactory;
import discord.utils.SelectMenuManager;
import discord.UserStatus;
import discord.commands.ChannelRole;
import discord.localisation.Language;
import discord.localisation.LocalizedFields;
import discord.services.MessageChannelService;
import discord.tree.Node;
import discord.tree.TreeRoot;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.SelectMenu;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SelectMenuListener implements DiscordEventListener<SelectMenuInteractionEvent> {
    public final List<SelectMenuManager> smManagers;
    public final TreeRoot treeRoot;
    public final GatewayDiscordClient client;
    public final MessageChannelService channelService;

    public Mono<Void> handle(SelectMenuInteractionEvent event) {
        // TODO: refactor here
        final var interactionAuthor = event.getInteraction().getMember();
        if(interactionAuthor.isEmpty()) {
            log.info("Error! interactionAuthor in SelectMenuInteractionEvent is empty.");
            return Mono.empty();
        }
        var activeMember = interactionAuthor.get();
        Snowflake authorId = activeMember.getId();

        SelectMenuManager smManager = smManagers.stream()
                .filter(manager -> authorId.equals(manager.getUserId()))
                .findAny()
                .orElse(null);
        if(smManager == null) {
            return Mono.empty();
        }

        String value = event
                .getValues()
                .toString().replace("[", "").replace("]", "");

        if(event.getCustomId().equals(smManager.getLanguageSelectMenu().getCustomId())) {
            switch (value) {
                case "English":
                    smManager.setLanguage(Language.EN);
                    break;
                case "Русский":
                    smManager.setLanguage(Language.RU);
                    break;
                default:
                    throw new IllegalArgumentException("Undefined language in SelectMenu");
            }
            SelectMenu selectMenu = smManager.createNextSelectMenu(null);
            channelService.getChannel(ChannelRole.HELP).createMessage(
                    LocalizedFields.get("lets", smManager.getLanguage())
            ).withComponents(ActionRow.of(selectMenu)).subscribe();
            smManager.updateLastUpdateTime();

            return event.edit().withComponents(ActionRow.of(SelectMenuFactory.disableAndSetDefault(smManager.getLanguageSelectMenu(), value)));
        } else
        if(event.getCustomId().equals(smManager
                .getCurrentTreeSelectMenu()
                .getCustomId())) {

            SelectMenu selectMenu = smManager.createNextSelectMenu(value);

            String selectedLabel = smManager.getTreeWalker()
                    .getCurrentNode()
                    .getLocalizedText()
                    .getTranslatedText(smManager.getLanguage());

            if(smManager.getTreeWalker().getCurrentNode().getChildText().get(0).getChildText() == null) {
                Node answerNode = smManager.getTreeWalker().getCurrentNode().getChildText().get(0);
                String translatedAnswer = answerNode.getLocalizedText()
                        .getTranslatedText(smManager.getLanguage());
                channelService.getChannel(ChannelRole.HELP).createMessage(translatedAnswer).subscribe();
                switch (answerNode.getNodeFunction()) {
                    case ASK_INPUT:
                        smManager.setUserStatus(UserStatus.WRITES_MESSAGE);
                        smManager.updateLastUpdateTime();
                        break;
                    case RETURN_TEXT:
                        smManagers.remove(smManager);
                        break;
                    default:
                        return Mono.empty();
                }
            } else {
                log.info("Test: clarification message. language: " + smManager.getLanguage().toString());
                channelService.getChannel(ChannelRole.HELP).createMessage(
                        LocalizedFields.get("clar", smManager.getLanguage())
                ).withComponents(ActionRow.of(selectMenu)).subscribe();
                log.info("Test: clarification message. created");
                smManager.updateLastUpdateTime();
            }

            return event.edit().withComponents(
                    ActionRow.of(SelectMenuFactory.disableAndSetDefault(
                            smManager.getCurrentTreeSelectMenu(), selectedLabel))
            );
        } else {
            return Mono.empty();
        }
    }
}
