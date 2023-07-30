package org.apache.causeway.viewer.restfulobjects.client.auth.oauth2.azure;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;

import org.apache.causeway.commons.functional.Railway;

class TokenParser {

    static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    Railway<IOException,TokenSuccessResponse> parseTokenEntity(final String entity) {
        try {
            return Railway.success(OBJECT_MAPPER.readerFor(TokenSuccessResponse.class).readValue(entity));
        } catch (IOException e) {
            return Railway.failure(e);
        }
    }

    @Data
    static
    class TokenSuccessResponse {
        private String token_type;
        private int expires_in;
        private int ext_expires_in;
        private String access_token;
    }
}
