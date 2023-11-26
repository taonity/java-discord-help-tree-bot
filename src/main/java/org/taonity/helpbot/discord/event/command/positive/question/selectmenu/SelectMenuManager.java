package org.taonity.helpbot.discord.event.command.positive.question.selectmenu;

import discord4j.common.util.Snowflake;
import discord4j.core.object.component.SelectMenu;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.taonity.helpbot.discord.event.command.positive.question.UserStatus;
import org.taonity.helpbot.discord.event.command.tree.TreeWalker;
import org.taonity.helpbot.discord.event.command.tree.model.IdentifiedLocalizedNodeText;
import org.taonity.helpbot.discord.event.command.tree.model.IdentifiedNodeText;
import org.taonity.helpbot.discord.event.command.tree.model.Node;
import org.taonity.helpbot.discord.localisation.Language;

@Getter
@Setter
public class SelectMenuManager {
    private final SelectMenuFactory selectMenuFactory = new SelectMenuFactory();
    private static final long TTL = 60 * 24;

    private boolean firstNodeProcessed = false;
    private UserStatus userStatus = UserStatus.USES_SELECT_MENU;

    private TreeWalker treeWalker;
    private long lastUpdate;
    private Language language;
    private String languageSelectMenuCustomId;
    private String treeSelectMenuCustomId;

    private final Snowflake userId;

    public SelectMenuManager(Snowflake userId, Node root) {
        this.userId = userId;
        treeWalker = new TreeWalker(root);

        updateLastUpdateTime();
    }

    public SelectMenu createLanguageSelectMenu() {
        final var selectMenu = selectMenuFactory.createLanguageSelectMenu();
        languageSelectMenuCustomId = selectMenu.getCustomId();
        return selectMenu;
    }

    public void updateLastUpdateTime() {
        lastUpdate = Instant.now().getEpochSecond();
    }

    public boolean isDead() {
        return Instant.now().getEpochSecond() > (lastUpdate + TTL);
    }

    public SelectMenu createFirstTreeSelectMenu() {
        final var localizedOptionsList = treeWalker.getIdentifiedLocalizedNodeTextList();

        return createTreeSelectMenuByLocalizedOptions(localizedOptionsList);
    }

    public SelectMenu createNextTreeSelectMenu(String nodeId) {
        final var localizedOptionsList = treeWalker.getNextIdentifiedLocalizedNodeTextListById(nodeId);

        return createTreeSelectMenuByLocalizedOptions(localizedOptionsList);
    }

    private SelectMenu createTreeSelectMenuByLocalizedOptions(List<IdentifiedLocalizedNodeText> localizedOptionsList) {
        final List<IdentifiedNodeText> optionsList = localizedOptionsList.stream()
                .map(localizedOption -> new IdentifiedNodeText(
                        localizedOption.getId(),
                        localizedOption.getLocalizedText().getTranslatedText(language)))
                .collect(Collectors.toList());
        var selectMenu = selectMenuFactory.createTreeSelectMenu(optionsList);
        treeSelectMenuCustomId = selectMenu.getCustomId();
        return selectMenu;
    }

    public boolean atLastQuestionInBranch() {
        return treeWalker.getCurrentNode().getChildText().get(0).getChildText() == null;
    }

    public String getTargetId() {
        return getAnswerNode().getTargetId();
    }

    public Node getAnswerNode() {
        return treeWalker.getCurrentNode().getChildText().get(0);
    }

    public String getTranslatedAnswerText() {
        return getAnswerNode().getLocalizedText().getTranslatedText(language);
    }
}
