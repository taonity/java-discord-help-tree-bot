package discord;

import discord.dao.GiteaNewUser;
import discord.repository.GuildSettingsRepository;
import discord.services.GiteaApiService;
import discord.services.MessageChannelService;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "discord")
@RequiredArgsConstructor
public class CmdRunner implements CommandLineRunner {

    @Setter
    private Map<String,String> githubUsers;

    private final GuildSettingsRepository guildSettingsRepository;

    private final MessageChannelService messageChannelService;

    private final GiteaApiService giteaApiService;


    @Override
    @Transactional
    public void run(String... args) throws Exception {

        RestTemplate restTemplate = new RestTemplate();

        /*String uri = "http://localhost:3000/api/v1/admin/users";
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization","token 051640a5e325a4bfe0caaab82c98370c0bc3cbc8");
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<?> result =
                restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
        System.out.println(result.getBody());*/

        System.out.println(giteaApiService.getUsers().get());
        System.out.println(giteaApiService.createUser(new GiteaNewUser("omg", "d", "d@d.d1")).get());

        System.out.println(guildSettingsRepository.findAll());
    }
}
