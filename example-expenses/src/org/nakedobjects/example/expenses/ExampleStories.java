package org.nakedobjects.example.expenses;
import org.nakedobjects.exploration.AbstractExplorationFixture;
import org.nakedobjects.object.value.Money;
import org.nakedobjects.utility.ComponentException;
import org.nakedobjects.utility.ConfigurationException;
import org.nakedobjects.xat.AcceptanceTestCase;
import org.nakedobjects.xat.TestObject;

import java.util.Locale;

public class ExampleStories extends AcceptanceTestCase {

	public ExampleStories(String name) {
		super(name);
	}

	public  void test1() {
		subtitle("Make claim for new employee");

		nextStep("Set up employee");
		TestObject robert = getTestClass("Employees").newInstance();
		robert.fieldEntry("Surname", "Matthews");

		nextStep("Set up project");
		TestObject ait = getTestClass("Projects").newInstance();
		ait.fieldEntry("Name", "AIT phone system");
		robert.associate("Projects", ait);
		
		nextStep("Create claim for employee");
		TestObject claim = getTestClass("Claims").invokeAction("New Claim", robert);
		claim.assertFieldContains("Total", new Money(0.0));
	}


	public void setUpFixtures() {
	    addFixture(new AbstractExplorationFixture() {
	       public void install() {
	           Locale.setDefault(Locale.US);
	           registerClass(Employee.class);
	           registerClass(Claim.class);
	           registerClass(Expense.class);
	           registerClass(Project.class);    
	       }
	    });
		
	}
	
	

	public static void main(String[] args) throws ConfigurationException, ComponentException {
        junit.textui.TestRunner.run(ExampleStories.class);
	}
}

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