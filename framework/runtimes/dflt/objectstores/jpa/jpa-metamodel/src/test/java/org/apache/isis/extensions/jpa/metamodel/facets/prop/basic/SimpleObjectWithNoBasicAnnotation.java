package org.apache.isis.extensions.jpa.metamodel.facets.prop.basic;


public class SimpleObjectWithNoBasicAnnotation {


    private Long someColumn;

    public Long getSomeColumn() {
        return someColumn;
    }

    public void setSomeColumn(final Long someColumn) {
        this.someColumn = someColumn;
    }
}