package org.apache.isis.extensions.groovy.testapp.claims.dom.employee;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.MemberOrder;

import org.apache.isis.extensions.groovy.testapp.claims.dom.claim.Approver;
import org.apache.isis.extensions.groovy.testapp.claims.dom.claim.Claimant;


public class Employee extends AbstractDomainObject implements Claimant, Approver {

	String name
	Approver approver
	
    String title() { name }
    
    @MemberOrder(sequence="1")
    String getName() { name }

    @MemberOrder(sequence="2")
    public Approver getApprover() { approver }
    
}


// Copyright (c) Naked Objects Group Ltd.
