/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */


package org.apache.isis.support.prototype.dom.claim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.annotation.MaxLength;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.util.Reasons;
import org.apache.isis.applib.value.Date;
import org.apache.isis.applib.value.Money;

public class Claim extends AbstractDomainObject /* implements Calendarable */ {

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

    // {{ Rush
    private boolean rush;

    @MemberOrder(sequence = "1.2")
    public boolean getRush() {
        return rush;
    }

    public void setRush(final boolean flag) {
        this.rush = flag;
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

    public String validateDescription(final String description) {
        if (description == null)
            return null;
        if (description.contains("foobar")) {
            return "can't contain foobar!";
        }
        return null;
    }

    // }}

    // {{ Date
    private Date date;

    @MemberOrder(sequence = "2")
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
    @MaxLength(5)
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private String ifAlreadySubmitted() {
        return "Submitted".equals(getStatus()) ? "Already submitted" : null;
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

    // @Disabled
    @MemberOrder(sequence = "5")
    @Optional
    public Approver getApprover() {
        return approver;
    }

    public void setApprover(Approver approver) {
        this.approver = approver;
    }

    public String disableApprover() {
        return getDescription().contains("baz") ? "desc contains baz" : null;
    }

    public String validateApprover(final Approver approver) {
        if (approver == null)
            return null;
        return approver == getClaimant() ? "Can't approve own claims" : null;
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
    public void removeFromItems(ClaimItem item) {
        items.remove(item);
    }
    // }}



    // }}

    // {{ action: Submit
    public void submit(Approver approver) {
        setStatus("Submitted");
        setApprover(approver);
    }

    public String disableSubmit() {
        return getStatus().equals("New") ? null
                : "Claim has already been submitted";
    }

    public Approver default0Submit() {
        return getClaimant().getApprover();
    }

    // }}

    // {{ action: addItem
    @MemberOrder(sequence = "1")
    public void addItem(@Named("Days since") int days,
            @Named("Amount") double amount,
            @Named("Description") String description) {
        ClaimItem claimItem = newTransientInstance(ClaimItem.class);
        Date date = new Date();
        date = date.add(0, 0, days);
        claimItem.setDateIncurred(date);
        claimItem.setDescription(description);
        claimItem.setAmount(new Money(amount, "USD"));
        persist(claimItem);
        addToItems(claimItem);
    }

    public String disableAddItem() {
        return Reasons.coalesce(ifAlreadySubmitted());
    }

    // }}

    
    // {{ removeItem
    @MemberOrder(sequence = "2")
    public void removeItem(final ClaimItem claimItem) {
        removeFromItems(claimItem);
    }
    public String disableRemoveItem() {
        return Reasons.coalesce(ifAlreadySubmitted());
    }
    public ClaimItem default0RemoveItem() {
        if(getItems().size()>0) {
            return getItems().get(getItems().size()-1);
        } else {
            return null;
        }
    }
    public List<ClaimItem> choices0RemoveItem() {
        return Collections.unmodifiableList(getItems());
    }
    // }}


    public String validate() {
        if (getDescription().contains("foobaz")) {
            return "no foobaz allowed in description!";
        }
        return null;
    }

//    @Ignore
//    @Override
//    public CalendarEvent getCalendarEvent() {
//        return CalendarEvent.newAllDayEvent(getDate().dateValue());
//    }
     
}
