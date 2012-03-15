package org.apache.isis.extensions.jpa.metamodel.facets.prop.joincolumn;

import javax.persistence.JoinColumn;

public class SimpleObjectWithJoinColumnName {


    private Long joinColumn;

    @JoinColumn(name = "joinCol")
    public Long getJoinColumn() {
        return joinColumn;
    }

    public void setJoinColumn(final Long joinColumn) {
        this.joinColumn = joinColumn;
    }
}