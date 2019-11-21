package org.isisaddons.module.excel.dom;

import java.util.List;

public class WorksheetContent {

    private final List<?> domainObjects;
    private final WorksheetSpec spec;

    public <T> WorksheetContent(
            final List<T> domainObjects,
            final WorksheetSpec spec) {
        this.domainObjects = domainObjects;
        this.spec = spec;
    }

    public List<?> getDomainObjects() {
        return domainObjects;
    }

    public WorksheetSpec getSpec() {
        return spec;
    }
}
