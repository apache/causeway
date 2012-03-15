package org.apache.isis.extensions.jpa.metamodel.facets.prop.column;

import javax.persistence.Column;

public class SimpleObjectWithColumnNullableFalse {


    private Long someColumn;

    @Column(nullable = false)
    public Long getSomeColumn() {
        return someColumn;
    }

    public void setSomeColumn(final Long someColumn) {
        this.someColumn = someColumn;
    }
}