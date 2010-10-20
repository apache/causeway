package org.apache.isis.extensions.groovy.testapp.claims.dom.claim;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.value.Date;
import org.apache.isis.applib.value.Money;


class ClaimItem extends AbstractDomainObject {

	Date dateIncurred
	String description
	Money amount

    def String title() { description }
    
    @MemberOrder(sequence="1")
    def Date getDateIncurred() { dateIncurred }
    
    @MemberOrder(sequence="2")
    def String getDescription() { description }

    @MemberOrder(sequence="3")
    def Money getAmount() { amount }

}
