package org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.dataclasses.polymorphism;

import org.apache.isis.applib.AbstractDomainObject;

public class PolyInterfaceImplA extends AbstractDomainObject implements PolyInterface {
    public String title() {
        return string;
    }

    // {{ String type
    private String string;

    public String getString() {
        return string;
    }

    public void setString(final String string) {
        this.string = string;
    }

    // }}

}
