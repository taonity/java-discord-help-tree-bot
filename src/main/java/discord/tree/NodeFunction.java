package discord.tree;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum NodeFunction {
    @JsonProperty("return_text")
    RETURN_TEXT,

    @JsonProperty("ask_input")
    ASK_INPUT
}
