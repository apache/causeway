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


package org.apache.isis.examples.claims.service.claim;

import java.util.List;

import org.apache.isis.applib.AbstractFactoryAndRepository;

import org.apache.isis.examples.claims.dom.claim.Claim;
import org.apache.isis.examples.claims.dom.claim.ClaimRepository;
import org.apache.isis.examples.claims.dom.claim.Claimant;


public class ClaimRepositoryInMemory extends AbstractFactoryAndRepository implements ClaimRepository {

	// {{ Id, iconName
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
    public List<Claim> findClaims(String description
    		) {
        return allMatches(Claim.class, description);
    }
    // }}

    
    // {{ action: claimsFor
    public List<Claim> claimsFor(Claimant claimant) {
        Claim pattern = newTransientInstance(Claim.class);
        pattern.setStatus(null);
        pattern.setDate(null);
        pattern.setClaimant(claimant);
        return allMatches(Claim.class, pattern);
    }
    // }}
    
    // {{ action: newClaim
	public Claim newClaim(Claimant claimant) {
        Claim claim = newTransientInstance(Claim.class);
        if (claimant != null) {
	        claim.setClaimant(claimant);
	        claim.setApprover(claimant.getApprover());
        }
		return claim;
	}
	// }}

    
}
