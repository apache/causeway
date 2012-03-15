package org.apache.isis.extensions.jpa.metamodel.facets.prop.version;


public class SimpleObjectWithNoVersionAnnotation {


    private Long versionColumn;

    public Long getVersionColumn() {
        return versionColumn;
    }

    public void setVersionColumn(final Long versionColumn) {
        this.versionColumn = versionColumn;
    }
}