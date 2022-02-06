package discord.tree;

import discord.localisation.LocalizedText;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IdentifiedLocalizedNodeText {
    private String id;
    private LocalizedText localizedText;
}
