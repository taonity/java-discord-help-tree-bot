package discord;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;

public class YamlLoader<T> {
    public static <T> T loadFromFile(String pathName, Class<T> className) {
        try {
            var mapper = new ObjectMapper(new YAMLFactory());
            mapper.findAndRegisterModules();
            var file  = new File(pathName);
            return mapper.readValue(file, className);
        } catch (IOException error) {
            System.out.println(error.getMessage());
            throw new IllegalArgumentException("Can't load " + className + " object by path: " + pathName);
        }
    }
}
