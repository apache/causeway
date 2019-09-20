package org.apache.isis.testdomain.model.good;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;

@DomainObject(nature = Nature.VIEW_MODEL)
public class GoodDomainObject {
    
    @Action 
    public void disableSomething() {
        
    }
    
}
