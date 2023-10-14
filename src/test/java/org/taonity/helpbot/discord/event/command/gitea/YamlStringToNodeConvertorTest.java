package org.taonity.helpbot.discord.event.command.gitea;

import org.junit.jupiter.api.Test;
import org.taonity.helpbot.discord.logging.exception.YamlProcessingException;

class NodeTest {

    @Test
    void asIdJsonString() throws YamlProcessingException {
        var dialogYaml = ResourceFileLoader.loadFile("dialog-starter.yaml");
        var node = YamlStringToNodeConvertor.convert(dialogYaml);
        node.identifyNodes();

        System.out.println(node.asIdJsonString());
    }
}
