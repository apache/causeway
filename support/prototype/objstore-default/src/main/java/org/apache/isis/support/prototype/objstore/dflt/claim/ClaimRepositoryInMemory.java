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

package org.apache.isis.support.prototype.objstore.dflt.claim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.Filter;
import org.apache.isis.applib.value.Date;
import org.apache.isis.applib.value.Money;
import org.apache.isis.support.prototype.dom.claim.Claim;
import org.apache.isis.support.prototype.dom.claim.ClaimItem;
import org.apache.isis.support.prototype.dom.claim.ClaimRepository;
import org.apache.isis.support.prototype.dom.claim.ClaimWizard;
import org.apache.isis.support.prototype.dom.claim.Claimant;
import org.apache.isis.support.prototype.dom.claim.ClaimantExpenseSummary;

import com.google.common.collect.Maps;

public class ClaimRepositoryInMemory extends AbstractFactoryAndRepository implements ClaimRepository {

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
    @Override
    public List<Claim> allClaims() {
        return allInstances(Claim.class);
    }

    // }}

    // {{ action: findClaims
    @Override
    public List<Claim> findClaims(String description) {
        return allMatches(Claim.class, description);
    }

    // }}

    // {{ action: claimsFor
    @Override
    public List<Claim> claimsFor(Claimant claimant) {
        Claim pattern = newTransientInstance(Claim.class);
        pattern.setStatus(null);
        pattern.setDate(null);
        pattern.setClaimant(claimant);
        return allMatches(Claim.class, pattern);
    }

    // }}

    // {{ action: newClaim
    @Override
    public ClaimWizard newClaim(Claimant claimant) {
        final ClaimWizard claimWizard = newTransientInstance(ClaimWizard.class);
        claimWizard.modifyClaimant(claimant);
        return claimWizard;
    }

    // }}

    // {{ action: claimsSince
    @Override
    public List<Claim> claimsSince(final Claimant claimant, final Date since) {
        return allMatches(Claim.class, new Filter<Claim>() {

            @Override
            public boolean accept(Claim pojo) {
                return pojo.getClaimant() == claimant && pojo.getDate() != null && pojo.getDate().isGreaterThan(since);
            }
        });
    }

    public String validateClaimsSince(final Claimant claimant, final Date since) {
        Date today = new Date();
        return since.isGreaterThan(today) ? "cannot be after today" : null;
    }

    // }}

    // {{ analyseClaimantExpenses
    @Override
    public List<ClaimantExpenseSummary> analyseClaimantExpenses() {
        final Map<Claimant, ClaimantExpenseSummary> summaries = Maps.newHashMap();
        final List<Claim> claims = allInstances(Claim.class);
        for (Claim claim : claims) {
            final Claimant claimant = claim.getClaimant();
            ClaimantExpenseSummary summary = findOrCreateSummary(claimant, summaries);
            final List<ClaimItem> items = claim.getItems();
            for (ClaimItem item : items) {
                final Money amount = item.getAmount();
                summary.addAmount(amount);
            }
        }
        return new ArrayList<ClaimantExpenseSummary>(summaries.values());
        // }}

    }

    private ClaimantExpenseSummary findOrCreateSummary(Claimant claimant,
        Map<Claimant, ClaimantExpenseSummary> summaries) {
        ClaimantExpenseSummary summary = summaries.get(claimant);
        if (summary == null) {
            summary = newTransientInstance(ClaimantExpenseSummary.class);
            summary.setClaimant(claimant);
            summaries.put(claimant, summary);
        }
        return summary;
    }

    @Override
    public int countClaimsFor(Claimant claimant) {
        return claimsFor(claimant).size();
    }

    @Override
    public Claim mostRecentClaim(Claimant claimant) {
        List<Claim> claims = claimsFor(claimant);
        Collections.sort(claims, new Comparator<Claim>() {
            @Override
            public int compare(Claim o1, Claim o2) {
                return o1.getDate().isLessThan(o2.getDate()) ? +1 : -1;
            }
        });
        return claims.size() > 0 ? claims.get(0) : null;
    }

}
