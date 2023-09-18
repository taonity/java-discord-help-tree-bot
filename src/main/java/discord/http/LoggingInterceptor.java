package discord.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

@Slf4j
public class LoggingInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest req, byte[] reqBody, ClientHttpRequestExecution ex)
            throws IOException {
        log.debug("Request headers: {}", req.getHeaders());
        log.debug("Request body: {}", new String(reqBody, StandardCharsets.UTF_8));

        final var response = ex.execute(req, reqBody);
        final var inputStreamReader = new InputStreamReader(response.getBody(), StandardCharsets.UTF_8);
        final var body = new BufferedReader(inputStreamReader).lines().collect(Collectors.joining("\n"));

        log.debug("Response headers: {}", response.getHeaders());
        log.debug("Response body: {}", body);

        return response;
    }
}
