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


package org.apache.isis.example.expenses.claims;

import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.value.Date;
import org.apache.isis.applib.value.Money;
import org.apache.isis.example.expenses.employee.Employee;
import org.apache.isis.example.expenses.recordedAction.RecordActionService;
import org.apache.isis.example.expenses.recordedAction.RecordedActionContext;
import org.apache.isis.example.expenses.services.EmailSender;


public class Claim extends AbstractClaim implements RecordedActionContext {

    // {{ Injected Services

    // {{ Injected: RecordActionService
    private RecordActionService recordActionService;

    /**
     * This property is not persisted, nor displayed to the user.
     */
    protected RecordActionService getRecordActionService() {
        return this.recordActionService;
    }

    /**
     * Injected by the application container.
     */
    public void setRecordActionService(final RecordActionService recordActionService) {
        this.recordActionService = recordActionService;
    }

    // }}

    // {{ Injected: EmailSender
    private EmailSender emailSender;

    /**
     * This property is not persisted, nor displayed to the user.
     */
    protected EmailSender getEmailSender() {
        return this.emailSender;
    }

    /**
     * Injected by the application container.
     */
    public void setEmailSender(final EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    // }}

    // }}

    // {{ Life Cycle methods
    public void created() {
        changeStatusToNew();
        dateCreated = new Date();
    }

    // }}

    // {{ DateCreated
    private Date dateCreated;

    @Disabled
    @MemberOrder(sequence = "2")
    public Date getDateCreated() {
        return this.dateCreated;
    }

    public void setDateCreated(final Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    // }}

    // {{ Status field
    private void changeStatusTo(final String title) {
        setStatus(getClaimRepository().findClaimStatus(title));
    }

    protected boolean isNew() {
        return getStatus().isNew();
    }

    protected void changeStatusToNew() {
        changeStatusTo(ClaimStatus.NEW);
    }

    protected boolean isSubmitted() {
        return getStatus().isSubmitted();
    }

    protected void changeStatusToSubmitted() {
        changeStatusTo(ClaimStatus.SUBMITTED);
    }

    protected boolean isReturned() {
        return getStatus().isReturned();
    }

    protected void changeStatusToReturned() {
        changeStatusTo(ClaimStatus.RETURNED);
    }

    protected boolean isToBePaid() {
        return getStatus().isPaid();
    }

    protected void changeStatusToToBePaid() {
        changeStatusTo(ClaimStatus.TO_BE_PAID);
    }

    protected boolean isPaid() {
        return getStatus().isPaid();
    }

    protected void changeStatusToPaid() {
        changeStatusTo(ClaimStatus.PAID);
    }

    private ClaimStatus status;

    @MemberOrder(sequence = "3")
    @Disabled
    public ClaimStatus getStatus() {
        return status;
    }

    public void setStatus(final ClaimStatus claimStatus) {
        this.status = claimStatus;
    }

    @Override
    @Hidden
    public String modifiable() {
        if (isNew() || isReturned()) {
            return null;
        }
        return "Cannot modify claim";
    }

    // }}

    // {{ Calculating the Total of Expense Items

    // {{ Total Field
    private Money total;

    @MemberOrder(sequence = "7")
    @Disabled
    public Money getTotal() {
        return this.total;
    }

    public void setTotal(final Money total) {
        this.total = total;
    }

    @Hidden
    public void initialiseTotal() {
        total = new Money(0, currencyCode());
    }

    @Hidden
    public void recalculateTotal() {
        Money runningTotal = new Money(0, currencyCode());
        for (final ExpenseItem item : getExpenseItems()) {
            final Money itemAmount = item.requestedOrApprovedAmount();
            if (itemAmount != null) {
                runningTotal = runningTotal.add(itemAmount);
            }
        }
        setTotal(runningTotal);
    }

    // }}

    @Override
    public void addToExpenseItems(final ExpenseItem collectionNameSingularForm) {
        super.addToExpenseItems(collectionNameSingularForm);
        recalculateTotal();
    }

    @Override
    public void removeFromExpenseItems(final ExpenseItem item) {
        super.removeFromExpenseItems(item);
        recalculateTotal();
    }

    // }}

    // {{ Workflow (submit, return etc)

    // {{ Submit

    @MemberOrder(sequence = "4.1")
    public void submit(@Named("Approver")
    final Employee approver, @Named("Advise approver by email")
    final boolean sendEmail) {
        setApprover(approver);
        changeStatusToSubmitted();
        for (final ExpenseItem item : getExpenseItems()) {
            item.setLocked(true);
        }
        final String message = getClaimant().getName() + CLAIM_AWAITING_YOUR_APPROVAL;
        sendEmailIfPossible(sendEmail, getApprover().getEmailAddress(), message);
        getRecordActionService().recordMenuAction(this, "Submit", "to " + approver.title());
    }

    private void sendEmailIfPossible(final boolean sendEmail, final String emailAddress, final String message) {
        if (sendEmail & !emailAddress.equals("") && getEmailSender() != null) {
            try {
                getEmailSender().sendTextEmail(emailAddress, message);
            } catch (final Exception e) {
                warnUser(e.getMessage() == null ? "Unknown problem sending email" : e.getMessage());
            }
        }
    }

    public static final String CLAIM_AWAITING_YOUR_APPROVAL = " has submitted an expenses claim for your approval";

    public Employee default0Submit() {
        return  getApprover();
    }

    public boolean default1Submit() {
        return Boolean.TRUE ;
    }
    
    public static final String HAS_INCOMPLETE_ITEMS = "Cannot submit claim with incomplete Items";

    public static final String CAN_ONLY_SUBMIT_NEW_OR_RETURNED_CLAIMS = "Can only submit a claim that is 'New' or 'Returned'";

    public static final String ONLY_CLAIMANT_MAY_SUBMIT = "Only the Claimant may submit a Claim";

    public String disableSubmit() {
        if (!allItemsComplete()) {
            return HAS_INCOMPLETE_ITEMS;
        }
        if (!isNew() && !isReturned()) {
            return CAN_ONLY_SUBMIT_NEW_OR_RETURNED_CLAIMS;
        }
        if (!userIsClaimant()) {
            return ONLY_CLAIMANT_MAY_SUBMIT;
        }
        return null;
    }

    // }}

    // {{ Return to Claimant
    public void returnToClaimant(@Optional
    @Named("Message to claimant")
    final String message) {
        returnToClaimant(message, true);
    }

    @Hidden
    public void returnToClaimant(@Named("Message to claimant")
    final String message, final boolean sendEmail) {
        changeStatusToReturned();
        for (final ExpenseItem item : getExpenseItems()) {
            item.setLocked(false);
        }
        getRecordActionService().recordMenuAction(this, "Return To Claimant", message);
        final String fullMessage = ("Your Expenses Claim: " + title() + " has been returned to you with the message " + message);
        sendEmailIfPossible(sendEmail, getClaimant().getEmailAddress(), fullMessage);
    }

    public static final String RETURNED_CLAIM = "A previously-submitted expenses claim has been returned to you";

    public String disableReturnToClaimant(final String message) {
        return isSubmitted() ? null : STATUS_NOT_SUBMITTED;
    }

    public static final String STATUS_NOT_SUBMITTED = "Status of claim is not 'Submitted'";

    // }}

    // }}

    // {{ Approving claims

    // {{ ApproveAllItems
    @MemberOrder(sequence = "5.1")
    public void approveItems(@Optional
    @Named("Approve New Items Only")
    boolean newOnly) {
        for (final ExpenseItem item : getExpenseItems()) {
            if (!newOnly || (newOnly && item.isNewComplete())) {
                item.approve();
            }
        }
    }

    public String disableApproveItems() {
        return disableApproverActionsOnAllItems();
    }

    // }}

    // {{ RejectAllItems
    @MemberOrder(sequence = "5.3")
    public void rejectItems(@Named("Reason For Rejection")
    final String reason, @Optional
    @Named("Reject New Items Only")
    boolean newOnly) {
        for (final ExpenseItem item : getExpenseItems()) {
            if (!newOnly || (newOnly && item.isNewComplete())) {
                item.reject(reason);
            }
        }

    }

    public String disableRejectItems() {
        return disableApproverActionsOnAllItems();
    }

    // }}

    // {{ QueryAllItems
    @MemberOrder(sequence = "5.2")
    public void queryItems(@Named("Reason For Query")
    final String reason, @Optional()
    @Named("Query New Items Only")
    boolean newOnly) {
        for (final ExpenseItem item : getExpenseItems()) {
            if (!newOnly || (newOnly && item.isNewComplete())) {
                item.query(reason);
            }
        }
    }

    public String disableQueryItems() {
        return disableApproverActionsOnAllItems();
    }

    // }}

    @Override
    @Hidden
    public String disableApproverActionsOnAllItems() {
        if (!isSubmitted()) {
            return APPROVER_ACTIONS_NOT_VALID_ON_NEW_CLAIM;
        }
        if (!userIsTheApproverForThisClaim()) {
            return USER_IS_NOT_THE_APPROVER;
        }
        return null;
    }

    public static final String APPROVER_ACTIONS_NOT_VALID_ON_NEW_CLAIM = "Approver actions only available when status is Submitted";

    public static final String USER_IS_NOT_THE_APPROVER = "User is not the specified approver for this claim";

    @Override
    public String disableApprover() {
        if (isNew() || isReturned()) {
            return null;
        }
        return CANNOT_CHANGE_APPROVER_ON_SUBMITTED_CLAIM;
    }

    public static final String CANNOT_CHANGE_APPROVER_ON_SUBMITTED_CLAIM = "Cannot change the Approver on a claim that has been submitted";

    // }}

    // {{ Support for rules
    private boolean allItemsComplete() {
        if (getExpenseItems().size() == 0) {
            return false;
        }
        for (final ExpenseItem item : getExpenseItems()) {
            if (!item.isNewComplete()) {
                return false;
            }
        }
        return true;
    }

    @Hidden
    public boolean userIsTheApproverForThisClaim() {
        return getUserFinder().currentUserAsObject() == getApprover();
    }

    private boolean userIsClaimant() {
        return getUserFinder().currentUserAsObject() == getClaimant();
    }
    // }}

}
