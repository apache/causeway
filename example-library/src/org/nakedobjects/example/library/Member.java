package org.nakedobjects.example.library;

import org.nakedobjects.application.BusinessObjectContainer;
import org.nakedobjects.application.Title;
import org.nakedobjects.application.control.ActionAbout;
import org.nakedobjects.application.control.FieldAbout;
import org.nakedobjects.application.control.State;
import org.nakedobjects.application.value.Date;
import org.nakedobjects.application.value.Logical;
import org.nakedobjects.application.value.MultilineTextString;
import org.nakedobjects.application.value.SimpleState;
import org.nakedobjects.application.value.TextString;

import java.util.Vector;


public class Member extends BaseObject {
	private final TextString name = new TextString();
	private final MultilineTextString address = new MultilineTextString();
	private final TextString phone = new TextString(); 
	private final TextString email = new TextString(); 
	private final Logical junior = new Logical();
	private final TextString code = new TextString();
	private final Date joined = new Date();
	
	private final Vector loans = new Vector();
	
	static final State NEW = new SimpleState(1, "New member");
	static final State VERIFIED = new SimpleState(2, "Verified");
	static final State EXPIRED = new SimpleState(3, "Expired");
	
	private State state = new SimpleState(new State[] {NEW, VERIFIED, EXPIRED});
	
	   public void setContainer(BusinessObjectContainer container) {
	        this.container = container;
	    }

	public static String fieldOrder() {
		return "name, junior, address, phone, email, loans, status";
	}
	
	public static void aboutNewInstance(ActionAbout about) {
		about.visibleOnlyToRole(Roles.ADMIN);
	}
	
	public void aboutFieldDefault(FieldAbout about) {
		about.unmodifiable();
	}
	
	public void aboutActionLoan(ActionAbout about, Book book) {
		about.setDescription("Take out " + book.title() + " on a 3 week loan");
		about.unusableOnCondition(book.getOnLoan() != null, "Book already on loan");
		about.unusableOnCondition(getLoans().size() > 2, "Reached borrowing limit");
	}
	
	public Loan actionLoan(Book book) {
		Loan loan = (Loan) container.createInstance(Loan.class);
		loan.associateBook(book);
		loan.associateLentTo(this);
		return loan;
	}
	
	public void created() {
		state.changeTo(NEW);
		junior.reset();
		code.setValue("M" + container.serialNumber("members"));
	}
	
	public Title title() {
		return name.title();
	}
	public MultilineTextString getAddress() {
		return address;
	}

	public TextString getEmail() {
		return email;
	}

	public Logical getJunior() {
		return junior;
	}
	
	public void aboutJoined(	FieldAbout about) {
		about.modifiableOnlyByRole(Roles.ADMIN);
	}
	
	public Date getJoined() {
		return joined;
	}

	public void aboutLoans(FieldAbout about, Loan loan, boolean add) {
		about.unmodifiableOnCondition(add, "no adding loans by the user");
		about.modifiableOnlyByRole(Roles.ADMIN);
	}

	public Vector getLoans() {
		return loans;
	}
	
	public void addLoans(Loan loan) {
		loans.add(loan);
		loan.setLentTo(this);
	}
	
	public void removeLoans(Loan loan) {
		loans.remove(loan);
		loan.setLentTo(null);
	}
	
	public TextString getName() {
		return name;
	}
	
	public State getStatus() {
		return state;
	}
	
	public TextString getPhone() {
		return phone;
	}

	public void aboutCode(FieldAbout about) {
		about.unmodifiable();
	}
	
	
	public TextString getCode() {
		return code;
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

