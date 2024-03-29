package automation.services;

import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;

import automation.services.dto.UpdateFileOption;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.taonity.helpbot.discord.event.command.gitea.api.ContentsResponse;
import org.taonity.helpbot.discord.event.command.gitea.api.Repo;
import org.taonity.helpbot.discord.event.command.gitea.api.SearchResult;

@Slf4j
@Component
@RequiredArgsConstructor
public class GiteaApiTestService {

    private static final String API_PATH_FORMAT = "%s/api/v1%s";
    private static final String REPO_SEARCH_UID_PATH_FORMAT = "/repos/search?uid=%s";
    private static final String REPO_FILE_PATH_FORMAT = "/repos/%s/%s/contents/%s?ref=%s";

    @Value("${gitea.branch-name}")
    private String branchName;

    @Value("${gitea.url}")
    private String giteaBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
    private final HttpHeaders headers = new HttpHeaders();

    private final JdbcTemplate jdbcTemplate;
    ;

    private HttpHeaders getHeaders() {
        if (!isNull(headers.get("Authorization"))) {
            return headers;
        }
        final var token = jdbcTemplate.queryForObject("SELECT gitea_token FROM app_settings ", String.class);

        if (isNull(token)) {
            throw new RuntimeException("Failed to retrieve gitea token");
        }

        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "token " + token);
        return headers;
    }

    private Object sendApiRequest(
            HttpEntity<?> httpEntity, String path, ParameterizedTypeReference<?> parameterizedTypeReference) {
        final String fullPath = String.format(API_PATH_FORMAT, giteaBaseUrl, path);
        try {
            final var result = restTemplate.exchange(fullPath, HttpMethod.GET, httpEntity, parameterizedTypeReference);
            return ofNullable(result.getBody()).orElseThrow(() -> new RuntimeException("Result body is null"));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void sendApiRequest(HttpEntity<?> httpEntity, String path, HttpMethod httpMethod) {
        final String fullPath = String.format(API_PATH_FORMAT, giteaBaseUrl, path);
        try {
            restTemplate.exchange(fullPath, httpMethod, httpEntity, Void.class);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public SearchResult<Repo> getReposByUid(int userId) {
        final var entity = new HttpEntity<String>(getHeaders());
        final var path = String.format(REPO_SEARCH_UID_PATH_FORMAT, userId);
        return (SearchResult<Repo>)
                sendApiRequest(entity, path, new ParameterizedTypeReference<SearchResult<Repo>>() {});
    }

    public ContentsResponse getFile(String owner, String repo, String filepath, String ref) {
        final var path = String.format(REPO_FILE_PATH_FORMAT, owner, repo, filepath, ref);
        final var entity = new HttpEntity<String>(getHeaders());
        return (ContentsResponse) sendApiRequest(entity, path, new ParameterizedTypeReference<ContentsResponse>() {});
    }

    public void updateFile(String owner, String repo, String filepath, UpdateFileOption updateFileOption) {
        final var path = String.format(REPO_FILE_PATH_FORMAT, owner, repo, filepath, branchName);
        final var entity = new HttpEntity<>(updateFileOption, getHeaders());
        sendApiRequest(entity, path, HttpMethod.PUT);
    }
}
