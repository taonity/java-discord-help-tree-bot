package org.taonity.helpbot.discord.event.command.gitea;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.taonity.helpbot.discord.event.command.tree.model.Node;
import org.taonity.helpbot.discord.logging.LogMessage;
import org.taonity.helpbot.discord.logging.exception.YamlProcessingException;

public class YamlStringToNodeConvertor {
    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory()) {
        {
            findAndRegisterModules();
        }
    };

    public static Node convert(String yamlString) throws YamlProcessingException {
        try {
            return mapper.readValue(yamlString, Node.class);
        } catch (JsonProcessingException e) {
            throw new YamlProcessingException(LogMessage.ALERT_20016, e.getMessage());
        }
    }
}
