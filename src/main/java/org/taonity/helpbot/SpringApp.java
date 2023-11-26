package org.taonity.helpbot;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.web.reactive.config.EnableWebFlux;

@EnableWebFlux
@SpringBootApplication
public class SpringApp {

    public static void main(String[] args) {
        new SpringApplicationBuilder(SpringApp.class).build().run(args);
    }
}
