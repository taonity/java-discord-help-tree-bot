package discord;

import discord4j.core.object.component.SelectMenu;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class SelectMenuFactory {

    public SelectMenu createLanguageSelectMenu() {
        return SelectMenu.of(generateComponentId(),
                SelectMenu.Option.of("English", "English"),
                SelectMenu.Option.of("Русский", "Русский")
        );
    }

    public SelectMenu createTreeSelectMenu(List<IdentifiedNodeText> options) {
        var selectMenuOptionList= options
                .stream()
                .map(option -> SelectMenu.Option.of(
                        option.getText(),
                        option.getId()
                ))
                .collect(Collectors.toList());
        return SelectMenu.of(generateComponentId(), selectMenuOptionList);
    }

    /*public static SelectMenu disableAndSetDefault(SelectMenu selectMenu, String defaultOption) {
        var selectMenuOptionList = selectMenu.getOptions();
        selectMenuOptionList.add(SelectMenu.Option.ofDefault(defaultOption, "null"));
        return SelectMenu.of(selectMenu.getCustomId(), selectMenuOptionList).disabled();
    }*/
    public static SelectMenu disableAndSetDefault(SelectMenu selectMenu, String defaultOption) {
        return SelectMenu.of(selectMenu.getCustomId(),
                SelectMenu.Option.ofDefault(defaultOption, "null")).disabled();
    }

    private String generateComponentId() {
        return Long.toString(Instant.now().toEpochMilli());
    }

}
