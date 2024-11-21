package com.kurtcan.sttp.response;

import com.kurtcan.sttp.SttpVersion;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.Optional;

@Data
@Builder
public class Response {

    @NonNull
    @Builder.Default
    private SttpVersion version = SttpVersion.V1;

    private ResponseStatus status;

    private String type;

    private String content;

    public Optional<String> getContent() {
        return Optional.ofNullable(content);
    }

    public static Response success(String type, String content) {
        return Response.builder()
                .status(ResponseStatus.SUCCESS)
                .type(type)
                .content(content)
                .build();
    }

    public static Response failure(String type, String content) {
        return Response.builder()
                .status(ResponseStatus.FAILURE)
                .type(type)
                .content(content)
                .build();
    }

}
