package org.apache.isis.extensions.bdd.testapp.claims.dom.claim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.value.Date;

public class Claim extends AbstractDomainObject {

    // {{ Title
    public String title() {
        return getStatus() + " - " + getDate();
    }
    // }}

    
    // {{ Lifecycle
    public void created() {
        status = "New";
        date = new Date();
    }
    // }}

    
    // {{ Description
    private String description;
    @MemberOrder(sequence = "1")
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String defaultDescription() {
		return "enter a description here";
	}
    // }}

    
    // {{ Date
    private Date date;
    @MemberOrder(sequence="2")
    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }
    // }}

    

    
    // {{ Status
    private String status;
    @Disabled
    @MemberOrder(sequence = "3")
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    // }}
    
    // {{ changeStatus
	@MemberOrder(sequence = "1")
	public void changeStatus(final String status) {
		setStatus(status);
	}
	public List<String> choices0ChangeStatus() {
		return Arrays.asList("New", "Incomplete", "Done");
	}
	// }}



    
    // {{ Claimant
    private Claimant claimant;
    @Disabled
    @MemberOrder(sequence = "4")
    public Claimant getClaimant() {
        return claimant;
    }
    public void setClaimant(Claimant claimant) {
        this.claimant = claimant;
    }
    // }}


    

    // {{ Approver
    private Approver approver;
    @Disabled
    @MemberOrder(sequence = "5")
    public Approver getApprover() {
        return approver;
    }
    public void setApprover(Approver approver) {
        this.approver = approver;
    }
    // }}


    // {{ Items
    private List<ClaimItem> items = new ArrayList<ClaimItem>();
    @MemberOrder(sequence = "6")
    public List<ClaimItem> getItems() {
        return items;
    }
    public void addToItems(ClaimItem item) {
        items.add(item);
    }
    // }}

    
    // {{ action: Submit
    public void submit(Approver approver) {
        setStatus("Submitted");
        setApprover(approver);
    }
    public String disableSubmit() {
        return getStatus().equals("New") ? null : "Claim has already been submitted";
    }
    public Object[] defaultSubmit() {
        return new Object[] { getClaimant().getApprover() };
    }
    // }}

    
    public String validate() {
		if(getStatus().equals("Incomplete")) {
			return "incomplete";
		}
		return null;
	}
    
    
    // {{ Lifecycle methods
	public void persisting() {
	}

	public void persisted() {
		getClaimant().addToMostRecentClaims(this);
	}
	// }}

}
