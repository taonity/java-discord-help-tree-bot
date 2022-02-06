package discord.tree;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IdentifiedNodeText {
    private String id;
    private String text;
}
