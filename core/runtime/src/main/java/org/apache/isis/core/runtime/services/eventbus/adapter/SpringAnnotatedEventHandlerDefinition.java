package org.apache.isis.core.runtime.services.eventbus.adapter;

import java.lang.reflect.AccessibleObject;
import java.util.Arrays;

import org.axonframework.common.annotation.HandlerDefinition;

class SpringAnnotatedEventHandlerDefinition implements HandlerDefinition<AccessibleObject> {

    public static final SpringAnnotatedEventHandlerDefinition INSTANCE = new SpringAnnotatedEventHandlerDefinition();

    private SpringAnnotatedEventHandlerDefinition() {}

    @Override
    public boolean isMessageHandler(AccessibleObject member) {
        return member != null && Arrays.stream(member.getAnnotations()).anyMatch(annotation ->
                "org.springframework.context.event.EventListener".equals(annotation.annotationType().getName()));
    }

    @Override
    public Class<?> resolvePayloadFor(AccessibleObject member) {
        return null;
    }
}
