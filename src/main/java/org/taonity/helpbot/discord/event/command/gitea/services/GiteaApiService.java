package org.taonity.helpbot.discord.event.command.gitea.services;

import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;

import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.taonity.helpbot.discord.event.command.gitea.AppSettings;
import org.taonity.helpbot.discord.event.command.gitea.AppSettingsRepository;
import org.taonity.helpbot.discord.event.command.gitea.api.*;
import org.taonity.helpbot.discord.logging.LogMessage;
import org.taonity.helpbot.discord.logging.exception.GiteaApiException;
import org.taonity.helpbot.discord.logging.exception.main.EmptyOptionalException;
import org.taonity.helpbot.discord.logging.exception.main.FailedToCreateGiteaAdminTokenException;
import reactor.core.publisher.Mono;

@SuppressWarnings({"unchecked", "squid:S1075"})
@Slf4j
@Component
@RequiredArgsConstructor
public class GiteaApiService {
    private static final String ADMIN_USERS_PATH = "/api/v1/admin/users";
    private static final String ADMIN_USER_REPO_PATH_FORMAT = "/api/v1/admin/users/%s/repos";
    private static final String REPO_FILE_PATH_FORMAT = "/api/v1/repos/%s/%s/contents/%s?ref=%s";
    private static final String REPO_COMMITS_PATH_FORMAT = "/api/v1/repos/%s/%s/commits?sha=%s&limit=%s";
    private static final String ADMIN_USER_PATH_FORMAT = "/api/v1/admin/users/%s";
    private static final String REPO_OWNER_PATH_FORMAT = "/api/v1/repos/%s/%s";
    private static final String REPO_SEARCH_UID_PATH_FORMAT = "/api/v1/repos/search?uid=%s";
    private static final String USERS_SEARCH_UID_PATH_FORMAT = "/api/v1/users/search?uid=%s";
    private static final String REPOS_OWNER_REPO_HOOKS_PATH_FORMAT = "/api/v1/repos/%s/%s/hooks";
    private static final String USERS_TOKENS_PATH_FORMAT = "/api/v1/users/%s/tokens";
    private static final String USER_SIGN_UP_PATH = "/user/sign_up";
    private static final List<String> HOOK_EVENT_TYPES = List.of("push");
    private static final String HOOK_TYPE = "gitea";
    private static final boolean HOOK_ACTIVE = true;
    private static final String HOOK_CONTENT_TYPE = "json";
    private static final String ADMIN_ACCESS_TOKEN_NAME = "ADMIN_TOKEN";

    public static final String HOOK_PATH = "/dialog-push";

    @Value("${gitea.branch-name}")
    private String branchName;

    @Value("${app-reference.url}")
    private String hookServerUrl;

    @Value("${gitea.private.url}")
    private String giteaBaseUrl;

    @Value("${gitea.admin.username}")
    private String adminUsername;

    @Value("${gitea.admin.password}")
    private String adminPassword;

    @Value("${gitea.admin.email}")
    private String adminEmail;

    private WebClient webClient;

    private final AppSettingsRepository appSettingsRepository;

    public Mono<Void> init() {
        webClient = WebClient.builder().baseUrl(giteaBaseUrl).build();
        return appSettingsRepository
                .findOne()
                .defaultIfEmpty(new AppSettings())
                .flatMap(appSettings -> {
                    if (isNull(appSettings.getGiteaToken())) {
                        return this.createAndSaveGiteaToken();
                    } else {
                        return Mono.just(appSettings.getGiteaToken());
                    }
                })
                .doOnSuccess(token -> webClient = WebClient.builder()
                        .baseUrl(giteaBaseUrl)
                        .defaultHeaders(httpHeaders -> {
                            httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                            httpHeaders.add(HttpHeaders.AUTHORIZATION, "token " + token);
                        })
                        .build())
                .then();
    }

    private Mono<String> createAndSaveGiteaToken() {
        final var createAndSaveAdminToken = getAdminUserToken()
                .flatMap(adminUserToken -> appSettingsRepository
                        .save(new AppSettings(adminUserToken.getSha1()))
                        .thenReturn(adminUserToken.getSha1()))
                .onErrorMap(e -> new FailedToCreateGiteaAdminTokenException(LogMessage.ALERT_20082, e));

        return createAdminUser().then(createAndSaveAdminToken);
    }

