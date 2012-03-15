package org.apache.isis.extensions.jpa.metamodel.facets.prop.onetoone.onetoone;

import javax.persistence.OneToOne;

public class SimpleObjectWithOneToOneOptionalTrue {

    private Long other;

    @OneToOne(optional = true)
    public Long getOther() {
        return other;
    }

    public void setOther(final Long other) {
        this.other = other;
    }

}