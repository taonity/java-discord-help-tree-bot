package discord;

import discord.localisation.Language;
import discord.tree.IdentifiedLocalizedNodeText;
import discord.tree.IdentifiedNodeText;
import discord.tree.TreeManager;
import discord.tree.TreeRoot;
import discord4j.common.util.Snowflake;
import discord4j.core.object.component.SelectMenu;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class SelectMenuManager {
    private SelectMenuFactory selectMenuFactory;
    private TreeRoot treeRoot;
    private TreeManager currentTree;
    private boolean firstNodeProcessed = false;
    private SelectMenu currentTreeSelectMenu;
    private Snowflake userId;
    private final long ttl = 60*24;
    private long lastUpdate;
    private Language language;
    private SelectMenu languageSelectMenu;
    private UserStatus userStatus = UserStatus.USES_SELECT_MENU;



    public SelectMenuManager(Snowflake userId, TreeRoot treeRoot) {
        selectMenuFactory = new SelectMenuFactory();
        this.treeRoot = treeRoot;
        currentTree = new TreeManager(treeRoot);
        this.userId = userId;
        updateLastUpdateTime();
        languageSelectMenu = selectMenuFactory.createLanguageSelectMenu();
    }

    public void updateLastUpdateTime() {
        lastUpdate = Instant.now().getEpochSecond();
    }

    public boolean isDead() {
        return Instant.now().getEpochSecond() > (lastUpdate + ttl);
    }

    public SelectMenu createNextSelectMenu(String nodeId) {
        List<IdentifiedLocalizedNodeText> localizedOptionsList;
        if(firstNodeProcessed) {
            localizedOptionsList = currentTree.getNextIdentifiedLocalizedNodeTextListById(nodeId);
        } else {
            localizedOptionsList = currentTree.IdentifiedLocalizedNodeTextList();
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

    public SelectMenu getCurrentTreeSelectMenu() {
        return currentTreeSelectMenu;
    }

    public Snowflake getUserId() {
        return userId;
    }

    public TreeManager getCurrentTree() {
        return currentTree;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public SelectMenu getLanguageSelectMenu() {
        return languageSelectMenu;
    }

    public void setLanguageSelectMenu(SelectMenu languageSelectMenu) {
        this.languageSelectMenu = languageSelectMenu;
    }

    public UserStatus getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(UserStatus userStatus) {
        this.userStatus = userStatus;
    }
}
