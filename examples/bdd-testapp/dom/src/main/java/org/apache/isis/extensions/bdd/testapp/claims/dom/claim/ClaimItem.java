package org.apache.isis.extensions.bdd.testapp.claims.dom.claim;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.value.Date;
import org.apache.isis.applib.value.Money;


public class ClaimItem extends AbstractDomainObject {

	// {{ Title
    public String title() {
        return getDescription();
    }
    // }}
	
    
	// {{ DateIncurred
	private Date dateIncurred;
    @MemberOrder(sequence="1")
    public Date getDateIncurred() {
        return dateIncurred;
    }
    public void setDateIncurred(Date dateIncurred) {
        this.dateIncurred = dateIncurred;
    }
    // }}
    
    
    // {{ Description
    private String description;
    @MemberOrder(sequence="2")
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    // }}


    // {{ Amount
    private Money amount;
    @MemberOrder(sequence="3")
    public Money getAmount() {
        return amount;
    }
    public void setAmount(Money price) {
        this.amount = price;
    }
    // }}

}
