package org.apache.causeway.viewer.restfulobjects.client.auth.oauth2;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class Oauth2Creds {
    private final String tenantId;
    private final String clientId;
    private final String clientSecret;
}
