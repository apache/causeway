package org.apache.isis.extensions.bdd.testapp.claims.dom.employee;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.extensions.bdd.testapp.claims.dom.claim.Approver;
import org.apache.isis.extensions.bdd.testapp.claims.dom.claim.Claim;
import org.apache.isis.extensions.bdd.testapp.claims.dom.claim.Claimant;


public class Employee extends AbstractDomainObject implements Claimant, Approver {

	// {{ Title
    public String title() {
        return getName();
    }
    // }}

    
    // {{ Name
    private String name;
    @MemberOrder(sequence="1")
    public String getName() {
        return name;
    }
    public void setName(String lastName) {
        this.name = lastName;
    }
    // }}
    

    // {{ Approver
	private Approver approver;

	@MemberOrder(sequence = "2")
	public Approver getApprover() {
		return approver;
	}

	public void setApprover(final Approver approver) {
		this.approver = approver;
	}

	public String validateApprover(final Approver approver) {
		if (approver == null)
			return null;
		if (approver == this) {
			return "Cannot act as own approver";
		}
		return null;
	}
	// }}


 
    
    // {{ SomeHiddenProperty
	private String someHiddenProperty;

	@Hidden
	@MemberOrder(sequence = "1")
	public String getSomeHiddenProperty() {
		return someHiddenProperty;
	}

	public void setSomeHiddenProperty(final String someHiddenProperty) {
		this.someHiddenProperty = someHiddenProperty;
	}

	public void modifySomeHiddenProperty(final String someHiddenProperty) {
		// check for no-op
		if (someHiddenProperty == null || someHiddenProperty.equals(getSomeHiddenProperty())) {
			return;
		}
		// associate new
		setSomeHiddenProperty(someHiddenProperty);
		// additional business logic
		onModifySomeHiddenProperty(someHiddenProperty);
	}

	public void clearSomeHiddenProperty() {
		// check for no-op
		if (getSomeHiddenProperty() == null) {
			return;
		}
		// dissociate existing
		setSomeHiddenProperty(null);
		// additional business logic
		onClearSomeHiddenProperty();
	}

	protected void onModifySomeHiddenProperty(final String someHiddenProperty) {
	}

	protected void onClearSomeHiddenProperty() {
	}
	// }}

	
	// {{ SomeDisabledProperty
	private String someDisabledProperty;

	@Disabled
	@MemberOrder(sequence = "1")
	public String getSomeDisabledProperty() {
		return someDisabledProperty;
	}

	public void setSomeDisabledProperty(final String someDisabledProperty) {
		this.someDisabledProperty = someDisabledProperty;
	}

	public void modifySomeDisabledProperty(final String someDisabledProperty) {
		// check for no-op
		if (someDisabledProperty == null || someDisabledProperty.equals(getSomeDisabledProperty())) {
			return;
		}
		// associate new
		setSomeDisabledProperty(someDisabledProperty);
		// additional business logic
		onModifySomeDisabledProperty(someDisabledProperty);
	}

	public void clearSomeDisabledProperty() {
		// check for no-op
		if (getSomeDisabledProperty() == null) {
			return;
		}
		// dissociate existing
		setSomeDisabledProperty(null);
		// additional business logic
		onClearSomeDisabledProperty();
	}

	protected void onModifySomeDisabledProperty(final String someDisabledProperty) {
	}

	protected void onClearSomeDisabledProperty() {
	}
	// }}


	// {{ Favorite
	private Employee favorite;

	@Optional
	@MemberOrder(sequence = "3")
	public Employee getFavorite() {
		return favorite;
	}

	public void setFavorite(final Employee favorite) {
		this.favorite = favorite;
	}

	public void modifyFavorite(final Employee favorite) {
		// check for no-op
		if (favorite == null || favorite.equals(getFavorite())) {
			return;
		}
		// associate new
		setFavorite(favorite);
		// additional business logic
		onModifyFavorite(favorite);
	}

	public void clearFavorite() {
		// check for no-op
		if (getFavorite() == null) {
			return;
		}
		// dissociate existing
		setFavorite(null);
		// additional business logic
		onClearFavorite();
	}

