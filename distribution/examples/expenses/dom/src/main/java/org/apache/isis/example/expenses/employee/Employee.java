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


package org.apache.isis.example.expenses.employee;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.annotation.RegEx;
import org.apache.isis.example.expenses.currency.Currency;
import org.apache.isis.example.expenses.recordedAction.Actor;
import org.apache.isis.example.expenses.recordedAction.RecordActionService;
import org.apache.isis.example.expenses.recordedAction.RecordedActionContext;
import org.apache.isis.example.expenses.services.UserFinder;


public class Employee extends AbstractDomainObject implements Actor, RecordedActionContext {

    // {{ Title
    public String title() {
        return getName();
    }

    // }}

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

    // {{ Injected: UserFinder
    private UserFinder userFinder;

    /**
     * This property is not persisted, nor displayed to the user.
     */
    protected UserFinder getUserFinder() {
        return this.userFinder;
    }

    /**
     * Injected by the application container.
     */
    public void setUserFinder(final UserFinder userFinder) {
        this.userFinder = userFinder;
    }

    // }}

    // }}

    // {{ Name
    private String name;

    @MemberOrder(sequence = "1")
    @Disabled
    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    // }}

    // {{ UserName field
    private String userName;

    /**
     * Hidden field containing this TeamMember log-on username
     */
    @Hidden
    public String getUserName() {
        return userName;
    }

    /**
     * @see #getUserName
     */
    public void setUserName(final String variable) {
        this.userName = variable;
    }

    // }}

    // {{ EmailAddress
    private String emailAddress;

    @MemberOrder(sequence = "2")
    @Optional
    @RegEx(validation = "(\\w+\\.)*\\w+@(\\w+\\.)+[A-Za-z]+")
    public String getEmailAddress() {
        return this.emailAddress;
    }

    public void setEmailAddress(final String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public void modifyEmailAddress(final String emailAddress) {
        getRecordActionService().recordFieldChange(this, "Email Address", getEmailAddress(), emailAddress);
        setEmailAddress(emailAddress);
    }

    public void clearEmailAddress() {
        getRecordActionService().recordFieldChange(this, "Email Address", getEmailAddress(), "EMPTY");
        setEmailAddress(null);
    }

    public boolean hideEmailAddress() {
        return !employeeIsCurrentUser();
    }

    private Object currentUser;

    private boolean employeeIsCurrentUser() {
        if (currentUser == null) {
            currentUser = getUserFinder().currentUserAsObject();
        }
        return currentUser == this;
    }

    // }}

    // {{ Currency
    private Currency currency;

    @MemberOrder(sequence = "3")
    @Disabled
    public Currency getCurrency() {
        return this.currency;
    }

    public void setCurrency(final Currency currency) {
        this.currency = currency;
    }

    // }}

    // {{ NormalApprover
    private Employee normalApprover;

    @MemberOrder(sequence = "4")
    @Optional
    public Employee getNormalApprover() {
        return this.normalApprover;
    }

    public void setNormalApprover(final Employee normalAuthoriser) {
        this.normalApprover = normalAuthoriser;
    }

    public void modifyNormalApprover(final Employee normalAuthoriser) {
        getRecordActionService().recordFieldChange(this, "Normal Approver", getNormalApprover(), normalApprover);
        setNormalApprover(normalAuthoriser);
    }

    public void clearNormalApprover() {
        getRecordActionService().recordFieldChange(this, "Normal Approver", getNormalApprover(), "EMPTY");
        setNormalApprover(null);
    }

    public String validateNormalApprover(final Employee newApprover) {
        return newApprover == this ? CANT_BE_APPROVER_FOR_OWN_CLAIMS : null;
    }

    public String disableNormalApprover() {
        return employeeIsCurrentUser() ? null : NOT_MODIFIABLE;
    }

    public static final String NOT_MODIFIABLE = "Not modifiable by current user";
    public static final String CANT_BE_APPROVER_FOR_OWN_CLAIMS = "Can't be the approver for your own claims";

    // }}
}
