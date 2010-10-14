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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.ApplicationException;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.example.expenses.employee.Employee;


/**
 * Contains common logic for creating a new Claim, which may be called from several contexts.
 * 
 */
public class ClaimFactory extends AbstractFactoryAndRepository {

    // {{ Injected Services
    /*
     * This region contains references to the services (Repositories, Factories or other Services) used by
     * this domain object. The references are injected by the application container.
     */

    // {{ Injected: ClaimRepository
    private ClaimRepository claimRepository;

    /**
     * This field is not persisted, nor displayed to the user.
     */
    protected ClaimRepository getClaimRepository() {
        return this.claimRepository;
    }

    /**
     * Injected by the application container.
     */
    public void setClaimRepository(final ClaimRepository claimRepository) {
        this.claimRepository = claimRepository;
    }

    // }}

    // }}

    @Hidden
    public Claim createNewClaim(final Employee employee, final String description) {
        final Claim claim = newTransientInstance(Claim.class);
        claim.setClaimant(employee);
        claim.setApprover(employee.getNormalApprover());
        claim.initialiseTotal();
        claim.setDescription(createUniqueDescription(employee, description));
        persist(claim);
        claim.changeStatusToNew();
        return claim;
    }

    @Hidden
    public String defaultUniqueClaimDescription(final Employee employee) {
        return createUniqueDescription(employee, createDefaultClaimDescription(null));
    }

    public String createDefaultClaimDescription(final String inputDescription) {
        if (inputDescription == null || inputDescription.length() == 0) {
            return new SimpleDateFormat("dd-MMM-yy").format(new Date());
        }
        return inputDescription;
    }

    private String createUniqueDescription(final Employee employee, final String initialDescription) {
        int increment = 2;
        String description = initialDescription;
        while (!claimRepository.descriptionIsUniqueForClaimant(employee, description)) {
            description = initialDescription + CLAIM_DIFFERENTIATOR + increment++;
        }
        return description;
    }

    public static final String CLAIM_DIFFERENTIATOR = " - ";

    @Hidden
    public ExpenseItem createNewExpenseItem(final AbstractClaim claim, final ExpenseType type) {
        try {
            final ExpenseItem item = newTransientInstance(classFor(type));
            item.setExpenseType(type);
            item.modifyProjectCode(claim.getProjectCode());
            item.setClaim(claim);
            item.initialiseAmount();
            return item;
        } catch (final ClassNotFoundException e) {
            throw new ApplicationException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private Class<ExpenseItem> classFor(final ExpenseType type) throws ClassNotFoundException {
        return (Class<ExpenseItem>) Class.forName(type.getCorrespondingClassName());
    }

    @Hidden
    public void makePersistent(final ExpenseItem transientExpenseItem) {
        super.persist(transientExpenseItem);
    }

}
