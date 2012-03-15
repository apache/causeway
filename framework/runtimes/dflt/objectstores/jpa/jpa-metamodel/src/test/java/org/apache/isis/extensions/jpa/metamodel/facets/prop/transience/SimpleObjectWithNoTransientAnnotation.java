package org.apache.isis.extensions.jpa.metamodel.facets.prop.transience;


public class SimpleObjectWithNoTransientAnnotation {


    private Long transientColumn;

    public Long getTransientColumn() {
        return transientColumn;
    }

    public void setTransientColumn(final Long transientColumn) {
        this.transientColumn = transientColumn;
    }
}