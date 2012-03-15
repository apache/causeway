package org.apache.isis.extensions.jpa.metamodel.facets.prop.onetoone;

import javax.persistence.FetchType;
import javax.persistence.OneToOne;

public class SimpleObjectWithOneToOneFetchEager {

    private Long other;

    @OneToOne(fetch = FetchType.EAGER)
    public Long getOther() {
        return other;
    }

    public void setOther(final Long other) {
        this.other = other;
    }

}