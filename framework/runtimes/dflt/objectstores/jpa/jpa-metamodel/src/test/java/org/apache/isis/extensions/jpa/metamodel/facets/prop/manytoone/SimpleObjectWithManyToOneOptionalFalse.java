package org.apache.isis.extensions.jpa.metamodel.facets.prop.manytoone;

import javax.persistence.ManyToOne;

public class SimpleObjectWithManyToOneOptionalFalse {

    private Long parent;

    @ManyToOne(optional = false)
    public Long getParent() {
        return parent;
    }

    public void setParent(final Long parent) {
        this.parent = parent;
    }

}