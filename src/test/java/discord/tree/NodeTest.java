package discord.tree;

import com.fasterxml.jackson.core.JsonProcessingException;
import discord.exception.YamlProcessingException;
import discord.utils.ResourceFileLoader;
import discord.utils.YamlStringToNodeConvertor;
import org.junit.jupiter.api.Test;

class NodeTest {

    @Test
    void asIdJsonString() throws YamlProcessingException {
        var dialogYaml = ResourceFileLoader.loadFile("dialog-starter.yaml");
        var node = YamlStringToNodeConvertor.convert(dialogYaml);
        node.identifyNodes();

        System.out.println(node.asIdJsonString());
    }
}
