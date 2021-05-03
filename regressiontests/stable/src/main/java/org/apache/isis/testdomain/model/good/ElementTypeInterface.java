package org.apache.isis.testdomain.model.good;

import org.apache.isis.applib.annotation.DomainObject;

@DomainObject(objectType = "isis.testdomain.ElementTypeInterface")
public interface ElementTypeInterface {

    default String title() {
        return "aTitle";
    }
    
}
