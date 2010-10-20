package org.apache.isis.extensions.groovy.testapp.claims.dom.claim;

import java.util.List;

import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.value.Date;

@Named("Claims")
interface ClaimRepository {

    List<Claim> allClaims()

    List<Claim> findClaims(
    		@Named("Description") 
    		String description
    		)

    List<Claim> claimsFor(Claimant claimant)

    List<Claim> claimsSince(Claimant claimant, Date since)
}
