package org.apache.causeway.viewer.restfulobjects.client.auth.basic;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class BasicCreds {
    private final String username;
    private final String password;
}
