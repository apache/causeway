package org.apache.isis.extensions.jpa.metamodel.facets.prop.column;

import javax.persistence.Column;

public class SimpleObjectWithColumnAnnotation {


    private Long someColumn;

    @Column
    public Long getSomeColumn() {
        return someColumn;
    }

    public void setSomeColumn(final Long someColumn) {
        this.someColumn = someColumn;
    }
}