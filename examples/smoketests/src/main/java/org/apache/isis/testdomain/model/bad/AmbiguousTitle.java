package org.apache.isis.testdomain.model.bad;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Title;

@DomainObject(nature = Nature.VIEW_MODEL)
public class AmbiguousTitle {
    
    @Title 
    public void invalidTitleProvider() {
        
    }
    
    public String title() {
        return null;
    }
    
}
