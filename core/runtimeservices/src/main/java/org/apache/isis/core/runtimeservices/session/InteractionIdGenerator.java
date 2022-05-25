package org.apache.isis.core.runtimeservices.session;

import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class InteractionIdGenerator {

    public UUID interactionId() {
        return UUID.randomUUID();
    }

}
