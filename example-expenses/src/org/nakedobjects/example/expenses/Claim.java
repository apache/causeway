/*
    Naked Objects - a framework that exposes behaviourally complete
    business objects directly to the user.
    Copyright (C) 2000 - 2003  Naked Objects Group Ltd

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

    The authors can be contacted via www.nakedobjects.org (the
    registered address of Naked Objects Group is Kingsway House, 123 Goldworth
    Road, Woking GU21 1NR, UK).
*/

package org.nakedobjects.example.expenses;

import java.util.Enumeration;
import java.util.Vector;

import org.nakedobjects.object.AbstractNakedObject;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.Title;
import org.nakedobjects.object.collection.InternalCollection;
import org.nakedobjects.object.control.ActionAbout;
import org.nakedobjects.object.control.FieldAbout;
import org.nakedobjects.object.value.Case;
import org.nakedobjects.object.value.Money;
import org.nakedobjects.object.value.TextString;


public class Claim extends AbstractNakedObject {
	private Employee claimant;
	private final InternalCollection expenseItems;
	private final TextString status;

	public Claim() {
		expenseItems = new InternalCollection(Expense.class, this);
		status = new TextString();
		status.setAbout(FieldAbout.READ_ONLY);
	}

	public static String fieldOrder() {
		return "claimant, status, expenses";
	}

	public void setClaimant(Employee newClaimant) {
		this.claimant = newClaimant;
		objectChanged();
	}

	public Employee getClaimant() {
		resolve(claimant);

		return claimant;
	}

	public InternalCollection getExpenses() {
		return expenseItems;
	}

	public static Claim actionNewClaim(Employee forEmployee) {
		Claim newClaim = (Claim) createInstance(Claim.class);
		newClaim.associateClaimant(forEmployee);

		return newClaim;
	}

	public TextString getStatus() {
		return status;
	}

	public void aboutActionAuthoriseClaim(ActionAbout about) {
		if (getClaimant() == null) {
			about.unusable("Claim requires a claimant");
		} else {
			about.unusableOnCondition(
				getClaimant().getAccount() != null, "The claimant needs an account");
			about.unusableOnCondition(
				getClaimant().getProjects() != null, "The claimant needs a project");
		}

		about.unusableOnCondition(
			!getExpenses().isEmpty(), "A claim needs to have some expense items");
		about.unusableOnCondition(getStatus().isSameAs("Finalised"), "Already claimed");
	}

	public void aboutActionPayClaim(ActionAbout about) {
		about.unusableOnCondition(! getStatus().contains("Finalised", Case.INSENSITIVE), 
				"Claim needs to be finialised before it can be paid");
	}

	public Claim actionAuthoriseClaim() {
		Vector unauthorisedExpenses = new Vector();
		Claim newClaim = null;

		Enumeration expenses = getExpenses().elements();

		while (expenses.hasMoreElements()) {
			Expense expense = (Expense) expenses.nextElement();

			if (expense.isAuthorised()) {
				expense.getProject().getExpenses().getExpenseItems().add(expense);
			}

			if (expense.isValid()) {
				if (newClaim == null) {
					newClaim = (Claim) createInstance(Claim.class);
				}

				unauthorisedExpenses.addElement(expense);

				newClaim.associateExpenses(expense);
			}
		}

		for (int i = 0; i < unauthorisedExpenses.size(); i++) {
			expenseItems.remove((NakedObject) unauthorisedExpenses.elementAt(i));
		}

		getClaimant().getAccount().getOutStandingClaims().remove(this);

		if (newClaim != null) {
			newClaim.associateClaimant(getClaimant());
		}

		getStatus().setValue("Finalised");
		getClaimant().getAccount().getClaims().add(this);

		return newClaim;
	}

	// RCM added
	public Expense actionNewExpenseItem(Project forProject) {
		Expense newExpenseItem = (Expense) createInstance(Expense.class);
		associateExpenses(newExpenseItem);
		newExpenseItem.setProject(forProject);

		return newExpenseItem;
	}

	public void actionPayClaim() {
		Payment newPayment = (Payment) createInstance(Payment.class);
		getClaimant().getAccount().getPayments().add(newPayment);
		newPayment.getPaymentAmount().setValue(deriveTotal());
		newPayment.setClaim(this);
		status.setValue("Paid");
		getClaimant().getAccount().objectChanged();
	}

	public void associateClaimant(Employee claimant) {
		claimant.getAccount().getOutStandingClaims().add(this);
		setClaimant(claimant);
	}

	public void associateExpenses(Expense expenseItem) {
		expenseItems.add(expenseItem);
		expenseItem.setClaim(this);
	}

	public void created() {
		status.setValue("Open");
	}

	public Money deriveTotal() {
		Money total = new Money();
		Enumeration e = getExpenses().elements();

		while (e.hasMoreElements()) {
			Expense newExpenseItem = (Expense) e.nextElement();
			resolve(newExpenseItem);
			total.add(newExpenseItem.getAmount());
		}

		return total;
	}

	public void dissociateClaimant(Employee claimant) {
		getClaimant().getAccount().getOutStandingClaims().remove(this);
		setClaimant(null);
	}

	public void dissociateExpenses(Expense expenseItem) {
		getExpenses().remove(expenseItem);
		expenseItem.setClaim(null);
	}

	public Title title() {
		if ((claimant == null) && (deriveTotal() == null)) {
			return new Title("New Claim");
		} else {
			return new Title(getClaimant()).append(deriveTotal()).append(getStatus());
		}
	}

	public String toString() {
		return status + super.toString();
	}
}
