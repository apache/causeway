package org.apache.isis.extensions.jpa.metamodel.facets.prop.onetoone;

import javax.persistence.OneToOne;

public class SimpleObjectWithOneToOneOptionalFalse {

    private Long other;

    @OneToOne(optional = false)
    public Long getOther() {
        return other;
    }

    public void setOther(final Long other) {
        this.other = other;
    }

}