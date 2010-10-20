package org.apache.isis.extensions.restful.testapp.claims.dom.claim;

import java.util.List;

import org.apache.isis.applib.annotation.Named;


@Named("Claims")
public interface ClaimRepository {

    public List<Claim> allClaims();

    public List<Claim> findClaims(
    		@Named("Description") 
    		String description
    		);

    public List<Claim> claimsFor(Claimant claimant);

	public Claim newClaim(Claimant claimant);

}
