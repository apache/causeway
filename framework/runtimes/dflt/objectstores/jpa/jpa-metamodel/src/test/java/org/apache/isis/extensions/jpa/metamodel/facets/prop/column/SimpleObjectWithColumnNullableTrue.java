package org.apache.isis.extensions.jpa.metamodel.facets.prop.column;

import javax.persistence.Column;

public class SimpleObjectWithColumnNullableTrue {


    private Long someColumn;

    @Column(name = "ColumnName", nullable = true)
    public Long getSomeColumn() {
        return someColumn;
    }

    public void setSomeColumn(final Long someColumn) {
        this.someColumn = someColumn;
    }
}