package org.apache.isis.testdomain.model.good;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.extensions.modelannotation.applib.annotation.Model;

import lombok.Getter;
import lombok.Setter;

@DomainObject(nature = Nature.VIEW_MODEL)
public class ProperPrefixedMember {
    
    // should be allowed since 2.0
    @Action 
    public String disableSomething() {
        return null;
    }
    
    // should be identified as support
    @Model
    public String disableDisableSomething() {
        return null;
    }
    
    
    // should be allowed
    @Property
    @Getter @Setter
    private String disableProperty;
    
    
}
