package org.apache.isis.extensions.jpa.metamodel.facets.prop.joincolumn;

import javax.persistence.JoinColumn;

public class SimpleObjectWithJoinColumnAnnotation {


    private Long joinColumn;

    @JoinColumn
    public Long getJoinColumn() {
        return joinColumn;
    }

    public void setJoinColumn(final Long joinColumn) {
        this.joinColumn = joinColumn;
    }
}