package org.apache.isis.testdomain.model.good;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;

import lombok.Getter;
import lombok.Setter;

@DomainObject(nature = Nature.VIEW_MODEL)
public class ReservedPrefixedMember {
    
    // should be allowed since 2.0
    @Action 
    public void disableSomething() {
        
    }
    
    // should be allowed since 2.0
    @Property
    @Getter @Setter
    private String disableProperty;
    
    
}
