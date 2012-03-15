package org.apache.isis.extensions.jpa.metamodel.facets.prop.column;

import javax.persistence.Column;

public class SimpleObjectWithColumnMaxLength30 {


    private Long someColumn;

    @Column(length = 30)
    public Long getSomeColumn() {
        return someColumn;
    }

    public void setSomeColumn(final Long someColumn) {
        this.someColumn = someColumn;
    }
}