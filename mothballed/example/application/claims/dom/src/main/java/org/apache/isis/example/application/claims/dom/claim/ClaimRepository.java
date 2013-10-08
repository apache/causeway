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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.NotContributed;
import org.apache.isis.applib.annotation.NotInServiceMenu;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.value.Date;

@Named("Claims")
public class ClaimRepository extends AbstractFactoryAndRepository {

    // {{ Id, iconName
    @Override
    public String getId() {
        return "claims";
    }

    public String iconName() {
        return "ClaimRepository";
    }

    // }}

    // {{ action: allClaims
    public List<Claim> allClaims() {
        return allInstances(Claim.class);
    }

    // }}

    // {{ action: findClaims
    public List<Claim> findClaims(@Named("Description") String description) {
        return allMatches(Claim.class, description);
    }

    // }}

    // {{ action: claimsFor
    public List<Claim> claimsFor(final Claimant claimant) {
        final Claim pattern = newTransientInstance(Claim.class);
        pattern.setDescription(null);
        pattern.setApprover(null);
        pattern.setStatus(null);
        pattern.setDate(null);
        pattern.setClaimant(claimant);
        return allMatches(Claim.class, pattern);
    }

    // }}

    // {{ action: newClaim
    public Claim newClaim(final Claimant claimant) {
        final Claim claim = newTransientInstance(Claim.class);
        if (claimant != null) {
            claim.setClaimant(claimant);
            claim.setApprover(claimant.getDefaultApprover());
        }
        return claim;
    }

    // }}

    // {{ action: newClaimWithDescription
    @Named("New Claim")
    public Claim newClaimWithDescription(Claimant claimant, String description) {
        final Claim claim = newClaim(claimant);
        claim.setDescription(description);
        return claim;
    }

    // }}

    // {{ action: claimsSince
    @NotInServiceMenu
    public List<Claim> claimsSince(final Claimant claimant, final Date since) {
        return allMatches(Claim.class, new Filter<Claim>() {

            @Override
            public boolean accept(final Claim pojo) {
                return pojo.getClaimant() == claimant && pojo.getDate() != null && pojo.getDate().isGreaterThan(since);
            }
        });
    }

    public String validateClaimsSince(final Claimant claimant, final Date since) {
        final Date today = new Date();
        return since.isGreaterThan(today) ? "cannot be after today" : null;
    }

    // }}

    @NotContributed
    int countClaimsFor(Claimant claimant) {
        return claimsFor(claimant).size();
    }

    @NotContributed
    Claim mostRecentClaim(Claimant claimant) {
        final List<Claim> claims = claimsFor(claimant);
        Collections.sort(claims, new Comparator<Claim>() {
            @Override
            public int compare(final Claim o1, final Claim o2) {
                return o1.getDate().isLessThan(o2.getDate()) ? +1 : -1;
            }
        });
        return claims.size() > 0 ? claims.get(0) : null;
    }
    
}
