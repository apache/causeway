package org.apache.isis.extensions.jpa.metamodel.facets.prop.version;

import javax.persistence.Version;

public class SimpleObjectWithVersionAnnotation {


    private Long versionColumn;

    @Version
    public Long getVersionColumn() {
        return versionColumn;
    }

    public void setVersionColumn(final Long versionColumn) {
        this.versionColumn = versionColumn;
    }
}