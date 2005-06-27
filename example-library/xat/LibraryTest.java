import org.nakedobjects.example.library.Book;
import org.nakedobjects.example.library.Loan;
import org.nakedobjects.example.library.Member;
import org.nakedobjects.example.xat.JavaAcceptanceTestCase;
import org.nakedobjects.reflector.java.fixture.JavaFixture;


public class LibraryTest extends JavaAcceptanceTestCase {

	public static void main(String[] args) {
	       junit.textui.TestRunner.run(LibraryTest.class);
	}
	
	public LibraryTest(String name) {
		super(name);
	}

	public void testEverything() {
		title("Stock library with books");
		nextStep();
		
//		TestObject newBook = getTestClass(Book.class.getName()).newInstance();
/*		newBook.fieldEntry("Title", "The Cat in the Hat");
		newBook.fieldEntry("Author", "Dr Suess");
		newBook.fieldEntry("Code", "9282821");
		
		nextStep();
		newBook = getTestClass(Book.class.getName()).newInstance();
		newBook.fieldEntry("Title", "The Sneetches");
		newBook.fieldEntry("Author", "Dr Suess");
		newBook.fieldEntry("Code", "9282321");
		
		
		subtitle("Register a new member");
		nextStep("Create a new instance");
		TestObject newMember = getTestClass(Member.class.getName()).newInstance();
		nextStep("Enter data");
		newMember.fieldEntry("Name", "Henry Muir");
		newMember.fieldEntry("Address", "18 The Cresent, Popewell");
		newMember.fieldEntry("Phone", "0101 8372821");

		newMember.assertFieldContains("Junior", "FALSE");
		
		
		subtitle("Check out a book");
		nextStep("Find chosen book");
		TestObject book = getTestClass(Book.class.getName()).findInstance("The Cat in the Hat");

		//	View loan = newMember.drop(book.drag());
		
		note("The generated loan object details who is borrowing what and when.");
		
//		loan.assertFieldContains("Lent To", newMember);
//		loan.assertFieldContains("Book", book);
	
		
		
		subtitle("Manually check out a book");
		nextStep("Although dropping a book onto a member checks out a book, it can be " +
				"done manually by creating a loan object and associating the member and book.");
		TestObject loan = getTestClass(Loan.class.getName()).newInstance();

		nextStep("specify the book");
		book = getTestClass(Book.class.getName()).findInstance("Cat in the Hat");
		loan.associate("Book", book);
		
		nextStep("Specify the member");
		TestObject member = getTestClass("Members").findInstance("Henry Muir");
		loan.invokeAction("Lent To", member);
	
		note("Now the loan knows the book and the member:");
		loan.assertFieldContains("Book", book);
		loan.assertFieldContains("Lent To", member);
		
		note("And both the book and member know the loan.");
		book.assertFieldContains("On Loan", loan);
		member.assertFieldContains("Loans", loan);
		
		
		
		subtitle("Renew a book");
//		loan = getTestClass("Loans").findInstance("Cat in the Hat");
		loan.invokeAction("Renew");
		loan.assertFieldContains("Return By", "Feb 19, 2004");
		
		
		
		subtitle("Return a book");
//		loan = getTestClass("Loans").findInstance("Cat in the Hat");
		book = (TestObject) loan.getField("Book");
		member = (TestObject) loan.getField("Lent To");
//		loan.rightClick("Returned");
		book.assertFieldContains("On Loan", "");
		member.assertFieldContains("Loans", "");
		
		// how do we test the dates?
		 * 
		 */
	}

	
    protected void setUpFixtures() {
        addFixture(new JavaFixture() {
            public void install() {
        		registerClass(Member.class);
        		registerClass(Book.class);
        		registerClass(Loan.class);                
            }
        });
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
