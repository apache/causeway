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

package org.apache.isis.example.application.claims.dom.claim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.MaxLength;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.MustSatisfy;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.spec.Specification;
import org.apache.isis.applib.util.Reasons;
import org.apache.isis.applib.value.Date;
import org.apache.isis.applib.value.Money;

/**
 * @author danhaywood
 */
public class Claim extends AbstractDomainObject /* implements Calendarable */{

    // {{ Title
    public String title() {
        return getStatus() + " - " + getDate();
    }

    // }}

    // {{ Lifecycle
    public void created() {
        status = "New";
        date = new Date(); // applib date uses the Clock
    }

    // }}

    // {{ Rush
    private boolean rush;

    @Hidden
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

    public void setDescription(final String description) {
        this.description = description;
    }

    public String defaultDescription() {
        return "enter a description here";
    }

    public String validateDescription(final String description) {
        if (description == null) {
            return null;
        }
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

    public void setDate(final Date date) {
        this.date = date;
    }

    // }}

    // {{ Status
    /**
     * @uml.property name="status"
     */
    private String status;

    /**
     * @return
     * @uml.property name="status"
     */
    @Disabled
    @MemberOrder(sequence = "3")
    @MaxLength(5)
    public String getStatus() {
        return status;
    }

    /**
     * @param status
     * @uml.property name="status"
     */
    public void setStatus(final String status) {
        this.status = status;
    }

    // }}

    // {{ changeStatus
    @MemberOrder(sequence = "1")
    public void changeStatus(@MustSatisfy(ClaimStatus.ChoicesSpecification.class) final String status) {
        setStatus(status);
    }

    public List<String> choices0ChangeStatus() {
        return ClaimStatus.ALL;
    }

    private String ifAlreadySubmitted() {
        return ClaimStatus.SUBMITTED.equals(getStatus()) ? "Already submitted" : null;
    }

    // }}

    // {{ Claimant
    /**
     * @uml.property name="claimant"
     * @uml.associationEnd
     */
    private Claimant claimant;

    /**
     * @return
     * @uml.property name="claimant"
     */
    @Disabled
    @MemberOrder(sequence = "4")
    public Claimant getClaimant() {
        return claimant;
    }

    /**
     * @param claimant
     * @uml.property name="claimant"
     */
    public void setClaimant(final Claimant claimant) {
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

    public void setApprover(final Approver approver) {
        this.approver = approver;
    }

    public String disableApprover() {
        return getDescription().contains("baz") ? "desc contains baz" : null;
    }

    public String validateApprover(final Approver approver) {
        if (approver == null) {
            return null;
        }
        return approver == getClaimant() ? "Can't approve own claims" : null;
    }

    // }}

    // {{ Items
    private final List<ClaimItem> items = new ArrayList<ClaimItem>();

    @MemberOrder(sequence = "6")
    public List<ClaimItem> getItems() {
        return items;
    }

    public void addToItems(final ClaimItem item) {
        items.add(item);
    }

    public void removeFromItems(final ClaimItem item) {
        items.remove(item);
    }

    // }}

    // {{ action: Submit
    public void submit(final Approver approver) {
        setStatus(ClaimStatus.SUBMITTED);
        setApprover(approver);
    }

    public String disableSubmit(final Approver approver) {
        return !ClaimStatus.SUBMITTED.equals(getStatus()) ? null : "Claim has already been submitted";
    }

    public Approver default0Submit() {
        return getClaimant().getDefaultApprover();
    }

    // }}

    // {{ action: addItem
    @MemberOrder(sequence = "1")
    public void addItem(@Named("Days since") final int days, @Named("Amount") final double amount, @Named("Description") final String description) {
        final ClaimItem claimItem = newTransientInstance(ClaimItem.class);
        Date date = new Date();
        date = date.add(0, 0, days);
        claimItem.setDateIncurred(date);
        claimItem.setDescription(description);
        claimItem.setAmount(new Money(amount, "USD"));
        persist(claimItem);
        addToItems(claimItem);
    }

    public String disableAddItem(final int days, final double amount, final String description) {
        return Reasons.coalesce(ifAlreadySubmitted());
    }

    // }}

    // {{ removeItem
    @MemberOrder(sequence = "2")
    public void removeItem(final ClaimItem claimItem) {
        removeFromItems(claimItem);
    }

    public String disableRemoveItem(final ClaimItem claimItem) {
        return Reasons.coalesce(ifAlreadySubmitted());
    }

    public ClaimItem default0RemoveItem() {
        if (getItems().size() > 0) {
            return getItems().get(getItems().size() - 1);
        } else {
            return null;
        }
    }

    public List<ClaimItem> choices0RemoveItem() {
        return Collections.unmodifiableList(getItems());
    }

    // }}

    public String validate() {
        if (ClaimStatus.INCOMPLETE.equals(getStatus())) {
            return "incomplete";
        }
        if (getDescription().contains("foobaz")) {
            return "no 'foobaz' allowed in description!";
        }
        return null;
    }

    public static class ClaimStatus {

        private static final String NEW = "New";
        private static final String INCOMPLETE = "Incomplete";
        private static final String SUBMITTED = "Submitted";

        public static final List<String> ALL = Collections.unmodifiableList(Arrays.asList(NEW, INCOMPLETE, SUBMITTED));

        public static class ChoicesSpecification implements Specification {

            @Override
            public String satisfies(final Object obj) {
                for (final String str : ALL) {
                    if (str.equals(obj)) {
                        return null;
                    }
                }
                return "Must be one of " + ALL;
            }
        }
    }

}
