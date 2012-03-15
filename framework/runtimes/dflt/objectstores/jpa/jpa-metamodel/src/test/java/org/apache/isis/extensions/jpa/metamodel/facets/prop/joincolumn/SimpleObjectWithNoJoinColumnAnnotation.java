package org.apache.isis.extensions.jpa.metamodel.facets.prop.joincolumn;


public class SimpleObjectWithNoJoinColumnAnnotation {


    private Long joinColumn;

    public Long getJoinColumn() {
        return joinColumn;
    }

    public void setJoinColumn(final Long joinColumn) {
        this.joinColumn = joinColumn;
    }
}