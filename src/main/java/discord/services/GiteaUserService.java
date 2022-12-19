package discord.services;

import discord.dao.gitea.api.*;
import discord.exception.*;
import discord.localisation.LogMessage;
import discord.model.GuildSettings;
import discord.repository.GuildSettingsRepository;
import discord.structure.EmbedBuilder;
import discord.structure.EmbedType;
import discord.structure.NodeAndError;
import discord.tree.Node;
import discord.utils.PassayPasswordGenerator;
import discord.utils.ResourceFileLoader;
import discord.utils.YamlStringToNodeConvertor;
import discord.utils.validation.RootValidator;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Optional;

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

    public void createUser(String guildId) {
        final var userName = String.format(USER_NAME_FORMAT, guildId);
        final var repoName = String.format(REPO_NAME_FORMAT, guildId);
        final var fileContent = ResourceFileLoader.loadFile(FILE_NAME);

        final var userId = createGiteaUser(userName);
        createGiteaRepo(userName, repoName);
        createFile(userName, repoName, fileContent);
        giteaApiService.createHook(userName, repoName);

        guildSettingsRepository.updateGiteaUserId(guildId, userId);
    }

    public EditUserOption resetPassword(String guildId) {
        final var editUserOption = guildSettingsRepository.findGuildSettingById(guildId)
                .map(GuildSettings::getGiteaUserId)
                .map(giteaApiService::getUserByUid)
                .map(giteaUser -> new EditUserOption(giteaUser.getUsername(), PassayPasswordGenerator.generate()))
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20069));

        giteaApiService.editUser(editUserOption);

        return editUserOption;
    }

    public void deleteUser(String guildId) {
        final var giteaUserId = guildSettingsRepository.findGuildSettingById(guildId)
                .map(GuildSettings::getGiteaUserId)
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20030));

        final var userName = giteaApiService
                .getUsers()
                .stream()
                .filter(user -> user.getId() == giteaUserId)
                .map(GiteaUser::getUsername)
                .findFirst()
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20032));

        final var reposByUid = giteaApiService.getReposByUid(giteaUserId);

        if(!reposByUid.isOk()) {
            throw new FailedToSearchRepoException(LogMessage.ALERT_20036);
        }

        reposByUid.getData()
                .stream()
                .filter(repo -> repo.getOwner().getId() == giteaUserId)
                .map(Repo::getName)
                .forEach(repoName -> giteaApiService.deleteRepo(userName, repoName));

        giteaApiService.deleteUser(userName);
    }

    public NodeAndError getDialogRoot(String guildId) {
        final var userName = String.format(USER_NAME_FORMAT, guildId);
        final var repoName = String.format(REPO_NAME_FORMAT, guildId);

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
            new CorruptTreeRootException(e.getMessage()).printStackTrace();
        }

        if(!commitIterator.hasNext()) {
            throw new TreeRootNoValidCommitsException(LogMessage.ALERT_20048);
        }

        final var nodeOpt = searchForValidNode(commitIterator, userName, repoName);
        if(nodeOpt.isEmpty()) {
            gitApiService.squashCommits(userName, repoName, FAILURE_TREE_BUILDING_ATTEMPTS_LIMIT);

            return searchForValidNode(commitIterator, userName, repoName)
                    .map(nodeAndErrorBuilder::node)
                    .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20029))
                    .build();
        } else {
            return nodeAndErrorBuilder.node(nodeOpt.get()).build();
        }

    }

    private Optional<Node> searchForValidNode(Iterator<RepoCommit> commitIterator, String userName, String repoName) {
        for (var i = 0; (i < COMMIT_QUANTITY_IN_BUNCH) && commitIterator.hasNext(); i++) {
            try {
                return Optional.of(getRootFromByCommit(commitIterator.next(), userName, repoName));
            } catch (YamlProcessingException | TreeRootValidationException e) {
                log.warn(LogMessage.ALERT_20046.name());
            }
        }
        return Optional.empty();
    }

    private Node getRootFromByCommit(RepoCommit commit, String userName, String repoName) {
        final var yamlFile = giteaApiService.getFile(userName, repoName, FILE_NAME, commit.getSha());
        final var node = YamlStringToNodeConvertor.convert(yamlFile.getContentAsString());
        final var messageCollector = RootValidator.validate(node, gatewayDiscordClient);

        if(messageCollector.isEmpty()) {
            return node;
        } else {
            throw new TreeRootValidationException(LogMessage.ALERT_20043, messageCollector.getErrorsAsString());
        }
    }

    private Integer createGiteaUser(String userName) {
        final var email = String.format(EMAIL_FORMAT, userName);
        final var password = PassayPasswordGenerator.generate();
        final var user = new CreateUserOption(userName, password, email);
        return giteaApiService.createUser(user).getId();
    }

    private void createGiteaRepo(String userName, String repoName) {
        final var repo = new CreateRepoOption(repoName);
        giteaApiService.createRepository(userName, repo);
    }

    private void createFile(String userName, String repoName, String content) {
        final var file = new CreateFileOption(content);
        giteaApiService.createFile(userName, repoName, FILE_NAME, file);
    }
}
