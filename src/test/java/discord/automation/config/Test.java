package discord.automation.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Test implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        System.out.println("===================");
    }
}
