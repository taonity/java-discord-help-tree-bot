package discord.tree;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import discord.Configs;
import discord.YamlLoader;
import discord.localisation.Language;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.rest.http.client.ClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class TreeRoot {
    private Node root;

    private final String pathName;

    @Autowired
    public TreeRoot(Configs configs) {
        /*try {
            this.pathname = pathname;
            var mapper = new ObjectMapper(new YAMLFactory());
            mapper.findAndRegisterModules();
            var file  = new File(pathname);
            root = mapper.readValue(file, Node.class);

        } catch (IOException error) {
            System.out.println(error.getMessage());
            throw new IllegalArgumentException("help_tree file not found");
        }*/
        pathName = configs.getTreePath();
        root = YamlLoader.loadFromFile(pathName, Node.class);
        root.identifyNodes();
    }

    public static void collectTargetIds(Node node, List<String> targetIds) {
        String targetId = node.getTargetId();
        if(targetId != null) {
            targetIds.add(targetId);
        }
        if(node.getChildText() != null) {
            for(Node childText: node.getChildText()) {
                collectTargetIds(childText, targetIds);
            }
        }
    }

    public static void collectLabels(Node node, List<String> labels) {
        if(node.getChildText() != null) {
            if(node.getLocalizedText() != null) {
                labels.add(node.getLocalizedText().getTranslatedText(Language.EN));
                labels.add(node.getLocalizedText().getTranslatedText(Language.RU));
            }
            for(Node childText: node.getChildText()) {
                collectLabels(childText, labels);
            }
        }
    }

    public String verifyFile(GatewayDiscordClient gateway) {
        /*var mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        var file  = new File(pathName);
        root = mapper.readValue(file, Node.class);
        root.identifyNodes();*/
        root = YamlLoader.loadFromFile(pathName, Node.class);
        root.identifyNodes();
        StringBuilder wrongTargetIdErrorBuilder = new StringBuilder();

        var labels = new ArrayList<String>();
        collectLabels(root, labels);
        for(var label: labels) {

            if(label.length() >= 100) {
                wrongTargetIdErrorBuilder.append("Label ").append(label).append(" is too big\n");
            }
        }

        List<String> targetIds = new ArrayList<>();
        collectTargetIds(root, targetIds);
        for(String targetId: targetIds) {
            Snowflake targetSnowflake;
            try {
                targetSnowflake = Snowflake.of(targetId);
            } catch (NumberFormatException error) {
                wrongTargetIdErrorBuilder.append("Target ").append(targetId).append(" is wrong\n");
                continue;
            }
            try {
                gateway.getUserById(targetSnowflake).block();
            } catch (ClientException error) {
                wrongTargetIdErrorBuilder.append("Target ").append(targetId).append(" is wrong\n");
            }
        }
        if(wrongTargetIdErrorBuilder.length() != 0) {
            return wrongTargetIdErrorBuilder.toString();
        }
        return null;
    }

    public Node getRoot() {
        return root;
    }
}
