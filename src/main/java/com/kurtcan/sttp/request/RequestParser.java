package com.kurtcan.sttp.request;

import com.kurtcan.sttp.SttpVersion;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
public class RequestParser {
    static final int MIN_PARTS = 2;
    static final int VERSION_INDEX = 0;
    static final int TYPE_INDEX = 1;
    static final int CONTENT_INDEX = MIN_PARTS;
    static final String DELIMITER = "<|>";

    public static Optional<Request> deserialize(final String requestStr) {
        var parts = requestStr.split(Pattern.quote(DELIMITER));
        if (parts.length < MIN_PARTS) {
            log.info("Invalid request: {}", requestStr);
            return Optional.empty();
        }

        var version = SttpVersion.fromString(parts[VERSION_INDEX]);
        if (version.isEmpty()) {
            log.info("Invalid version: {}", parts[VERSION_INDEX]);
            return Optional.empty();
        }

        var type = Optional.of(parts[TYPE_INDEX]);
        if (type.get().isBlank()) {
            log.info("Invalid type: {}", parts[TYPE_INDEX]);
            return Optional.empty();
        }

        String content = null;
        if (parts.length > MIN_PARTS) {
            try {
                content = collectContent(parts);
            } catch (Exception e) {
                log.error("Error instantiating content class", e);
                return Optional.empty();
            }
        }

        var request = Request.builder()
                .version(version.get())
                .type(type.get())
                .content(content)
                .build();

        return Optional.of(request);
    }

    public static String serialize(final Request request) {
        StringBuilder message = new StringBuilder();
        message.append(request.getVersion().getName())
                .append(DELIMITER)
                .append(request.getType());

        request.getContent().ifPresent(content -> message.append(DELIMITER).append(content));

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
