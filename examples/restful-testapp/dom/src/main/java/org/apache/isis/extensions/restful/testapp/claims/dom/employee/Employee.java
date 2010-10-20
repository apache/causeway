package org.apache.isis.extensions.restful.testapp.claims.dom.employee;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.extensions.restful.testapp.claims.dom.claim.Approver;
import org.apache.isis.extensions.restful.testapp.claims.dom.claim.Claimant;


public class Employee extends AbstractDomainObject implements Claimant, Approver {

	// {{ Title
    public String title() {
        return getName();
    }
    // }}

    
    // {{ Name
    private String name;
    @MemberOrder(sequence="1")
    public String getName() {
        return name;
    }
    public void setName(String lastName) {
        this.name = lastName;
    }
    // }}
    

    // {{ Approver
    private Approver approver;
    @MemberOrder(sequence="2")
    public Approver getApprover() {
        return approver;
    }
    public void setApprover(Approver approver) {
        this.approver = approver;
    }
    // }}
    
}


// Copyright (c) Naked Objects Group Ltd.
