package discord.services;

import static java.util.Optional.ofNullable;

import discord.dto.gitea.api.*;
import discord.exception.GiteaApiException;
import discord.exception.main.EmptyOptionalException;
import discord.exception.main.FailedToCreateGiteaAdminTokenException;
import discord.logging.LogMessage;
import discord.model.AppSettings;
import discord.repository.AppSettingsRepository;
import java.util.*;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@SuppressWarnings({"unchecked", "squid:S1075"})
@Slf4j
@Component
@RequiredArgsConstructor
public class GiteaApiService {
    private static final String ADMIN_USERS_PATH = "/admin/users";
    private static final String ADMIN_USER_REPO_PATH_FORMAT = "/admin/users/%s/repos";
    private static final String REPO_FILE_PATH_FORMAT = "/repos/%s/%s/contents/%s?ref=%s";
    private static final String REPO_COMMITS_PATH_FORMAT = "/repos/%s/%s/commits?sha=%s&limit=%s";
    private static final String ADMIN_USER_PATH_FORMAT = "/admin/users/%s";
    private static final String REPO_OWNER_PATH_FORMAT = "/repos/%s/%s";
    private static final String REPO_SEARCH_UID_PATH_FORMAT = "/repos/search?uid=%s";
    private static final String USERS_SEARCH_UID_PATH_FORMAT = "/users/search?uid=%s";
    private static final String REPOS_OWNER_REPO_HOOKS_PATH_FORMAT = "/repos/%s/%s/hooks";
    private static final String USER_SIGN_UP_PATH = "/user/sign_up";
    private static final String USERS_TOKENS_PATH_FORMAT = "/users/%s/tokens";

    private static final List<String> HOOK_EVENT_TYPES = List.of("push");
    private static final String HOOK_TYPE = "gitea";
    private static final boolean HOOK_ACTIVE = true;
    private static final String HOOK_CONTENT_TYPE = "json";
    private static final String ADMIN_ACCESS_TOKEN_NAME = "ADMIN_TOKEN";
    private static final String API_PATH_FORMAT = "%s/api/v1%s";
    private static final String URL_PATH_FORMAT = "%s%s";

    public static final String HOOK_PATH = "/dialog-push";

    @Value("${gitea.branch-name}")
    private String branchName;

    @Value("${app-reference.url}")
    private String hookServerUrl;

    @Value("${gitea.url}")
    private String giteaBaseUrl;

    @Value("${gitea.admin.username}")
    private String adminUsername;

    @Value("${gitea.admin.password}")
    private String adminPassword;

    @Value("${gitea.admin.email}")
    private String adminEmail;

    private final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
    private final HttpHeaders tokenHeaders = new HttpHeaders();

    private final AppSettingsRepository appSettingsRepository;

    @PostConstruct
    private void postConstruct() {
        tokenHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        final var token = appSettingsRepository
                .findOne()
                .map(AppSettings::getGiteaToken)
                .orElseGet(this::createAndSaveGiteaToken);

        tokenHeaders.add("Authorization", "token " + token);
    }

    private String createAndSaveGiteaToken() {
        try {
            createAdminUser();
            final var token = getAdminUserToken().getSha1();
            appSettingsRepository.save(new AppSettings(token));
            return token;
        } catch (GiteaApiException e) {
            throw new FailedToCreateGiteaAdminTokenException(LogMessage.ALERT_20082, e);
        }
    }

    private Object sendApiRequest(
            HttpEntity<?> httpEntity,
            String path,
            HttpMethod httpMethod,
            ParameterizedTypeReference<?> parameterizedTypeReference,
            LogMessage onNull,
            LogMessage onException)
            throws GiteaApiException {
        final String fullPath = String.format(API_PATH_FORMAT, giteaBaseUrl, path);
        try {
            final var result = restTemplate.exchange(fullPath, httpMethod, httpEntity, parameterizedTypeReference);
            return ofNullable(result.getBody()).orElseThrow(() -> new GiteaApiException(onNull, "Result body is null"));
        } catch (Exception e) {
            log.warn(onException.name());
            throw new GiteaApiException(onException, e.getMessage());
        }
    }

    private void sendRequest(
            HttpEntity<?> httpEntity, String path, HttpMethod httpMethod, LogMessage onException, String pathFormat)
            throws GiteaApiException {
        final String fullPath = String.format(pathFormat, giteaBaseUrl, path);
        try {
            restTemplate.exchange(fullPath, httpMethod, httpEntity, Void.class);
        } catch (Exception e) {
            throw new GiteaApiException(onException, e.getMessage());
        }
    }

    private void sendApiRequest(HttpEntity<?> httpEntity, String path, HttpMethod httpMethod, LogMessage onException)
            throws GiteaApiException {
        sendRequest(httpEntity, path, httpMethod, onException, API_PATH_FORMAT);
    }

    private void sendUrlRequest(HttpEntity<?> httpEntity)
            throws GiteaApiException {
        sendRequest(httpEntity, USER_SIGN_UP_PATH, HttpMethod.POST, LogMessage.ALERT_20075, URL_PATH_FORMAT);
    }

    public GiteaUser createUser(CreateUserOption createUserOption) throws GiteaApiException {
        final var entity = new HttpEntity<>(createUserOption, tokenHeaders);
        return (GiteaUser) sendApiRequest(
                entity,
                ADMIN_USERS_PATH,
                HttpMethod.POST,
                new ParameterizedTypeReference<GiteaUser>() {},
                LogMessage.ALERT_20014,
                LogMessage.ALERT_20015);
    }

