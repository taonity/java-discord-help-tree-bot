package discord.services;

import discord.dao.gitea.api.*;
import discord.exception.EmptyOptionalException;
import discord.exception.GiteaApiException;
import discord.exception.NullObjectException;
import discord.localisation.LogMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.*;

import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;

@SuppressWarnings("unchecked")
@Slf4j
@Component
public class GiteaApiService {
    private final static String ADMIN_USERS_PATH = "/admin/users";
    private final static String ADMIN_USER_REPO_PATH_FORMAT = "/admin/users/%s/repos";
    private final static String REPO_FILE_PATH_FORMAT = "/repos/%s/%s/contents/%s?ref=%s";
    private final static String REPO_COMMITS_PATH_FORMAT = "/repos/%s/%s/commits?sha=%s&limit=%s";
    private final static String ADMIN_USER_PATH_FORMAT = "/admin/users/%s";
    private final static String REPO_OWNER_PATH_FORMAT = "/repos/%s/%s";
    private final static String REPO_SEARCH_UID_PATH_FORMAT = "/repos/search?uid=%s";
    private final static String USERS_SEARCH_UID_PATH_FORMAT = "/users/search?uid=%s";
    private final static String REPOS_OWNER_REPO_HOOKS_PATH_FORMAT = "/repos/%s/%s/hooks";

    private final static List<String> HOOK_EVENT_TYPES = List.of("push");
    private final static String HOOK_TYPE = "gitea";
    private final static boolean HOOK_ACTIVE = true;
    private final static String HOOK_CONTENT_TYPE = "json";

    public final static String HOOK_PATH = "/dialog-push";

    @Value("${gitea.branch-name}")
    private String branchName;

    @Value("${server.protocol}://${server.address}:${server.port}")
    private String hookServerUrl;

    @Value("${gitea.protocol}://${gitea.address}:${gitea.port}/api/v1")
    private String giteaBaseUrl;

    @Value("token ${gitea.token}")
    private String token;


