Feature: Check guild data presence in Gitea and db after startup

  Scenario: Check if everything is preset
    Then Gitea user id, alphanumeric and dialog file content must match
      | guildId             | giteaUserId | giteaUserAlphaNumeric | dialogFile          |
      | 448934652992946176  | 2           | lsXF                  | dialog-starter.yaml |
      | 837611904267583539  | 3           | msXF                  | dialog-starter.yaml |
