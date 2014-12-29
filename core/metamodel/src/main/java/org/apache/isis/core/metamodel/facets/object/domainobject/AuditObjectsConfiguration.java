package org.apache.isis.core.metamodel.facets.object.domainobject;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facets.object.audit.AuditableFacetAbstract;

public enum AuditObjectsConfiguration {
    ALL,
    NONE;

    private static final String AUDIT_OBJECTS_KEY = "isis.services.audit.objects";

    public static AuditObjectsConfiguration parse(IsisConfiguration configuration) {
        final String configuredValue = configuration.getString(AUDIT_OBJECTS_KEY);
        return AuditObjectsConfiguration.parse(configuredValue);
    }

    private static AuditObjectsConfiguration parse(final String value) {
        if ("all".equalsIgnoreCase(value)) {
            return ALL;
        } else {
            return NONE;
        }
    }

    public AuditableFacetAbstract.Enablement asEnablement() {
        return this == ALL? AuditableFacetAbstract.Enablement.ENABLED: AuditableFacetAbstract.Enablement.DISABLED;
    }
}