	protected void onModifyFavorite(final Employee favorite) {
	}

	protected void onClearFavorite() {
	}
	// }}

	
	// {{ MostRecentClaims
	private Set<Claim> mostRecentClaims = new LinkedHashSet<Claim>();

	@MemberOrder(sequence = "1")
	public Set<Claim> getMostRecentClaims() {
		return mostRecentClaims;
	}

	public void setMostRecentClaims(final Set<Claim> mostRecentClaims) {
		this.mostRecentClaims = mostRecentClaims;
	}

	public void addToMostRecentClaims(final Claim claim) {
		// check for no-op
		if (claim == null || getMostRecentClaims().contains(claim)) {
			return;
		}
		// associate new
		getMostRecentClaims().add(claim);
		// additional business logic
		onAddToMostRecentClaims(claim);
	}

	public void removeFromMostRecentClaims(final Claim claim) {
		// check for no-op
		if (claim == null || !getMostRecentClaims().contains(claim)) {
			return;
		}
		// dissociate existing
		getMostRecentClaims().remove(claim);
		// additional business logic
		onRemoveFromMostRecentClaims(claim);
	}

	protected void onAddToMostRecentClaims(final Claim claim) {
	}

	protected void onRemoveFromMostRecentClaims(final Claim claim) {
	}
	public String validateAddToMostRecentClaims(final Claim claim) {
		if (claim.getClaimant() != this) {
			return "cannot add claim made by some other claimant";
		}
		return null;
	}

	public String validateRemoveFromMostRecentClaims(final Claim claim) {
		if (claim.getClaimant() != this) {
			return "cannot remove claim made by some other claimant";
		}
		return null;
	}


	// }}


	
	// {{ SomeHiddenCollection
	private List<Claim> someHiddenCollection = new ArrayList<Claim>();

	@Hidden
	@MemberOrder(sequence = "1")
	public List<Claim> getSomeHiddenCollection() {
		return someHiddenCollection;
	}

	public void setSomeHiddenCollection(final List<Claim> someHiddenCollection) {
		this.someHiddenCollection = someHiddenCollection;
	}

	public void addToSomeHiddenCollection(final Claim claim) {
		// check for no-op
		if (claim == null || getSomeHiddenCollection().contains(claim)) {
			return;
		}
		// associate new
		getSomeHiddenCollection().add(claim);
		// additional business logic
		onAddToSomeHiddenCollection(claim);
	}

	public void removeFromSomeHiddenCollection(final Claim claim) {
		// check for no-op
		if (claim == null || !getSomeHiddenCollection().contains(claim)) {
			return;
		}
		// dissociate existing
		getSomeHiddenCollection().remove(claim);
		// additional business logic
		onRemoveFromSomeHiddenCollection(claim);
	}

	protected void onAddToSomeHiddenCollection(final Claim claim) {
	}

	protected void onRemoveFromSomeHiddenCollection(final Claim claim) {
	}
	// }}


	// {{ SomeDisabledCollection
	private List<Claim> someDisabledCollection = new ArrayList<Claim>();

	@Disabled
	@MemberOrder(sequence = "1")
	public List<Claim> getSomeDisabledCollection() {
		return someDisabledCollection;
	}

	public void setSomeDisabledCollection(final List<Claim> someDisabledCollection) {
		this.someDisabledCollection = someDisabledCollection;
	}

	public void addToSomeDisabledCollection(final Claim claim) {
		// check for no-op
		if (claim == null || getSomeDisabledCollection().contains(claim)) {
			return;
		}
		// associate new
		getSomeDisabledCollection().add(claim);
		// additional business logic
		onAddToSomeDisabledCollection(claim);
	}

	public void removeFromSomeDisabledCollection(final Claim claim) {
		// check for no-op
		if (claim == null || !getSomeDisabledCollection().contains(claim)) {
			return;
		}
		// dissociate existing
		getSomeDisabledCollection().remove(claim);
		// additional business logic
		onRemoveFromSomeDisabledCollection(claim);
	}

	protected void onAddToSomeDisabledCollection(final Claim claim) {
	}

