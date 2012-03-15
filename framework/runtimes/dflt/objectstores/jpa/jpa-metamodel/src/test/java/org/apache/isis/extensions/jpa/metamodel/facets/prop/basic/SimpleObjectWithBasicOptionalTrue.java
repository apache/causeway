package org.apache.isis.extensions.jpa.metamodel.facets.prop.basic;

import javax.persistence.Basic;

public class SimpleObjectWithBasicOptionalTrue {


    private Long someColumn;

    @Basic(optional = true)
    public Long getSomeColumn() {
        return someColumn;
    }

    public void setSomeColumn(final Long someColumn) {
        this.someColumn = someColumn;
    }
}