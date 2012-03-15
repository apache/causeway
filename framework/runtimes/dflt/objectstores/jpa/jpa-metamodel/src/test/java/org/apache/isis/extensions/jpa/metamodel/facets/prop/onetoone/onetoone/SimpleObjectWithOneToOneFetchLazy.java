package org.apache.isis.extensions.jpa.metamodel.facets.prop.onetoone.onetoone;

import javax.persistence.FetchType;
import javax.persistence.OneToOne;

public class SimpleObjectWithOneToOneFetchLazy {

    private Long other;

    @OneToOne(fetch = FetchType.LAZY)
    public Long getOther() {
        return other;
    }

    public void setOther(final Long other) {
        this.other = other;
    }

}