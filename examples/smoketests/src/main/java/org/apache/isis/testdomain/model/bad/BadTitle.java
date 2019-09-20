package org.apache.isis.testdomain.model.bad;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Title;

@DomainObject(nature = Nature.VIEW_MODEL)
public class BadTitle {
    
    @Title 
    public void invalidTitleProvider() {
        
    }
    
    @Title 
    public String invalidTitleProvider2() {
        return null;
    }
    
    public String title() {
        return null;
    }
    
    @Action 
    public void disableSomething() {
        
    }
    
}