    private <T> Mono<?> sendApiRequest(
            T body,
            String path,
            HttpMethod httpMethod,
            ParameterizedTypeReference<?> parameterizedTypeReference,
            LogMessage onNull,
            LogMessage onException) {
        return webClient
                .method(httpMethod)
                .uri(path)
                .body(BodyInserters.fromValue(body))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (response) -> response.createException()
                        .flatMap(e -> Mono.error(new GiteaApiException(onException, e.getMessage()))))
                .bodyToMono(parameterizedTypeReference)
                .switchIfEmpty(Mono.error(new GiteaApiException(onNull, "Result body is null")));
    }

    private Mono<?> sendApiRequest(
            String path,
            HttpMethod httpMethod,
            ParameterizedTypeReference<?> parameterizedTypeReference,
            LogMessage onNull,
            LogMessage onException) {
        return webClient
                .method(httpMethod)
                .uri(path)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (response) -> response.createException()
                        .flatMap(e -> Mono.error(new GiteaApiException(onException, e.getMessage()))))
                .bodyToMono(parameterizedTypeReference)
                .switchIfEmpty(Mono.error(new GiteaApiException(onNull, "Result body is null")));
    }

    private <T> Mono<Void> sendApiRequest(T body, String path, HttpMethod httpMethod, LogMessage onException) {
        return webClient
                .method(httpMethod)
                .uri(path)
                .body(BodyInserters.fromValue(body))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (response) -> response.createException()
                        .flatMap(e -> Mono.error(new GiteaApiException(onException, e.getMessage()))))
                .bodyToMono(Void.class);
    }

    private Mono<Void> sendApiRequest(String path, HttpMethod httpMethod, LogMessage onException) {
        return webClient
                .method(httpMethod)
                .uri(path)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (response) -> response.createException()
                        .flatMap(e -> Mono.error(new GiteaApiException(onException, e.getMessage()))))
                .bodyToMono(Void.class);
    }

    public Mono<GiteaUser> createUser(CreateUserOption createUserOption) {
        return (Mono<GiteaUser>) sendApiRequest(
                createUserOption,
                ADMIN_USERS_PATH,
                HttpMethod.POST,
                new ParameterizedTypeReference<GiteaUser>() {},
                LogMessage.ALERT_20014,
                LogMessage.ALERT_20015);
    }

    public Mono<Void> createRepository(String owner, CreateRepoOption createRepoOption) {
        final var path = String.format(ADMIN_USER_REPO_PATH_FORMAT, owner);
        return sendApiRequest(createRepoOption, path, HttpMethod.POST, LogMessage.ALERT_20018);
    }

    public Mono<Void> createFile(String owner, String repo, String filepath, CreateFileOption createFileOption) {
        final var path = String.format(REPO_FILE_PATH_FORMAT, owner, repo, filepath, branchName);
        return sendApiRequest(createFileOption, path, HttpMethod.POST, LogMessage.ALERT_20019);
    }

    public Mono<ContentsResponse> getFile(String owner, String repo, String filepath, String ref) {
        final var path = String.format(REPO_FILE_PATH_FORMAT, owner, repo, filepath, ref);
        return (Mono<ContentsResponse>) sendApiRequest(
                path,
                HttpMethod.GET,
                new ParameterizedTypeReference<ContentsResponse>() {},
                LogMessage.ALERT_20020,
                LogMessage.ALERT_20021);
    }

    public Mono<List<RepoCommit>> getCommits(String owner, String repo, int limit) {
        final var path = String.format(REPO_COMMITS_PATH_FORMAT, owner, repo, branchName, limit);
        return (Mono<List<RepoCommit>>) sendApiRequest(
                path,
                HttpMethod.GET,
                new ParameterizedTypeReference<List<RepoCommit>>() {},
                LogMessage.ALERT_20022,
                LogMessage.ALERT_20023);
    }

    public Mono<Void> editUser(EditUserOption editUserOption) {
        final var path = String.format(ADMIN_USER_PATH_FORMAT, editUserOption.getLoginName());
        return sendApiRequest(editUserOption, path, HttpMethod.PATCH, LogMessage.ALERT_20024);
    }

    public Mono<Void> deleteUser(String username) {
        final var path = String.format(ADMIN_USER_PATH_FORMAT, username);
        return sendApiRequest(path, HttpMethod.DELETE, LogMessage.ALERT_20025);
    }

    public Mono<Void> deleteRepo(String owner, String repo) {
        final var path = String.format(REPO_OWNER_PATH_FORMAT, owner, repo);
        return sendApiRequest(path, HttpMethod.DELETE, LogMessage.ALERT_20026);
    }

    public Mono<SearchResult<Repo>> getReposByUid(int userId) {
        final var path = String.format(REPO_SEARCH_UID_PATH_FORMAT, userId);
        return (Mono<SearchResult<Repo>>) sendApiRequest(
                path,
                HttpMethod.GET,
                new ParameterizedTypeReference<SearchResult<Repo>>() {},
                LogMessage.ALERT_20034,
                LogMessage.ALERT_20035);
    }

    public Mono<GiteaUser> getUserByUid(int userId) {
        final var path = String.format(USERS_SEARCH_UID_PATH_FORMAT, userId);
        final var giteaUserSearchResultsMono = (Mono<SearchResult<GiteaUser>>) sendApiRequest(
                path,
                HttpMethod.GET,
                new ParameterizedTypeReference<SearchResult<GiteaUser>>() {},
                LogMessage.ALERT_20034,
                LogMessage.ALERT_20035);
        return giteaUserSearchResultsMono.flatMap(giteaUserSearchResults -> ofNullable(
                        giteaUserSearchResults.getData().get(0))
                .map(Mono::just)
                .orElseGet(() -> Mono.error(new EmptyOptionalException(LogMessage.ALERT_20069))));
    }

    public Mono<Void> createHook(String owner, String repo) {
        final var path = String.format(REPOS_OWNER_REPO_HOOKS_PATH_FORMAT, owner, repo);
        final var fullHookUrl = hookServerUrl + HOOK_PATH;
        final var hookConfig = new CreateHookOptionConfig(HOOK_CONTENT_TYPE, fullHookUrl);
        final var hook = new CreateHookOption(hookConfig, HOOK_EVENT_TYPES, HOOK_TYPE, HOOK_ACTIVE);
        return sendApiRequest(hook, path, HttpMethod.POST, LogMessage.ALERT_20049);
    }

    public Mono<Void> createAdminUser() {
        final var requestBody = new CreateUserOption(adminUsername, adminPassword, adminEmail).asMultiValueMap();
        return webClient
                .method(HttpMethod.POST)
                .uri(USER_SIGN_UP_PATH)
                .headers(httpHeaders -> httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED))
                .body(BodyInserters.fromValue(requestBody))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (response) -> response.createException()
                        .flatMap(e -> Mono.error(new GiteaApiException(LogMessage.ALERT_20075, e.getMessage()))))
                .bodyToMono(Void.class);
    }

    public Mono<AccessToken> getAdminUserToken() {
        return webClient
                .post()
                .uri(String.format(USERS_TOKENS_PATH_FORMAT, adminUsername))
                .headers(httpHeaders -> {
                    httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                    httpHeaders.add(
                            HttpHeaders.AUTHORIZATION, getBasicAuthenticationHeader(adminUsername, adminPassword));
                })
                .body(BodyInserters.fromValue(new CreateAccessTokenOption(ADMIN_ACCESS_TOKEN_NAME)))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (response) -> response.createException()
                        .flatMap(e -> Mono.error(new GiteaApiException(LogMessage.ALERT_20080, e.getMessage()))))
                .bodyToMono(AccessToken.class)
                .switchIfEmpty(Mono.error(new GiteaApiException(LogMessage.ALERT_20081, "Result body is null")));
    }

    private static String getBasicAuthenticationHeader(String username, String password) {
        String valueToEncode = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }
}
