package org.apache.isis.core.metamodel.facets.object.domainobject;

import org.apache.isis.core.commons.config.IsisConfiguration;

public enum EditingObjectsConfiguration {
    ALL,
    NONE;

    private static final String EDIT_OBJECTS_KEY = "isis.services.editing.objects";

    public static EditingObjectsConfiguration parse(IsisConfiguration configuration) {
        final String configuredValue = configuration.getString(EDIT_OBJECTS_KEY);
        return EditingObjectsConfiguration.parse(configuredValue);
    }

    private static EditingObjectsConfiguration parse(final String value) {
        if ("all".equalsIgnoreCase(value)) {
            return ALL;
        } else {
            return NONE;
        }
    }
}
