package org.taonity.helpbot.discord.event.command.gitea.services;

import discord4j.core.GatewayDiscordClient;
import java.time.Duration;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.taonity.helpbot.discord.GuildSettings;
import org.taonity.helpbot.discord.GuildSettingsRepository;
import org.taonity.helpbot.discord.event.command.AlphaNumericGenerator;
import org.taonity.helpbot.discord.event.command.gitea.PassayPasswordGenerator;
import org.taonity.helpbot.discord.event.command.gitea.ResourceFileLoader;
import org.taonity.helpbot.discord.event.command.gitea.YamlStringToNodeConvertor;
import org.taonity.helpbot.discord.event.command.gitea.api.*;
import org.taonity.helpbot.discord.event.command.gitea.validation.RootValidator;
import org.taonity.helpbot.discord.event.command.tree.model.Node;
import org.taonity.helpbot.discord.event.command.tree.model.NodeAndError;
import org.taonity.helpbot.discord.logging.LogMessage;
import org.taonity.helpbot.discord.logging.exception.NoCommitsException;
import org.taonity.helpbot.discord.logging.exception.TreeRootValidationException;
import org.taonity.helpbot.discord.logging.exception.YamlProcessingException;
import org.taonity.helpbot.discord.logging.exception.client.FailedToSearchRepoException;
import org.taonity.helpbot.discord.logging.exception.client.TreeRootNoValidCommitsException;
import org.taonity.helpbot.discord.logging.exception.main.EmptyOptionalException;
import org.taonity.helpbot.discord.mdc.OnCompleteSignalListenerBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class GiteaUserService {
    public static final String USER_NAME_FORMAT = "user_%s";
    private static final String EMAIL_FORMAT = "%s@helpbot.com";
    public static final String REPO_NAME_FORMAT = "repo_%s";
    public static final String FILE_NAME = "dialog-starter.yaml";
    private static final int FAILURE_TREE_BUILDING_ATTEMPTS_LIMIT = 5;
    private static final int COMMIT_QUANTITY_IN_BUNCH = FAILURE_TREE_BUILDING_ATTEMPTS_LIMIT - 1;
    private static final int COMMIT_QUANTITY_TO_RETRIEVE = COMMIT_QUANTITY_IN_BUNCH * 2 + 1;

    private final GiteaApiService giteaApiService;
    private final GuildSettingsRepository guildSettingsRepository;
    private final GatewayDiscordClient gatewayDiscordClient;
    private final GitApiService gitApiService;

    public Mono<Void> createUser(int guildSettingsId) {
        final var giteaUserAlphaNumeric = AlphaNumericGenerator.generateFourCharFromNumber(guildSettingsId);
        final var userName = String.format(USER_NAME_FORMAT, giteaUserAlphaNumeric);
        final var repoName = String.format(REPO_NAME_FORMAT, giteaUserAlphaNumeric);
        final var fileContent = ResourceFileLoader.loadFile(FILE_NAME);

        final var createGiteaUserRepo = createGiteaRepo(userName, repoName)
                .then(createFile(userName, repoName, fileContent))
                .delayElement(Duration.ofMillis(1500))
                .then(giteaApiService.createHook(userName, repoName));

        return createGiteaUser(userName)
                .flatMap(userId -> Mono.when(
                        createGiteaUserRepo,
                        guildSettingsRepository.updateGiteaUser(guildSettingsId, userId, giteaUserAlphaNumeric)));
    }

    public Mono<EditUserOption> resetPassword(String guildId) {
        final var editUserOption = guildSettingsRepository
                .findGuildSettingByGuildId(guildId)
                .switchIfEmpty(Mono.error(new EmptyOptionalException(LogMessage.ALERT_20069)))
                .map(GuildSettings::getGiteaUserId)
                .flatMap(giteaApiService::getUserByUid)
                .map(GiteaUser::getUsername)
                .map(giteaUserName -> new EditUserOption(giteaUserName, PassayPasswordGenerator.generate()));

        return editUserOption.flatMap(
                editUserOption1 -> giteaApiService.editUser(editUserOption1).thenReturn(editUserOption1));
    }

    public Mono<Void> deleteUser(String guildId) {
        return guildSettingsRepository
                .findGuildSettingByGuildId(guildId)
                .switchIfEmpty(Mono.error(new EmptyOptionalException(LogMessage.ALERT_20030)))
                .map(GuildSettings::getGiteaUserId)
                .flatMap(giteaApiService::getUserByUid)
                .map(GiteaUser::getId)
                .flatMap(giteaUserId -> giteaApiService
                        .getUserByUid(giteaUserId)
                        .map(GiteaUser::getUsername)
                        .zipWith(giteaApiService.getReposByUid(giteaUserId), (userName, repos) -> {
                            if (!repos.isOk()) {
                                return Mono.error(new FailedToSearchRepoException(LogMessage.ALERT_20036, guildId));
                            }

                            final var deleteRepoMonos = repos.getData().stream()
                                    .filter(repo -> repo.getOwner().getId() == giteaUserId)
                                    .map(Repo::getName)
                                    .map(repoName -> giteaApiService.deleteRepo(userName, repoName))
                                    .toList();
                            // TODO ???
                            return Flux.fromIterable(deleteRepoMonos)
                                    .then()
                                    .then(giteaApiService.getReposByUid(giteaUserId));
                        }))
                .then();
    }

    public Mono<NodeAndError> getDialogRoot(GuildSettings guildSettings) {
        final var giteaUserAlphaNumeric = AlphaNumericGenerator.generateFourCharFromNumber(guildSettings.getId());
        final var userName = String.format(USER_NAME_FORMAT, giteaUserAlphaNumeric);
        final var repoName = String.format(REPO_NAME_FORMAT, giteaUserAlphaNumeric);

        return giteaApiService
                .getCommits(userName, repoName, COMMIT_QUANTITY_TO_RETRIEVE)
                .filter(commits -> !commits.isEmpty())
                .switchIfEmpty(Mono.error(new NoCommitsException(LogMessage.ALERT_20041)))
                .flatMap(commits -> {
                    final var commitIterator = commits.iterator();
                    final var nodeAndErrorBuilder = NodeAndError.builder();
                    return getRootNodeByCommit(commitIterator.next(), userName, repoName)
                            .onErrorResume(e -> {
                                if (e instanceof YamlProcessingException || e instanceof TreeRootValidationException) {
                                    nodeAndErrorBuilder.errorMessage(e.getMessage());
                                    return Mono.<Void>empty()
                                            .tap(OnCompleteSignalListenerBuilder.of(
                                                    () -> log.info(LogMessage.ALERT_20047.name())))
                                            .then(Mono.defer(() -> {
                                                if (!commitIterator.hasNext()) {
                                                    return Mono.error(new TreeRootNoValidCommitsException(
                                                            LogMessage.ALERT_20048, guildSettings.getGuildId()));
                                                }
                                                return searchForValidNode(commitIterator, userName, repoName)
                                                        .switchIfEmpty(Mono.defer(() -> {
                                                            gitApiService
                                                                    .reactiveSquashCommit(
                                                                            userName,
                                                                            repoName,
                                                                            FAILURE_TREE_BUILDING_ATTEMPTS_LIMIT,
                                                                            guildSettings.getGuildId())
                                                                    .subscribe();
                                                            return searchForValidNode(
                                                                            commitIterator, userName, repoName)
                                                                    .switchIfEmpty(Mono.error(
                                                                            new EmptyOptionalException(
                                                                                    LogMessage.ALERT_20029)));
                                                        }));
                                            }));
                                } else {
                                    return Mono.error(e);
                                }
                            })
                            .map(node -> nodeAndErrorBuilder.node(node).build());
                });
    }

    private Mono<Node> searchForValidNode(Iterator<RepoCommit> commitIterator, String userName, String repoName) {
        final Iterable<RepoCommit> commitIterable = () -> commitIterator;
        return Flux.fromIterable(commitIterable)
                .take(COMMIT_QUANTITY_IN_BUNCH)
                .flatMap(commit -> Mono.just(commit)
                        .flatMap(commit1 -> getRootNodeByCommit(commit1, userName, repoName))
                        .onErrorResume(
                                e -> e instanceof YamlProcessingException || e instanceof TreeRootValidationException,
                                e -> Mono.<Node>empty()
                                        .tap(OnCompleteSignalListenerBuilder.of(
                                                () -> log.warn(LogMessage.ALERT_20046.name())))))
                .flatMap(commit -> Mono.just(commit).cache())
                .filter(Objects::nonNull)
                .take(1)
                .singleOrEmpty();
    }

    private Mono<Node> getRootNodeByCommit(RepoCommit commit, String userName, String repoName) {
        return giteaApiService
                .getFile(userName, repoName, FILE_NAME, commit.getSha())
                .flatMap(yamlFile -> {
                    try {
                        return Mono.just(YamlStringToNodeConvertor.convert(yamlFile.getContentAsString()));
                    } catch (YamlProcessingException e) {
                        return Mono.error(e);
                    }
                })
                .flatMap(node -> RootValidator.validate(node, gatewayDiscordClient)
                        .flatMap(stringErrorMessageCollector -> {
                            if (stringErrorMessageCollector.isEmpty()) {
                                return Mono.just(node);
                            } else {
                                return Mono.error(new TreeRootValidationException(
                                        LogMessage.ALERT_20043, stringErrorMessageCollector.getErrorsAsString()));
                            }
                        }));
    }

    private Mono<Integer> createGiteaUser(String userName) {
        final var email = String.format(EMAIL_FORMAT, userName);
        final var password = PassayPasswordGenerator.generate();
        final var user = new CreateUserOption(userName, password, email);
        return giteaApiService.createUser(user).map(GiteaUser::getId);
    }

    private Mono<Void> createGiteaRepo(String userName, String repoName) {
        final var repo = new CreateRepoOption(repoName);
        return giteaApiService.createRepository(userName, repo);
    }

    private Mono<Void> createFile(String userName, String repoName, String content) {
        final var file = new CreateFileOption(content);
        return giteaApiService.createFile(userName, repoName, FILE_NAME, file);
    }
}
