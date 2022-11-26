package discord.services;

import discord.dao.GiteaExistingUser;
import discord.dao.GiteaNewUser;
import discord.localisation.LogMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.isNull;

@SuppressWarnings("unchecked")
@Slf4j
@Component
public class GiteaApiService {
    private final static String ADMIN_USERS_PATH = "/admin/users";

    @Value("${gitea.protocol}://${gitea.address}:${gitea.port}/api/v1/")
    private String baseUrl;

    @Value("token ${gitea.token}")
    private String token;

    private final RestTemplate restTemplate = new RestTemplate();

    private final HttpHeaders headers = new HttpHeaders();

    @PostConstruct
    private void postConstruct() {
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", token);
    }

    @FunctionalInterface
    public interface FourParameterFunction<T> {
        ResponseEntity<?> apply(String t, HttpMethod u, HttpEntity<?>  v, T w);
    }

    private Optional<?> sendRequest(FourParameterFunction<?> exchange, LogMessage onNull, LogMessage onException) {
        try {
            final var result = exchange.apply();
            if(isNull(result.getBody())) {
                log.warn(onNull.name());
                new Exception().printStackTrace();
                return Optional.empty();
            }
            return Optional.of(result.getBody());
        } catch (Exception e) {
            log.warn(onException.name());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private Optional<?> sendRequest(HttpEntity<?> httpEntity, String path, HttpMethod httpMethod, ParameterizedTypeReference<?> parameterizedTypeReference, LogMessage onNull, LogMessage onException) {
        final String fullPath = baseUrl + path;
        return sendRequest(() -> restTemplate.exchange(fullPath, httpMethod, httpEntity, parameterizedTypeReference), onNull, onException);
    }

    private Optional<?> sendRequest(HttpEntity<?> httpEntity, String path, HttpMethod httpMethod, Class<?> clazz, LogMessage onNull, LogMessage onException) {
        final String fullPath = baseUrl + path;
        return sendRequest(() -> restTemplate.exchange(fullPath, httpMethod, httpEntity, clazz), onNull, onException);
    }

    public Optional<List<GiteaExistingUser>> getUsers() {
        final var entity = new HttpEntity<String>(headers);
        return (Optional<List<GiteaExistingUser>>)
                sendRequest(entity, ADMIN_USERS_PATH, HttpMethod.GET, new ParameterizedTypeReference<List<GiteaExistingUser>>() {},
                        LogMessage.ALERT_20014, LogMessage.ALERT_20015);
    }

    public Optional<GiteaExistingUser> createUser(GiteaNewUser giteaNewUser) {
        final var entity = new HttpEntity<>(giteaNewUser, headers);
        return (Optional<GiteaExistingUser>)
                sendRequest(entity, ADMIN_USERS_PATH, HttpMethod.POST, GiteaExistingUser.class,
                        LogMessage.ALERT_20016, LogMessage.ALERT_20017);
    }

}
