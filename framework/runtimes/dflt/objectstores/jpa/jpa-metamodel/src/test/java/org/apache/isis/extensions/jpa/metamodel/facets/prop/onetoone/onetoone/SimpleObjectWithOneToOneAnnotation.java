package org.apache.isis.extensions.jpa.metamodel.facets.prop.onetoone.onetoone;

import javax.persistence.OneToOne;

public class SimpleObjectWithOneToOneAnnotation {

    private Long other;

    @OneToOne
    public Long getOther() {
        return other;
    }

    public void setOther(final Long other) {
        this.other = other;
    }

}