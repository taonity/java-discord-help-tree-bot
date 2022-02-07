package discord;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.rest.http.client.ClientException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConfigsManager {
    private Configs configs;
    private final String pathname;

    public ConfigsManager(String pathname) {
        try {
            this.pathname = pathname;
            var mapper = new ObjectMapper(new YAMLFactory());
            mapper.findAndRegisterModules();
            var file  = new File(pathname);
            configs = mapper.readValue(file, Configs.class);
        } catch (IOException error) {
            System.out.println(error.getMessage());
            throw new IllegalArgumentException("config file not found");
        }
    }

    public Configs getConfigs() {
        return configs;
    }

    public void setConfigs(Configs configs) {
        this.configs = configs;
    }

    public String getPathname() {
        return pathname;
    }
}
