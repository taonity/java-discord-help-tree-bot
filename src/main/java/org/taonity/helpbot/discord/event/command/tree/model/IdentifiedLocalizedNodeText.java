package org.taonity.helpbot.discord.event.command.tree.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.taonity.helpbot.discord.localisation.LocalizedText;

@Data
@AllArgsConstructor
public class IdentifiedLocalizedNodeText {
    private String id;
    private LocalizedText localizedText;
}
