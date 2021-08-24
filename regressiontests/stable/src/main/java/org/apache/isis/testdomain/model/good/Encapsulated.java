package org.apache.isis.testdomain.model.good;

import java.io.Serializable;

import org.apache.isis.applib.ViewModel;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;

@DomainObject(nature = Nature.VIEW_MODEL)
public class Encapsulated
implements
    Serializable,
    ViewModel.CloneableViaSerialization {

    private static final long serialVersionUID = 1L;

    // TODO should be allowed to be private since 2.0.0-M7
    @Action
    public String myAction() {
        return "Hallo World!";
    }

    // TODO should be allowed to be private since 2.0.0-M7
    @MemberSupport
    public String disableMyAction() {
        return "disabled for testing purposes";
    }

    // TODO should also work if there are no getter/setter since 2.0.0-M7
    @Property
    private String myProperty = "Foo";

    // TODO should be allowed to be private since 2.0.0-M7
    public String getMyProperty() {
        return myProperty;
    }

    // TODO should be allowed to be private since 2.0.0-M7
    public void setMyProperty(final String myProperty) {
        this.myProperty = myProperty;
    }


}
