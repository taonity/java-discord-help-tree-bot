package discord.automation.config;

import discord.dto.gitea.api.ContentsResponse;
import discord.dto.gitea.api.Repo;
import discord.dto.gitea.api.SearchResult;
import discord.exception.GiteaApiException;
import discord.model.AppSettings;
import discord.repository.AppSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Collections;

import static java.util.Optional.ofNullable;

@Slf4j
@Component
@RequiredArgsConstructor
public class GiteaApiTestService {

    private final static String API_PATH_FORMAT = "%s/api/v1%s";
    private final static String REPO_SEARCH_UID_PATH_FORMAT = "/repos/search?uid=%s";
    private final static String REPO_FILE_PATH_FORMAT = "/repos/%s/%s/contents/%s?ref=%s";


    @Value("${gitea.url}")
    private String giteaBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
    private final HttpHeaders headers = new HttpHeaders();

    private final AppSettingsRepository appSettingsRepository;

    @PostConstruct
    private void postConstruct() {
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        final var token = appSettingsRepository.findOne()
                .map(AppSettings::getGiteaToken)
                .orElseThrow(() -> new RuntimeException("Failed to retrieve gitea token"));

        headers.add("Authorization", "token " + token);
    }

    private Object sendApiRequest(HttpEntity<?> httpEntity, String path, HttpMethod httpMethod, ParameterizedTypeReference<?> parameterizedTypeReference) {
        final String fullPath = String.format(API_PATH_FORMAT, giteaBaseUrl, path);
        try {
            final var result = restTemplate.exchange(fullPath, httpMethod, httpEntity, parameterizedTypeReference);
            return ofNullable(result.getBody())
                    .orElseThrow(() -> new RuntimeException("Result body is null"));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    @SuppressWarnings("unchecked")
    public SearchResult<Repo> getReposByUid(int userId) {
        final var entity = new HttpEntity<String>(headers);
        final var path = String.format(REPO_SEARCH_UID_PATH_FORMAT, userId);
        return (SearchResult<Repo>)
                sendApiRequest(entity, path, HttpMethod.GET, new ParameterizedTypeReference<SearchResult<Repo>>() {});
    }

    public ContentsResponse getFile(String owner, String repo, String filepath, String ref) {
        final var path = String.format(REPO_FILE_PATH_FORMAT, owner, repo, filepath, ref);
        final var entity = new HttpEntity<String>(headers);
        return (ContentsResponse)
                sendApiRequest(entity, path, HttpMethod.GET, new ParameterizedTypeReference<ContentsResponse>() {});
    }
}
