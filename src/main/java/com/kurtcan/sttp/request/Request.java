package com.kurtcan.sttp.request;

import com.kurtcan.sttp.SttpVersion;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.Optional;
import java.util.UUID;

@Data
@Builder
public class Request {

    @NonNull
    @Builder.Default
    private SttpVersion version = SttpVersion.V1;

    private String type;

    private UUID correlationId;

    private String content;

    public Optional<String> getContent() {
        return Optional.ofNullable(content);
    }

}

