package org.apache.isis.extensions.jpa.metamodel.facets.prop.basic;

import javax.persistence.Basic;
import javax.persistence.FetchType;

public class SimpleObjectWithBasicFetchTypeLazy {


    private Long someColumn;

    @Basic(fetch = FetchType.LAZY)
    public Long getSomeColumn() {
        return someColumn;
    }

    public void setSomeColumn(final Long someColumn) {
        this.someColumn = someColumn;
    }
}