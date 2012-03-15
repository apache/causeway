package org.apache.isis.extensions.jpa.metamodel.facets.prop.manytoone;

import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

public class SimpleObjectWithManyToOneFetchLazy {

    private Long parent;

    @ManyToOne(fetch = FetchType.LAZY)
    public Long getParent() {
        return parent;
    }

    public void setParent(final Long parent) {
        this.parent = parent;
    }

}