Feature: Check guild data presence in Gitea and db after startup

  Scenario: Check guild data presence in Gitea and db after startup
    Then Guild id is present in DB
      | guildId             |
      | 448934652992946176  |
      | 837611904267583539  |
    Then User gitea data is present in DB
      | giteaUserId | giteaUserAlphaNumeric |
      | 2           | lsXF                  |
      | 3           | msXF                  |
    And Tree config file is present in user gitea repo
      | guildId             | dialogFile          |
      | 448934652992946176  | dialog-starter.yaml |
      | 837611904267583539  | dialog-starter.yaml |
