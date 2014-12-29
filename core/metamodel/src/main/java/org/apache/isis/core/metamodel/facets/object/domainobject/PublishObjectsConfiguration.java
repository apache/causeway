package org.apache.isis.core.metamodel.facets.object.domainobject;

import org.apache.isis.core.commons.config.IsisConfiguration;

public enum PublishObjectsConfiguration {
    ALL,
    NONE;

    private static final String PUBLISH_OBJECTS_KEY = "isis.services.publish.objects";

    public static PublishObjectsConfiguration parse(IsisConfiguration configuration) {
        final String configuredValue = configuration.getString(PUBLISH_OBJECTS_KEY);
        return PublishObjectsConfiguration.parse(configuredValue);
    }

    private static PublishObjectsConfiguration parse(final String value) {
        if ("all".equalsIgnoreCase(value)) {
            return ALL;
        } else {
            return NONE;
        }
    }

}
