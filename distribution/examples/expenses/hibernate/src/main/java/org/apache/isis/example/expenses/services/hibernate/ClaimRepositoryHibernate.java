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


package org.apache.isis.example.expenses.services.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.example.expenses.claims.Claim;
import org.apache.isis.example.expenses.claims.ClaimRepositoryAbstract;
import org.apache.isis.example.expenses.claims.ClaimStatus;
import org.apache.isis.example.expenses.claims.ExpenseItem;
import org.apache.isis.example.expenses.claims.ExpenseType;
import org.apache.isis.example.expenses.claims.items.AbstractExpenseItem;
import org.apache.isis.example.expenses.employee.Employee;


public class ClaimRepositoryHibernate extends ClaimRepositoryAbstract {

    // {{ Injected Services
    /*
     * This region contains references to the services (Repositories, Factories or other Services) used by
     * this domain object. The references are injected by the application container.
     */

    // {{ Injected: HibernateHelper
    private HibernateHelper hibernateHelper;

    /**
     * This field is not persisted, nor displayed to the user.
     */
    protected HibernateHelper getHibernateHelper() {
        return this.hibernateHelper;
    }

    /**
     * Injected by the application container.
     */
    public void setHibernateHelper(final HibernateHelper hibernateHelper) {
        this.hibernateHelper = hibernateHelper;
    }

    // }}

    // }}

    final static String TEMPLATE_DESCRIPTION = "Template";

    private Integer maxClaimsToRetrieve;

    private List<Claim> findAllClaims(final Employee employee, final ClaimStatus status, final String description) {

        final Criteria criteria = hibernateHelper.createCriteria(Claim.class);
        if (employee != null) {
            criteria.add(Restrictions.eq("claimant", employee));
        }
        if (status != null) {
            criteria.add(Restrictions.eq("status", status));
        }
        if (description != null) {
            criteria.add(Restrictions.like("description", description, MatchMode.ANYWHERE));
        }
        if (maxClaimsToRetrieve != null) {
            criteria.setMaxResults(maxClaimsToRetrieve.intValue());
        }
        criteria.addOrder(Order.desc("dateCreated"));

        return hibernateHelper.findByCriteria(criteria, Employee.class);

    }

    @SuppressWarnings("unchecked")
    @Hidden
    public List<Claim> findClaims(final Employee employee, final ClaimStatus status, final String description) {

        final List<Claim> foundClaims = findAllClaims(employee, status, description);
        if (foundClaims.size() > MAX_CLAIMS) {
            warnUser("Too many claims found - refine search");
            return null;
        } else if (foundClaims.size() == 0) {
            informUser("No claims found");
        }

        return foundClaims;

    }

    /**
     * For testing to inject mock
     */
    @Hidden
    public void setHibernateSession(final Session session) {
    // setSession(session);
    }

    @Hidden
    public List<Claim> findRecentClaims(final Employee employee) {
        maxClaimsToRetrieve = Integer.valueOf(MAX_ITEMS);
        final List<Claim> foundClaims = findClaims(employee, null, null);
        maxClaimsToRetrieve = null;
        return foundClaims;
    }

    @Hidden
    public List<Claim> findClaimsAwaitingApprovalBy(final Employee approver) {
        final Criteria criteria = hibernateHelper.createCriteria(Claim.class);
        criteria.add(Restrictions.eq("approver", approver)).createCriteria("status").add(
                Restrictions.eq("titleString", ClaimStatus.SUBMITTED));
        return hibernateHelper.findByCriteria(criteria, Claim.class);
    }

    @Override
    @Hidden
    public List<ExpenseItem> findExpenseItemsOfType(final Employee employee, final ExpenseType type) {
        // example query implementation
        final Query query = hibernateHelper.createEntityQuery("o.expenseType = ? and o.claim.claimant = ?",
                AbstractExpenseItem.class);
        query.setEntity(0, type);
        query.setEntity(1, employee);
        return hibernateHelper.findByQuery(query, ExpenseItem.class);
    }
}
