

package org.nakedobjects.example.library;

import org.nakedobjects.application.Title;
import org.nakedobjects.application.control.ActionAbout;
import org.nakedobjects.application.control.FieldAbout;
import org.nakedobjects.application.control.State;
import org.nakedobjects.application.control.StatefulObject;
import org.nakedobjects.application.valueholder.Date;
import org.nakedobjects.application.valueholder.SimpleState;


public class Loan extends BaseObject implements StatefulObject {
	private Book book;
	private Member lentTo;
	private final Date date = new Date();
	private final Date returnBy = new Date();
	private final Date returned = new Date();
	
	static final State ON_LOAN = new SimpleState(1, "On-Loan");
	static final State EXTENDED = new SimpleState(2, "Extended");
	static final State OVERDUE = new SimpleState(3, "Overdue");
	static final State RETURNED = new SimpleState(4, "Returned");
	
	private State loanState = new SimpleState(new State[] {ON_LOAN, EXTENDED, OVERDUE, RETURNED});
	
	
	public static String fieldOrder() {
		return "book, lent to, date, return by";
	}
	
	public void aboutFieldDefault(FieldAbout about) {
		about.unmodifiable();
	}
	
	public void created() {
		returnBy.add(0, 0, 21);
		returned.clear();
		loanState.changeTo(ON_LOAN);
	}
	
	public void aboutActionRenew(ActionAbout about) {
		about.setDescription("Extend the loan period by a further three weeks");
		about.unusableOnCondition(getBook() == null, "Loan must be on a book");
		about.unusableOnCondition(getLentTo() == null, "Loan must be on a member");
		about.visibleOnlyToRole(Roles.LENDER);
		about.usableOnlyInState(ON_LOAN);
	}
	
	public void actionRenew() {
		returnBy.add(0, 0, 21);
		loanState.changeTo(EXTENDED);
		objectChanged();
	}
	
	public void aboutActionReturned(ActionAbout about) {
		about.setDescription("Check the book back in after a loan");
		about.unusableOnCondition(getBook() == null, "Loan must be on a book");
		about.unusableOnCondition(getLentTo() == null, "Loan must be on a member");
		about.visibleOnlyToRole(Roles.LENDER);
		about.usableOnlyInStates(new State[] {ON_LOAN, EXTENDED});
	}
	
	public void actionReturned() {
		getLentTo().getLoans().remove(this);
		getBook().setOnLoan(null);
		loanState.changeTo(RETURNED);
		objectChanged();
	}
	
	public Book getBook() {
		resolve(book);
		return book;
	}

	public void setBook(Book book) {
		this.book = book;
		objectChanged();
	}
	
	public void associateBook(Book book) {
		setBook(book);
		book.setOnLoan(this);
	}

	public void dissociateBook(Book book) {
		setBook(null);
		book.setOnLoan(null);
	}

	public Date getDate() {
		return date;
	}

	public Member getLentTo() {
		resolve(lentTo);
		return lentTo;
	}

	public void setLentTo(Member lentTo) {
		this.lentTo = lentTo;
		objectChanged();
	}

	public void associateLentTo(Member lentTo) {
		lentTo.addLoans(this);
	}

	public void dissociateLentTo(Member lentTo) {
		lentTo.removeLoans(this);
	}

	public Date getReturnBy() {
		return returnBy;
	}

	public Date getReturned() {
		return returned;
	}

	public Title title() {
		return new Title(getBook()).append(loanState);
	}

	public State getState() {
		return loanState;
	}
}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

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