    private final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());

    private final HttpHeaders headers = new HttpHeaders();

    @PostConstruct
    private void postConstruct() {
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", token);

    }

    private Object sendRequest(HttpEntity<?> httpEntity, String path, HttpMethod httpMethod, ParameterizedTypeReference<?> parameterizedTypeReference, LogMessage onNull, LogMessage onException) {
        final String fullPath = giteaBaseUrl + path;
        try {
            final var result = restTemplate.exchange(fullPath, httpMethod, httpEntity, parameterizedTypeReference);
            if(isNull(result.getBody())) {
                throw new NullObjectException(onNull);
            }
            return result.getBody();
        } catch (Exception e) {
            throw new GiteaApiException(onException, e.getMessage());
        }
    }

    private void sendRequest(HttpEntity<?> httpEntity, String path, HttpMethod httpMethod, LogMessage onException) {
        final String fullPath = giteaBaseUrl + path;
        try {
            restTemplate.exchange(fullPath, httpMethod, httpEntity, Void.class);
        } catch (Exception e) {
            throw new GiteaApiException(onException, e.getMessage());
        }
    }


    public List<GiteaUser> getUsers() {
        final var entity = new HttpEntity<String>(headers);
        return (List<GiteaUser>)
                sendRequest(entity, ADMIN_USERS_PATH, HttpMethod.GET, new ParameterizedTypeReference<List<GiteaUser>>() {},
                        LogMessage.ALERT_20013, LogMessage.ALERT_20027);
    }

    public GiteaUser createUser(CreateUserOption createUserOption) {
        final var entity = new HttpEntity<>(createUserOption, headers);
        return (GiteaUser)
                sendRequest(entity, ADMIN_USERS_PATH, HttpMethod.POST, new ParameterizedTypeReference<GiteaUser>() {},
                        LogMessage.ALERT_20014, LogMessage.ALERT_20015);
    }

    public void createRepository(String owner, CreateRepoOption createRepoOption) {
        final var path = String.format(ADMIN_USER_REPO_PATH_FORMAT, owner);
        final var entity = new HttpEntity<>(createRepoOption, headers);
        sendRequest(entity, path, HttpMethod.POST, LogMessage.ALERT_20018);
    }

    public void createFile(String owner, String repo, String filepath, CreateFileOption createFileOption) {
        final var path = String.format(REPO_FILE_PATH_FORMAT, owner, repo, filepath, branchName);
        final var entity = new HttpEntity<>(createFileOption, headers);
        sendRequest(entity, path, HttpMethod.POST, LogMessage.ALERT_20019);
    }

    public ContentsResponse getFile(String owner, String repo, String filepath, String ref) {
        final var path = String.format(REPO_FILE_PATH_FORMAT, owner, repo, filepath, ref);
        final var entity = new HttpEntity<String>(headers);
        return (ContentsResponse)
                sendRequest(entity, path, HttpMethod.GET, new ParameterizedTypeReference<ContentsResponse>() {},
                        LogMessage.ALERT_20020, LogMessage.ALERT_20021);
    }

    public List<RepoCommit> getCommits(String owner, String repo, int limit) {
        final var path = String.format(REPO_COMMITS_PATH_FORMAT, owner, repo, branchName, limit);
        final var entity = new HttpEntity<String>(headers);
        return (List<RepoCommit>)
                sendRequest(entity, path, HttpMethod.GET, new ParameterizedTypeReference<List<RepoCommit>>() {},
                        LogMessage.ALERT_20022, LogMessage.ALERT_20023);
    }

    public void editUser(EditUserOption editUserOption) {
        final var path = String.format(ADMIN_USER_PATH_FORMAT, editUserOption.getLoginName());
        final var entity = new HttpEntity<>(editUserOption, headers);
        sendRequest(entity, path, HttpMethod.PATCH, LogMessage.ALERT_20024);
    }

    public void deleteUser(String username) {
        final var path = String.format(ADMIN_USER_PATH_FORMAT, username);
        final var entity = new HttpEntity<>( headers);
        sendRequest(entity, path, HttpMethod.DELETE, LogMessage.ALERT_20025);
    }

    public void deleteRepo(String owner, String repo) {
        final var path = String.format(REPO_OWNER_PATH_FORMAT, owner, repo);
        final var entity = new HttpEntity<>( headers);
        sendRequest(entity, path, HttpMethod.DELETE, LogMessage.ALERT_20026);
    }

    public SearchResult<Repo> getReposByUid(int userId) {
        final var entity = new HttpEntity<String>(headers);
        final var path = String.format(REPO_SEARCH_UID_PATH_FORMAT, userId);
        return (SearchResult<Repo>)
                sendRequest(entity, path, HttpMethod.GET, new ParameterizedTypeReference<SearchResult<Repo>>() {},
                        LogMessage.ALERT_20034, LogMessage.ALERT_20035);
    }

    public GiteaUser getUserByUid(int userId) {
        final var entity = new HttpEntity<String>(headers);
        final var path = String.format(USERS_SEARCH_UID_PATH_FORMAT, userId);
        final var searchResult = (SearchResult<GiteaUser>) sendRequest(entity, path, HttpMethod.GET, new ParameterizedTypeReference<SearchResult<GiteaUser>>() {},
                        LogMessage.ALERT_20034, LogMessage.ALERT_20035);
        return ofNullable(searchResult.getData().get(0))
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20069));
    }

    public void createHook(String owner, String repo) {
        final var path = String.format(REPOS_OWNER_REPO_HOOKS_PATH_FORMAT, owner, repo);
        final var fullHookUrl = hookServerUrl + HOOK_PATH;
        final var hookConfig = new CreateHookOptionConfig(HOOK_CONTENT_TYPE, fullHookUrl);
        final var hook = new CreateHookOption(hookConfig, HOOK_EVENT_TYPES, HOOK_TYPE, HOOK_ACTIVE);
        final var entity = new HttpEntity<>(hook, headers);
        sendRequest(entity, path, HttpMethod.POST, LogMessage.ALERT_20049);
    }
}
