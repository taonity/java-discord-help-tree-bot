package discord.services;

import discord.dto.gitea.api.*;
import discord.exception.*;
import discord.exception.client.FailedToSearchRepoException;
import discord.exception.client.TreeRootNoValidCommitsException;
import discord.exception.main.EmptyOptionalException;
import discord.exception.main.MainInterruptedException;
import discord.logging.LogMessage;
import discord.model.GuildSettings;
import discord.repository.GuildSettingsRepository;
import discord.structure.NodeAndError;
import discord.tree.Node;
import discord.utils.AlphaNumericGenerator;
import discord.utils.PassayPasswordGenerator;
import discord.utils.ResourceFileLoader;
import discord.utils.YamlStringToNodeConvertor;
import discord.utils.validation.RootValidator;
import discord4j.core.GatewayDiscordClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class GiteaUserService {
    public final static String USER_NAME_FORMAT = "user_%s";
    private final static String EMAIL_FORMAT = "%s@helpbot.com";
    public final static String REPO_NAME_FORMAT = "repo_%s";
    public final static String FILE_NAME = "dialog-starter.yaml";
    private final static int FAILURE_TREE_BUILDING_ATTEMPTS_LIMIT = 5;
    private final static int COMMIT_QUANTITY_IN_BUNCH = FAILURE_TREE_BUILDING_ATTEMPTS_LIMIT - 1;
    private final static int COMMIT_QUANTITY_TO_RETRIEVE = COMMIT_QUANTITY_IN_BUNCH * 2 + 1;

    private final GiteaApiService giteaApiService;
    private final GuildSettingsRepository guildSettingsRepository;
    private final GatewayDiscordClient gatewayDiscordClient;
    private final GitApiService gitApiService;

    public void createUser(int guildSettingsId) throws GiteaApiException {
        final var giteaUserAlphaNumeric = AlphaNumericGenerator.generateFourCharFromNumber(guildSettingsId);
        final var userName = String.format(USER_NAME_FORMAT, giteaUserAlphaNumeric);
        final var repoName = String.format(REPO_NAME_FORMAT, giteaUserAlphaNumeric);
        final var fileContent = ResourceFileLoader.loadFile(FILE_NAME);

        final var userId = createGiteaUser(userName);
        createGiteaRepo(userName, repoName);
        createFile(userName, repoName, fileContent);

        try {
            // TODO: For some reason giteaApiService.createFile is synchronous, so a delay needed before applying webhook
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new MainInterruptedException(LogMessage.ALERT_20083, e);
        }

        giteaApiService.createHook(userName, repoName);

        guildSettingsRepository.updateGiteaUser(guildSettingsId, userId, giteaUserAlphaNumeric);
    }

    public EditUserOption resetPassword(String guildId) throws GiteaApiException {
        final var giteaUseID = guildSettingsRepository.findGuildSettingByGuildId(guildId)
                .map(GuildSettings::getGiteaUserId)
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20069));

        final var giteaUserName = giteaApiService.getUserByUid(giteaUseID).getUsername();
        final var giteaUserOption = new EditUserOption(giteaUserName, PassayPasswordGenerator.generate());

        giteaApiService.editUser(giteaUserOption);
        return giteaUserOption;
    }

    public void deleteUser(String guildId) throws GiteaApiException {
        final var giteaUserId = guildSettingsRepository.findGuildSettingByGuildId(guildId)
                .map(GuildSettings::getGiteaUserId)
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20030));

        final var userName = giteaApiService.getUserByUid(giteaUserId).getUsername();

        final var reposByUid = giteaApiService.getReposByUid(giteaUserId);

        if(!reposByUid.isOk()) {
            throw new FailedToSearchRepoException(LogMessage.ALERT_20036, guildId);
        }

        final var repoNameList = reposByUid.getData()
                .stream()
                .filter(repo -> repo.getOwner().getId() == giteaUserId)
                .map(Repo::getName)
                .collect(Collectors.toList());

        // TODO: Need a way to throw checked exception in forEach
        for(final var repoName: repoNameList) {
            giteaApiService.deleteRepo(userName, repoName);
        }

        giteaApiService.deleteUser(userName);
    }

    public NodeAndError getDialogRoot(GuildSettings guildSettings) throws GiteaApiException, NoCommitsException {
        final var giteaUserAlphaNumeric = AlphaNumericGenerator.generateFourCharFromNumber(guildSettings.getId());
        final var userName = String.format(USER_NAME_FORMAT, giteaUserAlphaNumeric);
        final var repoName = String.format(REPO_NAME_FORMAT, giteaUserAlphaNumeric);

        final var commitList = giteaApiService.getCommits(userName, repoName, COMMIT_QUANTITY_TO_RETRIEVE);

        if(commitList.isEmpty()) {
            throw new NoCommitsException(LogMessage.ALERT_20041);
        }

        final var commitIterator = commitList.iterator();
        final var nodeAndErrorBuilder = NodeAndError.builder();
        try {
            final var node = getRootFromByCommit(commitIterator.next(), userName, repoName);
            return nodeAndErrorBuilder.node(node).build();
        } catch (YamlProcessingException | TreeRootValidationException e) {
            nodeAndErrorBuilder.errorMessage(e.getMessage());
            log.warn(LogMessage.ALERT_20047.name());
        }

        if(!commitIterator.hasNext()) {
            throw new TreeRootNoValidCommitsException(LogMessage.ALERT_20048, guildSettings.getGuildId());
        }

        final var nodeOpt = searchForValidNode(commitIterator, userName, repoName);
        if(nodeOpt.isEmpty()) {
            gitApiService.squashCommits(userName, repoName, FAILURE_TREE_BUILDING_ATTEMPTS_LIMIT, guildSettings.getGuildId());

            return searchForValidNode(commitIterator, userName, repoName)
                    .map(nodeAndErrorBuilder::node)
                    .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20029))
                    .build();
        } else {
            return nodeAndErrorBuilder.node(nodeOpt.get()).build();
        }

    }

    private Optional<Node> searchForValidNode(Iterator<RepoCommit> commitIterator, String userName, String repoName) throws GiteaApiException {
        for (var i = 0; (i < COMMIT_QUANTITY_IN_BUNCH) && commitIterator.hasNext(); i++) {
            try {
                return Optional.of(getRootFromByCommit(commitIterator.next(), userName, repoName));
            } catch (YamlProcessingException | TreeRootValidationException e) {
                log.warn(LogMessage.ALERT_20046.name());
            }
        }
        return Optional.empty();
    }

    private Node getRootFromByCommit(RepoCommit commit, String userName, String repoName) throws GiteaApiException, TreeRootValidationException, YamlProcessingException {
        final var yamlFile = giteaApiService.getFile(userName, repoName, FILE_NAME, commit.getSha());
        final var node = YamlStringToNodeConvertor.convert(yamlFile.getContentAsString());
        final var messageCollector = RootValidator.validate(node, gatewayDiscordClient);

        if(messageCollector.isEmpty()) {
            return node;
        } else {
            throw new TreeRootValidationException(LogMessage.ALERT_20043, messageCollector.getErrorsAsString());
        }
    }

    private Integer createGiteaUser(String userName) throws GiteaApiException {
        final var email = String.format(EMAIL_FORMAT, userName);
        final var password = PassayPasswordGenerator.generate();
        final var user = new CreateUserOption(userName, password, email);
        return giteaApiService.createUser(user).getId();
    }

    private void createGiteaRepo(String userName, String repoName) throws GiteaApiException {
        final var repo = new CreateRepoOption(repoName);
        giteaApiService.createRepository(userName, repo);
    }

    private void createFile(String userName, String repoName, String content) throws GiteaApiException {
        final var file = new CreateFileOption(content);
        giteaApiService.createFile(userName, repoName, FILE_NAME, file);
    }
}
