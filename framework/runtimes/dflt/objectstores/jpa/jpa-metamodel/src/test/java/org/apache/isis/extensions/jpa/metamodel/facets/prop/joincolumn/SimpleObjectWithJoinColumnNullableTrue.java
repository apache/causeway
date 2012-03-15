package org.apache.isis.extensions.jpa.metamodel.facets.prop.joincolumn;

import javax.persistence.JoinColumn;

public class SimpleObjectWithJoinColumnNullableTrue {


    private Long joinColumn;

    @JoinColumn(nullable = true)
    public Long getJoinColumn() {
        return joinColumn;
    }

    public void setJoinColumn(final Long joinColumn) {
        this.joinColumn = joinColumn;
    }
}