    public void createRepository(String owner, CreateRepoOption createRepoOption) throws GiteaApiException {
        final var path = String.format(ADMIN_USER_REPO_PATH_FORMAT, owner);
        final var entity = new HttpEntity<>(createRepoOption, tokenHeaders);
        sendApiRequest(entity, path, HttpMethod.POST, LogMessage.ALERT_20018);
    }

    public void createFile(String owner, String repo, String filepath, CreateFileOption createFileOption)
            throws GiteaApiException {
        final var path = String.format(REPO_FILE_PATH_FORMAT, owner, repo, filepath, branchName);
        final var entity = new HttpEntity<>(createFileOption, tokenHeaders);
        sendApiRequest(entity, path, HttpMethod.POST, LogMessage.ALERT_20019);
    }

    public ContentsResponse getFile(String owner, String repo, String filepath, String ref) throws GiteaApiException {
        final var path = String.format(REPO_FILE_PATH_FORMAT, owner, repo, filepath, ref);
        final var entity = new HttpEntity<String>(tokenHeaders);
        return (ContentsResponse) sendApiRequest(
                entity,
                path,
                HttpMethod.GET,
                new ParameterizedTypeReference<ContentsResponse>() {},
                LogMessage.ALERT_20020,
                LogMessage.ALERT_20021);
    }

    public List<RepoCommit> getCommits(String owner, String repo, int limit) throws GiteaApiException {
        final var path = String.format(REPO_COMMITS_PATH_FORMAT, owner, repo, branchName, limit);
        final var entity = new HttpEntity<String>(tokenHeaders);
        return (List<RepoCommit>) sendApiRequest(
                entity,
                path,
                HttpMethod.GET,
                new ParameterizedTypeReference<List<RepoCommit>>() {},
                LogMessage.ALERT_20022,
                LogMessage.ALERT_20023);
    }

    public void editUser(EditUserOption editUserOption) throws GiteaApiException {
        final var path = String.format(ADMIN_USER_PATH_FORMAT, editUserOption.getLoginName());
        final var entity = new HttpEntity<>(editUserOption, tokenHeaders);
        sendApiRequest(entity, path, HttpMethod.PATCH, LogMessage.ALERT_20024);
    }

    public void deleteUser(String username) throws GiteaApiException {
        final var path = String.format(ADMIN_USER_PATH_FORMAT, username);
        final var entity = new HttpEntity<>(tokenHeaders);
        sendApiRequest(entity, path, HttpMethod.DELETE, LogMessage.ALERT_20025);
    }

    public void deleteRepo(String owner, String repo) throws GiteaApiException {
        final var path = String.format(REPO_OWNER_PATH_FORMAT, owner, repo);
        final var entity = new HttpEntity<>(tokenHeaders);
        sendApiRequest(entity, path, HttpMethod.DELETE, LogMessage.ALERT_20026);
    }

    public SearchResult<Repo> getReposByUid(int userId) throws GiteaApiException {
        final var entity = new HttpEntity<String>(tokenHeaders);
        final var path = String.format(REPO_SEARCH_UID_PATH_FORMAT, userId);
        return (SearchResult<Repo>) sendApiRequest(
                entity,
                path,
                HttpMethod.GET,
                new ParameterizedTypeReference<SearchResult<Repo>>() {},
                LogMessage.ALERT_20034,
                LogMessage.ALERT_20035);
    }

    public GiteaUser getUserByUid(int userId) throws GiteaApiException {
        final var entity = new HttpEntity<String>(tokenHeaders);
        final var path = String.format(USERS_SEARCH_UID_PATH_FORMAT, userId);
        final var searchResult = (SearchResult<GiteaUser>) sendApiRequest(
                entity,
                path,
                HttpMethod.GET,
                new ParameterizedTypeReference<SearchResult<GiteaUser>>() {},
                LogMessage.ALERT_20034,
                LogMessage.ALERT_20035);
        return ofNullable(searchResult.getData().get(0))
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20069));
    }

    public void createHook(String owner, String repo) throws GiteaApiException {
        final var path = String.format(REPOS_OWNER_REPO_HOOKS_PATH_FORMAT, owner, repo);
        final var fullHookUrl = hookServerUrl + HOOK_PATH;
        final var hookConfig = new CreateHookOptionConfig(HOOK_CONTENT_TYPE, fullHookUrl);
        final var hook = new CreateHookOption(hookConfig, HOOK_EVENT_TYPES, HOOK_TYPE, HOOK_ACTIVE);
        final var entity = new HttpEntity<>(hook, tokenHeaders);
        sendApiRequest(entity, path, HttpMethod.POST, LogMessage.ALERT_20049);
    }

    public void createAdminUser() throws GiteaApiException {
        final var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        final var requestBody = new CreateUserOption(adminUsername, adminPassword, adminEmail).asMultiValueMap();

        final var entity = new HttpEntity<>(requestBody, headers);
        sendUrlRequest(entity);
    }

    public AccessToken getAdminUserToken() throws GiteaApiException {
        final var basicAuthHeaders = new HttpHeaders();
        basicAuthHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        basicAuthHeaders.add("Authorization", getBasicAuthenticationHeader(adminUsername, adminPassword));
        final var requestBody = new CreateAccessTokenOption(ADMIN_ACCESS_TOKEN_NAME);
        final var path = String.format(USERS_TOKENS_PATH_FORMAT, adminUsername);

        final var entity = new HttpEntity<>(requestBody, basicAuthHeaders);
        return (AccessToken) sendApiRequest(
                entity,
                path,
                HttpMethod.POST,
                new ParameterizedTypeReference<AccessToken>() {},
                LogMessage.ALERT_20080,
                LogMessage.ALERT_20081);
    }

    private static String getBasicAuthenticationHeader(String username, String password) {
        String valueToEncode = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }
}
