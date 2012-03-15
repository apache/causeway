package org.apache.isis.extensions.jpa.metamodel.facets.prop.manytoone;

import javax.persistence.ManyToOne;

public class SimpleObjectWithManyToOneAnnotation {

    private Long parent;

    @ManyToOne
    public Long getParent() {
        return parent;
    }

    public void setParent(final Long parent) {
        this.parent = parent;
    }

}