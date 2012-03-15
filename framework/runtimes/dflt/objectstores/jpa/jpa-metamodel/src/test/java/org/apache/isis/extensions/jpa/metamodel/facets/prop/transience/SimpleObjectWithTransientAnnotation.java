package org.apache.isis.extensions.jpa.metamodel.facets.prop.transience;

import javax.persistence.Transient;

public class SimpleObjectWithTransientAnnotation {


    private Long transientColumn;

    @Transient
    public Long getTransientColumn() {
        return transientColumn;
    }

    public void setTransientColumn(final Long transientColumn) {
        this.transientColumn = transientColumn;
    }
}