package discord;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigsLoader {

    @Autowired
    private ApplicationArguments args;

    @Bean
    public Configs configs() {
        var sourceArgs = args.getSourceArgs();
        if(sourceArgs.length != 1) {
            throw new IllegalArgumentException("Missing argument");
        }
        return Configs.buildConfigs(sourceArgs[0]);
    }
}
