package discord.tree;

import com.fasterxml.jackson.annotation.JsonProperty;
import discord.localisation.Language;
import discord.localisation.LocalizedText;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Node implements Cloneable {
    private String id;
    private static int idCounter = 0;

    @JsonProperty("func")
    private NodeFunction nodeFunction;

    @JsonProperty("text")
    private LocalizedText localizedText;
    private ArrayList<Node> childText;
    private String targetId;


    public void identifyNodes() {
        id = Integer.toString(idCounter);
        idCounter++;
        if(childText != null) {
            for(Node node: childText) {
                node.identifyNodes();
            }
        }
    }

    public IdentifiedLocalizedNodeText getIdentifiedNodeLocalizedText() {
        return new IdentifiedLocalizedNodeText(id, localizedText);
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
