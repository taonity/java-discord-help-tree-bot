package discord.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import discord.exception.TreeRootValidationException;
import discord.exception.YamlProcessingException;
import discord.localisation.LogMessage;
import discord.services.MessageChannelService;
import discord.tree.Node;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;


public class YamlStringToNodeConvertor {
    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory()) {{
        findAndRegisterModules();
    }};

    public static Node convert(String yamlString) throws YamlProcessingException {
        try {
            return mapper.readValue(yamlString, Node.class);
        } catch (JsonProcessingException e) {
            throw new YamlProcessingException(LogMessage.ALERT_20016, e.getMessage());
        }
    }

}
