package org.apache.isis.extensions.groovy.testapp.claims.service.claim;

import java.util.List;

import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.Filter;
import org.apache.isis.applib.value.Date;
import org.apache.isis.extensions.groovy.testapp.claims.dom.claim.Claim;
import org.apache.isis.extensions.groovy.testapp.claims.dom.claim.ClaimRepository;
import org.apache.isis.extensions.groovy.testapp.claims.dom.claim.Claimant;


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
    
	// }}
	@Override
	public List<Claim> claimsSince(final Claimant claimant, final Date since) {
		return allMatches(Claim.class, new Filter<Claim>(){

			@Override
			public boolean accept(Claim pojo) {
				return pojo.getClaimant() == claimant && pojo.getDate() != null && pojo.getDate().isGreaterThan(since);
			}});
	}

    
}
