package discord.services;

import discord.dto.gitea.api.*;
import discord.exception.main.EmptyOptionalException;
import discord.exception.main.FailedToCreateGiteaAdminTokenException;
import discord.exception.GiteaApiException;
import discord.localisation.LogMessage;
import discord.model.AppSettings;
import discord.repository.AppSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.*;

import static java.util.Optional.ofNullable;

@SuppressWarnings("unchecked")
@Slf4j
@Component
@RequiredArgsConstructor
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
    private final static String USER_SIGN_UP_PATH = "/user/sign_up";
    private final static String USERS_TOKENS_PATH_FORMAT = "/users/%s/tokens";

    private final static List<String> HOOK_EVENT_TYPES = List.of("push");
    private final static String HOOK_TYPE = "gitea";
    private final static boolean HOOK_ACTIVE = true;
    private final static String HOOK_CONTENT_TYPE = "json";
    private final static String ADMIN_ACCESS_TOKEN_NAME = "ADMIN_TOKEN";
    private final static String API_PATH_FORMAT = "%s/api/v1%s";
    private final static String URL_PATH_FORMAT = "%s%s";

    public final static String HOOK_PATH = "/dialog-push";

    @Value("${gitea.branch-name}")
    private String branchName;

    @Value("${app-reference.protocol}://${app-reference.address}:${server.port}")
    private String hookServerUrl;

    @Value("${gitea.protocol}://${gitea.address}:${gitea.port}")
    private String giteaBaseUrl;

    @Value("${gitea.admin.username}")
    private String adminUsername;

    @Value("${gitea.admin.password}")
    private String adminPassword;

    @Value("${gitea.admin.email}")
    private String adminEmail;


    private final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
    private final HttpHeaders headers = new HttpHeaders();

    private final AppSettingsRepository appSettingsRepository;

    @PostConstruct
    private void postConstruct() {
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        final var token = appSettingsRepository.findOne()
                .map(AppSettings::getGiteaToken)
                .orElseGet(this::createAndSaveGiteaToken);

        headers.add("Authorization", "token " + token);

    }

    @Transactional
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

    private Object sendApiRequest(HttpEntity<?> httpEntity, String path, HttpMethod httpMethod, ParameterizedTypeReference<?> parameterizedTypeReference, LogMessage onNull, LogMessage onException) throws GiteaApiException {
        final String fullPath = String.format(API_PATH_FORMAT, giteaBaseUrl, path);
        try {
            final var result = restTemplate.exchange(fullPath, httpMethod, httpEntity, parameterizedTypeReference);
            return ofNullable(result.getBody())
                    .orElseThrow(() -> new GiteaApiException(onException, "Result body is null"));
        } catch (Exception e) {
            throw new GiteaApiException(onException, e.getMessage());
        }
    }

    private void sendRequest(HttpEntity<?> httpEntity, String path, HttpMethod httpMethod, LogMessage onException, String pathFormat) throws GiteaApiException {
        final String fullPath = String.format(pathFormat, giteaBaseUrl, path);
        try {
            restTemplate.exchange(fullPath, httpMethod, httpEntity, Void.class);
        } catch (Exception e) {
            throw new GiteaApiException(onException, e.getMessage());
        }
    }

    private void sendApiRequest(HttpEntity<?> httpEntity, String path, HttpMethod httpMethod, LogMessage onException) throws GiteaApiException {
        sendRequest(httpEntity, path, httpMethod, onException, API_PATH_FORMAT);
    }

    private void sendUrlRequest(HttpEntity<?> httpEntity, String path, HttpMethod httpMethod, LogMessage onException) throws GiteaApiException {
        sendRequest(httpEntity, path, httpMethod, onException, URL_PATH_FORMAT);
    }

    public GiteaUser createUser(CreateUserOption createUserOption) throws GiteaApiException {
        final var entity = new HttpEntity<>(createUserOption, headers);
        return (GiteaUser)
                sendApiRequest(entity, ADMIN_USERS_PATH, HttpMethod.POST, new ParameterizedTypeReference<GiteaUser>() {},
                        LogMessage.ALERT_20014, LogMessage.ALERT_20015);
    }

    public void createRepository(String owner, CreateRepoOption createRepoOption) throws GiteaApiException {
        final var path = String.format(ADMIN_USER_REPO_PATH_FORMAT, owner);
        final var entity = new HttpEntity<>(createRepoOption, headers);
        sendApiRequest(entity, path, HttpMethod.POST, LogMessage.ALERT_20018);
    }

    public void createFile(String owner, String repo, String filepath, CreateFileOption createFileOption) throws GiteaApiException {
        final var path = String.format(REPO_FILE_PATH_FORMAT, owner, repo, filepath, branchName);
        final var entity = new HttpEntity<>(createFileOption, headers);
        sendApiRequest(entity, path, HttpMethod.POST, LogMessage.ALERT_20019);
    }

    public ContentsResponse getFile(String owner, String repo, String filepath, String ref) throws GiteaApiException {
        final var path = String.format(REPO_FILE_PATH_FORMAT, owner, repo, filepath, ref);
        final var entity = new HttpEntity<String>(headers);
        return (ContentsResponse)
                sendApiRequest(entity, path, HttpMethod.GET, new ParameterizedTypeReference<ContentsResponse>() {},
                        LogMessage.ALERT_20020, LogMessage.ALERT_20021);
    }

    public List<RepoCommit> getCommits(String owner, String repo, int limit) throws GiteaApiException {
        final var path = String.format(REPO_COMMITS_PATH_FORMAT, owner, repo, branchName, limit);
        final var entity = new HttpEntity<String>(headers);
        return (List<RepoCommit>)
                sendApiRequest(entity, path, HttpMethod.GET, new ParameterizedTypeReference<List<RepoCommit>>() {},
                        LogMessage.ALERT_20022, LogMessage.ALERT_20023);
    }

    public void editUser(EditUserOption editUserOption) throws GiteaApiException {
        final var path = String.format(ADMIN_USER_PATH_FORMAT, editUserOption.getLoginName());
        final var entity = new HttpEntity<>(editUserOption, headers);
        sendApiRequest(entity, path, HttpMethod.PATCH, LogMessage.ALERT_20024);
    }

    public void deleteUser(String username) throws GiteaApiException {
        final var path = String.format(ADMIN_USER_PATH_FORMAT, username);
        final var entity = new HttpEntity<>( headers);
        sendApiRequest(entity, path, HttpMethod.DELETE, LogMessage.ALERT_20025);
    }

    public void deleteRepo(String owner, String repo) throws GiteaApiException {
        final var path = String.format(REPO_OWNER_PATH_FORMAT, owner, repo);
        final var entity = new HttpEntity<>( headers);
        sendApiRequest(entity, path, HttpMethod.DELETE, LogMessage.ALERT_20026);
    }

    public SearchResult<Repo> getReposByUid(int userId) throws GiteaApiException {
        final var entity = new HttpEntity<String>(headers);
        final var path = String.format(REPO_SEARCH_UID_PATH_FORMAT, userId);
        return (SearchResult<Repo>)
                sendApiRequest(entity, path, HttpMethod.GET, new ParameterizedTypeReference<SearchResult<Repo>>() {},
                        LogMessage.ALERT_20034, LogMessage.ALERT_20035);
    }

    public GiteaUser getUserByUid(int userId) throws GiteaApiException {
        final var entity = new HttpEntity<String>(headers);
        final var path = String.format(USERS_SEARCH_UID_PATH_FORMAT, userId);
        final var searchResult = (SearchResult<GiteaUser>) sendApiRequest(entity, path, HttpMethod.GET, new ParameterizedTypeReference<SearchResult<GiteaUser>>() {},
                        LogMessage.ALERT_20034, LogMessage.ALERT_20035);
        return ofNullable(searchResult.getData().get(0))
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20069));
    }

    public void createHook(String owner, String repo) throws GiteaApiException {
        final var path = String.format(REPOS_OWNER_REPO_HOOKS_PATH_FORMAT, owner, repo);
        final var fullHookUrl = hookServerUrl + HOOK_PATH;
        final var hookConfig = new CreateHookOptionConfig(HOOK_CONTENT_TYPE, fullHookUrl);
        final var hook = new CreateHookOption(hookConfig, HOOK_EVENT_TYPES, HOOK_TYPE, HOOK_ACTIVE);
        final var entity = new HttpEntity<>(hook, headers);
        sendApiRequest(entity, path, HttpMethod.POST, LogMessage.ALERT_20049);
    }

    public void createAdminUser() throws GiteaApiException {
        final var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        final var requestBody  = new CreateUserOption(adminUsername, adminPassword, adminEmail)
                .asMultiValueMap();

        final var entity = new HttpEntity<>(requestBody, headers);
        sendUrlRequest(entity, USER_SIGN_UP_PATH, HttpMethod.POST, LogMessage.ALERT_20075);
    }

    public AccessToken getAdminUserToken() throws GiteaApiException {
        final var headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", getBasicAuthenticationHeader(adminUsername, adminPassword));
        final var requestBody = new CreateAccessTokenOption(ADMIN_ACCESS_TOKEN_NAME);
        final var path = String.format(USERS_TOKENS_PATH_FORMAT, adminUsername);

        final var entity = new HttpEntity<>(requestBody, headers);
        return (AccessToken)
                sendApiRequest(entity, path, HttpMethod.POST, new ParameterizedTypeReference<AccessToken>() {},
                        LogMessage.ALERT_20080, LogMessage.ALERT_20081);
    }

    private static String getBasicAuthenticationHeader(String username, String password) {
        String valueToEncode = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }
}
