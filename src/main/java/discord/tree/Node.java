package discord.tree;

import com.fasterxml.jackson.annotation.JsonProperty;
import discord.localisation.LocalizedText;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Node implements Cloneable {
    private String id;
    private static int idCounter = 0;

    @JsonProperty("func")
    private NodeFunction nodeFunction;

    @JsonProperty("text")
    private LocalizedText localizedText;
    private ArrayList<Node> childText;
    private String targetId;

    public Node() {
    }

    public Node(NodeFunction nodeFunction, LocalizedText localizedText, ArrayList<Node> childText, String targetId) {
        this.nodeFunction = nodeFunction;
        this.localizedText = localizedText;
        this.childText = childText;
        this.targetId = targetId;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public NodeFunction getNodeFunction() {
        return nodeFunction;
    }

    public void setNodeFunction(NodeFunction nodeFunction) {
        this.nodeFunction = nodeFunction;
    }

    public void identifyNodes() {
        id = Integer.toString(idCounter);
        idCounter++;
        if(childText != null) {
            for(Node node: childText) {
                node.identifyNodes();
            }
        }
    }

    public LocalizedText getLocalizedText() {
        return localizedText;
    }

    public String getId() {
        return id;
    }

    public ArrayList<Node> getChildText() {
        return childText;
    }

    public IdentifiedLocalizedNodeText getIdentifiedNodeLocalizedText() {
        return new IdentifiedLocalizedNodeText(id, localizedText);
    }

    public void setChildText(ArrayList<Node> childText) {
        this.childText = childText;
    }

    public List<IdentifiedLocalizedNodeText> getListByChildNode() {
        return this.getChildText()
                .stream()
                .map(Node::getIdentifiedNodeLocalizedText)
                .collect(Collectors.toList());
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        Node helpTreeNode = null;
        helpTreeNode = (Node) super.clone();
        if(helpTreeNode.childText != null) {
            helpTreeNode.childText = new ArrayList<>();
            helpTreeNode.childText.addAll(
                    this.childText
                    .stream()
                    .map(child -> {
                        try {
                            return (Node) child.clone();
                        } catch (CloneNotSupportedException e) {
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .collect(Collectors.toList())
            );
        }
        return helpTreeNode;
    }
}
