package discord;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class SpringApp {

    public static void main(String[] args) {
        new SpringApplicationBuilder(SpringApp.class).build().run(args);
    }
}
