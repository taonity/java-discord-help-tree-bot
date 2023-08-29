@Run
Feature: Check guild data presence in Gitea and db after startup

  Scenario: Check guild data presence in Gitea and db after startup
    Then User gitea data is present in DB
      | guildId             | giteaUserId | giteaUserAlphaNumeric |
      | 448934652992946176  | 2           | lsXF                  |
      | 837611904267583539  | 3           | msXF                  |
    And Tree config file is present in user gitea repo
      | guildId             | dialogFile          |
      | 448934652992946176  | dialog-starter.yaml |
      | 837611904267583539  | dialog-starter.yaml |
