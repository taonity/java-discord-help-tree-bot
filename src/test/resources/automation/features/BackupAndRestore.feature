@DbCleanUp
@GiteaRepoReset
Feature: Backup and restore gitea and db
  Scenario: Backup and restore gitea and db
    Given User channel data is updated in DB
      | guildId             | logChannelId        | helpChannelId      |
      | 448934652992946176  | 1041419228051415200 | 935576925773127710 |
    And Tree config file is updated in user gitea repo
      | guildId             | dialogFile                |
      | 448934652992946176  | dialogs/dialog-small.yaml |
    When Run backup making
    And User channel data is updated in DB
      | guildId             | logChannelId        | helpChannelId      |
      | 448934652992946176  | 1041419228051415200 | 812331010213150724 |
    And Tree config file is updated in user gitea repo
      | guildId             | dialogFile                  |
      | 448934652992946176  | dialogs/dialog-middle.yaml  |
    Then Run last backup restoring
    And User channel data is present in DB
      | guildId             | logChannelId        | helpChannelId      |
      | 448934652992946176  | 1041419228051415200 | 935576925773127710 |
    And Tree config file is present in user gitea repo
      | guildId             | dialogFile                |
      | 448934652992946176  | dialogs/dialog-small.yaml |
