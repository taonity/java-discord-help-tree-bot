package discord.tree;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import discord.localisation.LocalizedText;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.*;

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
        if (childText != null) {
            for (Node node : childText) {
                node.identifyNodes();
            }
        }
    }

    public IdentifiedLocalizedNodeText getIdentifiedNodeLocalizedText() {
        return new IdentifiedLocalizedNodeText(id, localizedText);
    }

    public List<IdentifiedLocalizedNodeText> getListByChildNode() {
        return this.getChildText().stream()
                .map(Node::getIdentifiedNodeLocalizedText)
                .collect(Collectors.toList());
    }

    private Map<String, Object> toMap() {
        final var map = new HashMap<String, Object>();
        map.put("id", id);
        final var childNodesMap = new ArrayList<>();
        if (childText != null) {
            for (Node child : childText) {
                childNodesMap.add(child.toMap());
            }
        }
        map.put("cn", childNodesMap);
        return map;
    }

    public String asIdJsonString() {
        final var objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(toMap());
        } catch (JsonProcessingException e) {
            return "NULL";
        }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        Node helpTreeNode;
        helpTreeNode = (Node) super.clone();
        if (helpTreeNode.childText != null) {
            helpTreeNode.childText = new ArrayList<>();
            helpTreeNode.childText.addAll(this.childText.stream()
                    .map(child -> {
                        try {
                            return (Node) child.clone();
                        } catch (CloneNotSupportedException e) {
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .collect(Collectors.toList()));
        }
        return helpTreeNode;
    }
}
