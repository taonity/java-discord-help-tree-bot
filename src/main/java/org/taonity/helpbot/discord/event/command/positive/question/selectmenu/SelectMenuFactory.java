package org.taonity.helpbot.discord.event.command.positive.question.selectmenu;

import discord4j.core.object.component.SelectMenu;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.taonity.helpbot.discord.event.command.tree.model.IdentifiedNodeText;

public class SelectMenuFactory {

    public SelectMenu createLanguageSelectMenu() {
        return SelectMenu.of(
                generateComponentId(),
                SelectMenu.Option.of("English", "English"),
                SelectMenu.Option.of("Русский", "Русский"));
    }

    public SelectMenu createTreeSelectMenu(List<IdentifiedNodeText> options) {
        var selectMenuOptionList = options.stream()
                .map(option -> SelectMenu.Option.of(option.getText(), option.getId()))
                .collect(Collectors.toList());
        return SelectMenu.of(generateComponentId(), selectMenuOptionList);
    }

    private String generateComponentId() {
        return Long.toString(Instant.now().toEpochMilli());
    }
}
