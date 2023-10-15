package org.taonity.helpbot.discord.localisation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.taonity.helpbot.discord.CommandName;
import org.taonity.helpbot.discord.event.joinleave.service.GuildRoleService;

@RequiredArgsConstructor
public enum SimpleMessage {
    TIP_FOOTER_MESSAGE("This tip is triggered by random message every 1 hour"),
    LOG_FOOTER_MESSAGE("This log is just for information or means that some scenario just broke"),
    EXPECTED_ERROR_MESSAGE("Expected error message"),
    UNEXPECTED_ERROR_MESSAGE("Unexpected error message"),
    INIT_ERROR_MESSAGE("Initialisation error message"),
    SUCCESS_CHANNEL_UPDATE_MESSAGE("Success"),
    FAIL_CHANNEL_UPDATE_MESSAGE("Argument is empty"),
    SUCCESS_DIALOG_UPDATE_MESSAGE("Dialog was successfully updated"),
    FAIL_DIALOG_UPDATE_MESSAGE("Failed to update dialog on last changes. Last successful version will be used"),
    NO_CHANNEL_MESSAGE(String.format(
            "There is no `help` or/and `log` channel or you don't have the moderator role. Please, make sure you have the `%s` role and write `/%s` in desired channels to configure `log` and `help` channels",
            GuildRoleService.MODERATOR_ROLE_NAME, CommandName.CHANNELROLE.getCommandName())),
    COMMAND_IN_WRONG_CHANNEL_MESSAGE_FORMAT("Please, write the command in %s channel"),
    MUST_BE_MODERATOR_MESSAGE_FORMAT("You must have %s role to run this command"),
    GITEA_USER_CREDS_MESSAGE_FORMAT(
            "To update dialog settings you have to [login](%s) into our dialog editor service and edit file by [link](%s).\nUse the credentials to login:\nUsername: **%s**\nPassword: **%s**"),
    ON_GUILD_JOIN_INSTRUCTIONS(String.format(
            "Hi, it seems like you just added me into a guild. Make sure you configure me! \n\nFirst thing apply the `%s` role on yourself so you can configure me. After that you have to give the `help` channel where I will work on and the `log` channel where I will be logging important information - use `/%s` command. \n\nAnd after all you will be able to use the rest my commands! See you in your guild! \n\n[Here](https://www.youtube.com/watch?v=DZdLhIy2Ng4) you can see how I work.",
            GuildRoleService.MODERATOR_ROLE_NAME, CommandName.CHANNELROLE.getCommandName()));

    @Getter
    private final String message;

    public String format(Object... args) {
        return String.format(message, args);
    }
}
