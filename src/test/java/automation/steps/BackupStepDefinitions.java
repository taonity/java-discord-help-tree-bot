package automation.steps;

import static org.assertj.core.api.Assertions.assertThat;

import automation.runners.AbstractContainerRunner;
import io.cucumber.java.en.Then;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.Container;

@Slf4j
@RequiredArgsConstructor
public class BackupStepDefinitions {

    @Then("Run backup making")
    public void runBackupMaking() {
        final var backupListBeforeBackup = retrieveBackupList();
        final var outputLogs = execBackupMaking();
        log.info("Run backup making logs: {}", outputLogs);

        final var backupListSizeAfterBackup = retrieveBackupList().size();
        final var backupListSizeBeforeBackup = backupListBeforeBackup.size();
        final var backupQuantityAdded = backupListSizeAfterBackup - backupListSizeBeforeBackup;

        if (backupListBeforeBackup.isEmpty()) {
            assertThat(backupQuantityAdded).isEqualTo(2);
        } else {
            assertThat(backupQuantityAdded).isEqualTo(1);
        }
        assertThat(outputLogs)
                .contains("db_1", "gitea_1", "Finish dumping in file dump.zip", "Finished running backup tasks.");
    }

    private static String execBackupMaking() {
        final var execResult = AbstractContainerRunner.execCommandOnService(
                "make-backup", "/bin/sh", "-c", "export VERSION=\"1.0.0\" && backup");
        return getOutputAndErrorLogs(execResult);
    }

    private static String execBackupRestoring() {
        final var execResult = AbstractContainerRunner.execCommandOnService(
                "restore-backup", "bash", "-c", "runner backup.latest.tar.gz");
        return getOutputAndErrorLogs(execResult);
    }

    private static String getOutputAndErrorLogs(Container.ExecResult execResult) {
        return String.format("%s\n%s", execResult.getStdout(), execResult.getStderr());
    }

    private static List<String> retrieveBackupList() {
        final var execResult =
                AbstractContainerRunner.execCommandOnService("restore-backup", "bash", "-c", "ls -1 /archive");

        assertThat(execResult.getStderr()).isEmpty();

        final var backupListString = execResult.getStdout();
        final var backupListWithDummies = new ArrayList<>(Arrays.asList(backupListString.split("\n")));
        return backupListWithDummies.stream().filter(item -> !item.isEmpty()).toList();
    }

    @Then("Run last backup restoring")
    public void runLastBackupRestoring() {
        final var outputLogs = execBackupRestoring();

        assertThat(outputLogs)
                .contains("Gitea successfully restored!", "Finish execution", "Finish execution of after-all.sh");
    }
}
