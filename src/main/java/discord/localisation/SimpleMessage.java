package discord.localisation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SimpleMessage {
    TIP_FOOTER_MESSAGE("This tip is triggered by random message every 1 hour"),
    LOG_FOOTER_MESSAGE("This is log means that some scenario just broke"),
    EXPECTED_ERROR_MESSAGE("Expected error message"),
    UNEXPECTED_ERROR_MESSAGE("Unexpected error message"),
    INIT_ERROR_MESSAGE("Initialisation error message"),
    SUCCESS_CHANNEL_UPDATE_MESSAGE("Success"),
    FAIL_CHANNEL_UPDATE_MESSAGE("Argument is empty");

    @Getter
    private final String message;
}
