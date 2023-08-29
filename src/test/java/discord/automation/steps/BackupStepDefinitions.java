package discord.automation.steps;

import static org.assertj.core.api.Assertions.assertThat;

import discord.automation.utils.CommandExecutor;
import io.cucumber.java.en.Then;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class BackupStepDefinitions {
    static final String LIST_OF_BACKUPS_SCRIPT_PATH = "target/docker/test/backup/restore/scripts/ls.sh";
    static final String BACKUP_MAKING_SCRIPT_PATH = "target/docker/test/backup/make/scripts/make.sh";

    @Then("Run backup making")
    public void runBackupMaking() {
        final var commandExecutor = new CommandExecutor();
        final var backupListBeforeBackup = retrieveBackupList(commandExecutor);

        final var outputLogs = commandExecutor.executeCommand(BACKUP_MAKING_SCRIPT_PATH);
        log.info("Run backup making logs: {}", outputLogs);

        final var backupListSizeAfterBackup =
                retrieveBackupList(commandExecutor).size();
        final var backupListSizeBeforeBackup = backupListBeforeBackup.size();
        final var backupQuantityAdded = backupListSizeAfterBackup - backupListSizeBeforeBackup;

        if (backupListBeforeBackup.isEmpty()) {
            assertThat(backupQuantityAdded).isEqualTo(2);
        } else {
            assertThat(backupQuantityAdded).isEqualTo(1);
        }
        assertThat(outputLogs)
                .contains(
                        "java-discord-help-bot-db-1",
                        "java-discord-help-bot-gitea-1",
                        "Finish dumping in file dump.zip",
                        "Finished running backup tasks.");
    }

    private static List<String> retrieveBackupList(CommandExecutor commandExecutor) {
        final var backupListString = commandExecutor.executeCommand(LIST_OF_BACKUPS_SCRIPT_PATH);
        final var backupListWithDummies = new ArrayList<>(Arrays.asList(backupListString.split("\n")));
        return backupListWithDummies.stream().filter(item -> !item.isEmpty()).toList();
    }

    @Then("Run last backup restoring")
    public void runLastBackupRestoring() {
        final var commandExecutor = new CommandExecutor();
        final var lastBackupRestoringScript =
                "docker compose -f target/docker/test/docker-compose-test.yml exec restore-backup runner backup.latest.tar.gz";
        final var outputLogs = commandExecutor.executeCommand(lastBackupRestoringScript);

        assertThat(outputLogs)
                .contains("Gitea successfully restored!", "Finish execution", "Finish execution of after-all.sh");
    }
}
