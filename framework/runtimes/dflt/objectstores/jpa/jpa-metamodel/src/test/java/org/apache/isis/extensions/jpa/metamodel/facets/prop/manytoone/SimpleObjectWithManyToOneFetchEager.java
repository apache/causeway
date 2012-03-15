package org.apache.isis.extensions.jpa.metamodel.facets.prop.manytoone;

import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

public class SimpleObjectWithManyToOneFetchEager {

    private Long parent;

    @ManyToOne(fetch = FetchType.EAGER)
    public Long getParent() {
        return parent;
    }

    public void setParent(final Long parent) {
        this.parent = parent;
    }

}