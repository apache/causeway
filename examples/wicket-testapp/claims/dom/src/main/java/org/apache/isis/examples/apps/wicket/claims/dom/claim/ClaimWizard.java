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


package org.apache.isis.examples.apps.wicket.claims.dom.claim;

import java.util.Calendar;
import java.util.List;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.Ignore;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.NotPersistable;
import org.apache.isis.applib.clock.Clock;
import org.apache.isis.examples.apps.wicket.claims.dom.employee.EmployeeRepository;
import org.apache.isis.viewer.wicket.applib.WizardPageDescription;

@NotPersistable
public class ClaimWizard extends AbstractDomainObject {

    public enum Page {
        INTRO("This wizard will take you through the process of creating a claim"), 
        CLAIMANT("Enter the claimant that is making this claim"), 
        APPROVER("By default, the claimant's own approver will approve this claim.  " +
        		"Update here if another approver will approve this claim."), 
        DESCRIPTION("Update the description if required."), 
        SUMMARY("Confirm all details, or go back and amend if needed");

        private String description;

        private Page(String description) {
            this.description = description;
        }
        public String getDescription() {
            return description;
        }

        public boolean hasPrevious() {
            return ordinal() > 0;
        }

        public Page previous() {
            if (hasPrevious()) {
                return values()[ordinal() - 1];
            } else {
                return this;
            }
        }

        public boolean hasNext() {
            return ordinal() < values().length - 1;
        }

        public Page next() {
            if (hasNext()) {
                return values()[ordinal() + 1];
            } else {
                return this;
            }
        }

        @Ignore
        public boolean is(Page... pages) {
            for (Page page : pages) {
                if (page == this) {
                    return true;
                }
            }
            return false;
        }
    }

    public void created() {
        setPage(Page.INTRO);
        setDescription("Expenses for week #" + weekNum());
    }

    private int weekNum() {
        return getTimeAsCalendar().get(Calendar.WEEK_OF_YEAR);
    }

    protected Calendar getTimeAsCalendar() {
        return Clock.getTimeAsCalendar();
    }

    // {{ Page
    private Page page;

    @Hidden
    public Page getPage() {
        return page;
    }

    public void setPage(final Page page) {
        this.page = page;
    }

    // }}

    // {{ Page Description
    @WizardPageDescription
    @MemberOrder(sequence = "1")
    public String getPageDescription() {
        return getPage().getDescription();
    }

    // }}

    // {{ Claimant
    private Claimant claimant;

    @MemberOrder(sequence = "2")
    public Claimant getClaimant() {
        return claimant;
    }

    public void setClaimant(final Claimant claimant) {
        this.claimant = claimant;
    }

    public void modifyClaimant(final Claimant claimant) {
        Claimant currentClaimant = getClaimant();
        // check for no-op
        if (claimant == null || claimant.equals(currentClaimant)) {
            return;
        }
        // associate new
        setClaimant(claimant);
        // additional business logic
        onModifyClaimant(currentClaimant, claimant);
    }

    public void clearClaimant() {
        Claimant currentClaimant = getClaimant();
        // check for no-op
        if (currentClaimant == null) {
            return;
        }
        // dissociate existing
        setClaimant(null);
        // additional business logic
        onClearClaimant(currentClaimant);
    }

    protected void onModifyClaimant(final Claimant oldClaimant,
            final Claimant newClaimant) {
        setApprover(newClaimant.getApprover());
    }

    protected void onClearClaimant(final Claimant oldClaimant) {
    }

    @SuppressWarnings("unchecked")
    public List<Claimant> choicesClaimant() {
        List allEmployees = employeeRepository.allEmployees();
        return allEmployees;
    }

    public String disableClaimant() {
        return coalesce(claimCreated(), confirmIfOnSummaryPage());
    }

    public boolean hideClaimant() {
        return !getPage().is(Page.CLAIMANT, Page.SUMMARY);
    }

    // }}

    // {{ Approver
    private Approver approver;

    @MemberOrder(sequence = "3")
    public Approver getApprover() {
        return approver;
    }

    public void setApprover(final Approver approver) {
        this.approver = approver;
    }

    public String disableApprover() {
        return coalesce(claimCreated(), confirmIfOnSummaryPage());
    }

    public boolean hideApprover() {
        return !getPage().is(Page.APPROVER, Page.SUMMARY);
    }

    // }}

    // {{ Description
    private String description;

    @MemberOrder(sequence = "4")
    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String disableDescription() {
        return coalesce(claimCreated(), confirmIfOnSummaryPage());
    }

    public boolean hideDescription() {
        return !getPage().is(Page.DESCRIPTION, Page.SUMMARY);
    }
    
    private String claimCreated() {
        return claim != null ? "Claim created" : null;
    }

    // }}

    // {{ Claim
    private Claim claim;

    @Disabled
    @MemberOrder(sequence = "5")
    public Claim getClaim() {
        return claim;
    }

    public void setClaim(final Claim claim) {
        this.claim = claim;
    }

    public boolean hideClaim() {
        return claim == null;
    }

    // }}

    // {{ previous
    @MemberOrder(sequence = "1")
    public void previous() {
        setPage(getPage().previous());
    }

    public String disablePrevious() {
        return coalesce(noPreviousPage(), confirmIfOnSummaryPage());
    }

    private String noPreviousPage() {
        return !getPage().hasPrevious() ? "no previous page" : null;
    }

    // }}

    // {{ next
    @MemberOrder(sequence = "2")
    public void next() {
        setPage(getPage().next());
    }

    public String disableNext() {
        return coalesce(noNextPage(), confirmIfOnSummaryPage());
    }

    private String noNextPage() {
        return !getPage().hasNext() ? "no next page" : null;
    }

    // }}

    // {{ finish
    @MemberOrder(sequence = "3")
    public Claim finish() {
        Claim claim = newTransientInstance(Claim.class);
        claim.setClaimant(getClaimant());
        claim.setApprover(getApprover());
        claim.setDescription(getDescription());

        setClaim(claim);
        persist(claim);
        return claim;
    }

    public String disableFinish() {
        if (getPage().hasNext()) {
            return "wizard has further pages to complete";
        }
        return getContainer().validate(this);
    }

    // }}

    // {{ helpers
    private String confirmIfOnSummaryPage() {
        return getPage().is(Page.SUMMARY) ? "confirm" : null;
    }

    private static String coalesce(String... strings) {
        for (String string : strings) {
            if (string != null)
                return string;
        }
        return null;
    }

    // }}

    // {{ injected: EmployeeRepository
    private EmployeeRepository employeeRepository;

    public void setEmployeeRepository(
            final EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }
    // }}

}
