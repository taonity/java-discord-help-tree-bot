package discord.utils;

import discord.SelectMenuFactory;
import discord.UserStatus;
import discord.localisation.Language;
import discord.tree.IdentifiedLocalizedNodeText;
import discord.tree.IdentifiedNodeText;
import discord.tree.TreeWalker;
import discord.tree.TreeRoot;
import discord4j.common.util.Snowflake;
import discord4j.core.object.component.SelectMenu;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@RequiredArgsConstructor
public class SelectMenuManager {
    private final SelectMenuFactory selectMenuFactory = new SelectMenuFactory();
    private final static long TTL = 60*24;

    private boolean firstNodeProcessed = false;
    private UserStatus userStatus = UserStatus.USES_SELECT_MENU;

    private TreeWalker treeWalker;
    private SelectMenu currentTreeSelectMenu;
    private long lastUpdate;
    private Language language;
    private SelectMenu languageSelectMenu;

    private final TreeRoot treeRoot;
    private final Snowflake userId;

    @PostConstruct
    private void postConstruct() {
        treeWalker = new TreeWalker(treeRoot);

        updateLastUpdateTime();
        languageSelectMenu = selectMenuFactory.createLanguageSelectMenu();
    }

    public void updateLastUpdateTime() {
        lastUpdate = Instant.now().getEpochSecond();
    }

    public boolean isDead() {
        return Instant.now().getEpochSecond() > (lastUpdate + TTL);
    }

    public SelectMenu createNextSelectMenu(String nodeId) {
        List<IdentifiedLocalizedNodeText> localizedOptionsList;
        if(firstNodeProcessed) {
            localizedOptionsList = treeWalker.getNextIdentifiedLocalizedNodeTextListById(nodeId);
        } else {
            localizedOptionsList = treeWalker.getIdentifiedLocalizedNodeTextList();
            firstNodeProcessed = true;
        }

        List<IdentifiedNodeText> optionsList = localizedOptionsList.stream()
                .map(localizedOption -> new IdentifiedNodeText(
                        localizedOption.getId(),
                        localizedOption.getLocalizedText().getTranslatedText(language)
                ))
                .collect(Collectors.toList());
        currentTreeSelectMenu = selectMenuFactory.createTreeSelectMenu(optionsList);
        return currentTreeSelectMenu;
    }
}
