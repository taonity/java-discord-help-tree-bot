package discord.localisation;

import discord.structure.CommandName;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
            "There is no `help` or/and `log` channel. Please, write `/%s` in desired channel to make it `log` or `help` channel",
            CommandName.CHANNELROLE.getCommandName()
    )),
    COMMAND_IN_WRONG_CHANNEL_MESSAGE_FORMAT("Please, write the command in %s channel"),
    MUST_BE_MODERATOR_MESSAGE_FORMAT("You must have %s role to run this command"),
    GITEA_USER_CREDS_MESSAGE_FORMAT("To update dialog settings you have to [login](%s) into our dialog editor service and edit file by [link](%s).\nUse the credentials to login:\nUsername: **%s**\nPassword: **%s**");

    @Getter
    private final String message;

    public String format(Object... args) {
        return String.format(message, args);
    }
}
