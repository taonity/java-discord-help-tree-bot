package discord.tree;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

public class TreeWalker {
    private final TreeRoot treeRoot;

    @Getter
    private Node currentNode;

    public TreeWalker(TreeRoot treeRoot) {
        this.treeRoot = treeRoot;
        postConstruct();
    }

    private void postConstruct() {
        currentNode = treeRoot.getRoot();
    }

    public List<IdentifiedLocalizedNodeText> getIdentifiedLocalizedNodeTextList() {
        return currentNode.getListByChildNode();
    }

    public List<IdentifiedLocalizedNodeText> getNextIdentifiedLocalizedNodeTextListById(String id) {
        if(currentNode.getChildText().get(0).getChildText() == null) {
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

    public void reset() {
        currentNode = treeRoot.getRoot();
    }
}
