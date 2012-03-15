package org.apache.isis.extensions.jpa.metamodel.facets.prop.joincolumn;

import javax.persistence.JoinColumn;

public class SimpleObjectWithJoinColumnNullableFalse {


    private Long joinColumn;

    @JoinColumn(nullable = false)
    public Long getJoinColumn() {
        return joinColumn;
    }

    public void setJoinColumn(final Long joinColumn) {
        this.joinColumn = joinColumn;
    }
}