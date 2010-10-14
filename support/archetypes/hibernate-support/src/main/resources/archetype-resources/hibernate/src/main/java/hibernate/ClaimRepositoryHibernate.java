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


package ${package}.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.apache.isis.applib.annotation.Named;
import ${package}.service.ClaimRepository;
import ${package}.dom.Claim;
import ${package}.dom.Employee;


public class ClaimRepositoryHibernate extends ClaimRepository {

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
    public void setHibernateHelper(HibernateHelper hibernateHelper) {
        this.hibernateHelper = hibernateHelper;
    }

    // }}

    // }}

	final static int MAX_CLAIMS = 10;


    public List<Claim> findClaims(@Named("Description") String description) {
        final Criteria criteria = hibernateHelper.createCriteria(Claim.class);
	     criteria.add(Restrictions.like("description", description, MatchMode.ANYWHERE));
        criteria.addOrder(Order.desc("dateCreated"));        List<Claim> foundClaims = hibernateHelper.findByCriteria(criteria, Employee.class);
        if (foundClaims.size() > MAX_CLAIMS) {
            warnUser("Too many claims found - refine search");
            return null;
        } else if (foundClaims.size() == 0) {
            informUser("No claims found");
        }
		  return foundClaims;
    }
    
    public List<Claim> claimsFor(@Named("Claimant") Employee claimant) {
        final Criteria criteria = hibernateHelper.createCriteria(Claim.class);
        criteria.add(Restrictions.eq("claimant", claimant));
        criteria.addOrder(Order.desc("dateCreated"));
        List<Claim> foundClaims = hibernateHelper.findByCriteria(criteria, Employee.class);
        if (foundClaims.size() > MAX_CLAIMS) {
            warnUser("Too many claims found - refine search");
            return null;
        } else if (foundClaims.size() == 0) {
            informUser("No claims found");
        }
        return foundClaims;
    }

}
