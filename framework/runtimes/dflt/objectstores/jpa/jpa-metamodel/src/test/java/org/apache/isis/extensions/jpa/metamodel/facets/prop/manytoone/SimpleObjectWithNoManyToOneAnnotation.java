package org.apache.isis.extensions.jpa.metamodel.facets.prop.manytoone;


public class SimpleObjectWithNoManyToOneAnnotation {

    private Long parent;

    public Long getParent() {
        return parent;
    }

    public void setParent(final Long parent) {
        this.parent = parent;
    }

}