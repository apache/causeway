package org.apache.isis.extensions.bdd.testapp.claims.dom.claim;

public interface Claimant {

	Approver getApprover();

	void addToMostRecentClaims(Claim claim);
    
}
