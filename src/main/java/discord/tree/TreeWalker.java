package discord.tree;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class TreeWalker {

    @Getter
    private Node currentNode;

    public List<IdentifiedLocalizedNodeText> getIdentifiedLocalizedNodeTextList() {
        return currentNode.getListByChildNode();
    }

    public List<IdentifiedLocalizedNodeText> getNextIdentifiedLocalizedNodeTextListById(String id) {
        if (currentNode.getChildText().get(0).getChildText() == null) {
            return null;
        }

        List<IdentifiedLocalizedNodeText> list = null;

        for (Node node : currentNode.getChildText()) {
            if (node.getId().equals(id)) {
                list = node.getListByChildNode();
                currentNode = node;
                break;
            }
        }

        return list;
    }
}
