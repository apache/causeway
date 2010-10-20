package org.apache.isis.extensions.groovy.testapp.claims.dom.claim;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.value.Date;
import org.apache.isis.applib.value.Money;

class Claim extends AbstractDomainObject {
    
    boolean rush
    String description
    Date date
    String status
    Claimant claimant
    Approver approver
    List<ClaimItem> claimItems = new ArrayList<ClaimItem>()
    
    String title() { status + " - " + date
    }
    
    void created() {
        status = "New"
        date = new Date()
    }
    
    @MemberOrder(sequence = "1.5")
    boolean getRush() { rush
    }
    
    @MemberOrder(sequence = "1")
    String getDescription() { description
    }
    
    @MemberOrder(sequence="2")
    Date getDate() { date
    }
    
    @Disabled
    @MemberOrder(sequence = "3")
    String getStatus() { status
    }
    
    @Disabled
    @MemberOrder(sequence = "4")
    Claimant getClaimant() { claimant
    }
    
    @Disabled
    @MemberOrder(sequence = "5")
    Approver getApprover() { approver
    }
    
    
    @MemberOrder(sequence = "6")
    List<ClaimItem> getClaimItems() { claimItems
    }
    void addToClaimItems(ClaimItem item) {
        claimItems.add(item);
    }
    
    void submit(Approver approver) {
        status = "Submitted"
        this.approver = approver
    }
    String disableSubmit() {
        status == "New" ? null : "Claim has already been submitted"
    }
    Approver default0Submit() { claimant?.approver
    }
    
    
    void addItem(
    @Named("Days since")
    int days,
    @Named("Amount")
    double amount, 
    @Named("Description")
    String description) { 
        ClaimItem claimItem = newTransientInstance(ClaimItem.class)
        def date = new Date()
        date = date.add(0, 0, days)
        claimItem.dateIncurred = date
        claimItem.description = description
        claimItem.amount = new Money(amount, "USD")
        persist(claimItem)
        addToItems(claimItem)
    }
    String validateAddItem(int days, double amount, String description) {
        if (days <= 0) "Days must be positive value"
    }
    int default0AddItem() { 1
    }
    List<String> choices2AddItem() { ["meal", "taxi", "plane", "train"]
    }
    String disableAddItem() {
        status == "Submitted" ? "Already submitted" : null
    }
    
}
