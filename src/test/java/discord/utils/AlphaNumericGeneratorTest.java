package discord.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class AlphaNumericGeneratorTest {

    @Test
    @Disabled
    void generateFourCharFromNumber() {
        final var numberAlphanumericMap = new HashMap<Integer, String>() {
            {
                put(100, "WtXF");
                put(101, "XtXF");
                put(102, "YtXF");
                put(103, "ZtXF");
            }
        };

        numberAlphanumericMap.forEach((number, expectedAn) -> {
            final var actualAn = AlphaNumericGenerator.generateFourCharFromNumber(number);
            assertThat(actualAn).isEqualTo(expectedAn);
        });
    }
}
