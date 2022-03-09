package discord.tree;

import java.util.List;

public class TreeManager {
    private final TreeRoot treeRoot;

    private Node currentNode;

    public TreeManager(TreeRoot treeRoot) {
        this.treeRoot = treeRoot;
        currentNode = treeRoot.getRoot();
    }

    public TreeRoot getTreeRoot() {
        return treeRoot;
    }

    public List<IdentifiedLocalizedNodeText> IdentifiedLocalizedNodeTextList() {
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

    public Node getCurrentNode() {
        return currentNode;
    }

}
