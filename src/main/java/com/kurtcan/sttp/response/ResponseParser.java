package com.kurtcan.sttp.response;

import com.kurtcan.sttp.SttpVersion;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
public class ResponseParser {
    static final int MIN_PARTS = 3;
    static final int VERSION_INDEX = 0;
    static final int STATUS_INDEX = 1;
    static final int TYPE_INDEX = 2;
    static final int CONTENT_INDEX = MIN_PARTS;
    static final String DELIMITER = "<|>";

    public static Optional<Response> deserialize(final String responseStr) {
        var parts = responseStr.split(Pattern.quote(DELIMITER));
        if (parts.length < MIN_PARTS) {
            log.info("Invalid response: {}", responseStr);
            return Optional.empty();
        }

        var version = SttpVersion.fromString(parts[VERSION_INDEX]);
        if (version.isEmpty()) {
            log.info("Invalid version: {}", parts[VERSION_INDEX]);
            return Optional.empty();
        }

        var success = ResponseStatus.fromString(parts[STATUS_INDEX]);
        if (success.isEmpty()) {
            log.info("Invalid status: {}", parts[STATUS_INDEX]);
            return Optional.empty();
        }

        var type = Optional.of(parts[TYPE_INDEX]);
        if (type.get().isBlank()) {
            log.info("Invalid type: {}", parts[TYPE_INDEX]);
            return Optional.empty();
        }

        String content = null;
        if (parts.length > CONTENT_INDEX) {
            try {
                content = collectContent(parts);
            } catch (Exception e) {
                log.error("Error instantiating content class", e);
                return Optional.empty();
            }
        }

        var response = Response.builder()
                .version(version.get())
                .status(success.get())
                .type(type.get())
                .content(content)
                .build();

        return Optional.of(response);
    }

    public static String serialize(final Response response) {
        StringBuilder message = new StringBuilder();
        message.append(response.getVersion().getName())
                .append(DELIMITER)
                .append(response.getStatus().getName())
                .append(DELIMITER)
                .append(response.getType());

        response.getContent().ifPresent(content -> message.append(DELIMITER).append(content));

        return message.toString();
    }

    private static String collectContent(final String[] parts) {
        var contentBuilder = new StringBuilder();
        for (int i = CONTENT_INDEX; i < parts.length; i++) {
            contentBuilder.append(parts[i]);
            if (i < parts.length - 1) {
                contentBuilder.append(DELIMITER);
            }
        }
        return contentBuilder.toString();
    }
}