	protected void onRemoveFromSomeDisabledCollection(final Claim claim) {
	}
	// }}


	
	// {{ someHiddenAction
	@Hidden
	@MemberOrder(sequence = "1")
	public void someHiddenAction() {
		return;
	}
	// }}


	// {{ someDisabledAction
	@Disabled
	@MemberOrder(sequence = "1")
	public void someDisabledAction() {
		return;
	}
	// }}


	// {{ addTwoPositiveNumbers
	@MemberOrder(sequence = "1")
	public int addTwoPositiveNumbers(int a, int b) {
		return a+b;
	}
	public String validateAddTwoPositiveNumbers(int a, int b) {
		if (a < 0 || b < 0) {
			return "numbers must be positive";
		}
		return null;
	}
	// }}


	// {{ SomePropertyWithChoices
	private String somePropertyWithChoices;

	@MemberOrder(sequence = "1")
	public String getSomePropertyWithChoices() {
		return somePropertyWithChoices;
	}

	public void setSomePropertyWithChoices(final String somePropertyWithChoices) {
		this.somePropertyWithChoices = somePropertyWithChoices;
	}

	public void modifySomePropertyWithChoices(final String somePropertyWithChoices) {
		// check for no-op
		if (somePropertyWithChoices == null || somePropertyWithChoices.equals(getSomePropertyWithChoices())) {
			return;
		}
		// associate new
		setSomePropertyWithChoices(somePropertyWithChoices);
		// additional business logic
		onModifySomePropertyWithChoices(somePropertyWithChoices);
	}

	public void clearSomePropertyWithChoices() {
		// check for no-op
		if (getSomePropertyWithChoices() == null) {
			return;
		}
		// dissociate existing
		setSomePropertyWithChoices(null);
		// additional business logic
		onClearSomePropertyWithChoices();
	}

	protected void onModifySomePropertyWithChoices(final String somePropertyWithChoices) {
	}

	protected void onClearSomePropertyWithChoices() {
	}
	public List<String> choicesSomePropertyWithChoices() {
		return Arrays.asList("foo", "bar");
	}
	// }}


	
	// {{ SomePropertyWithDefault
	private String somePropertyWithDefault;

	@MemberOrder(sequence = "1")
	public String getSomePropertyWithDefault() {
		return somePropertyWithDefault;
	}

	public void setSomePropertyWithDefault(final String somePropertyWithDefault) {
		this.somePropertyWithDefault = somePropertyWithDefault;
	}

	public void modifySomePropertyWithDefault(final String somePropertyWithDefault) {
		// check for no-op
		if (somePropertyWithDefault == null || somePropertyWithDefault.equals(getSomePropertyWithDefault())) {
			return;
		}
		// associate new
		setSomePropertyWithDefault(somePropertyWithDefault);
		// additional business logic
		onModifySomePropertyWithDefault(somePropertyWithDefault);
	}

	public void clearSomePropertyWithDefault() {
		// check for no-op
		if (getSomePropertyWithDefault() == null) {
			return;
		}
		// dissociate existing
		setSomePropertyWithDefault(null);
		// additional business logic
		onClearSomePropertyWithDefault();
	}

	protected void onModifySomePropertyWithDefault(final String somePropertyWithDefault) {
	}

	protected void onClearSomePropertyWithDefault() {
	}
	
	public String defaultSomePropertyWithDefault() {
		return "Foo";
	}
	// }}
	

	// {{ SomeActionWithParameterDefaults
	@MemberOrder(sequence = "1")
	public int someActionWithParameterDefaults(final int param0, final int param1) {
		return param0 + param1;
	}
	public int default0SomeActionWithParameterDefaults() {
		return 5;
	}
	// }}

	// {{ someActionWithParameterChoices
	@MemberOrder(sequence = "1")
	public int someActionWithParameterChoices(final int param0, final int param1) {
		return param0 - param1;
	}
	public List<Integer> choices0SomeActionWithParameterChoices() {
		return Arrays.asList(1, 2, 3);
	}
	// }}






	
}


// Copyright (c) Naked Objects Group Ltd.
