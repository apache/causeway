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
import java.util.Locale;

import org.nakedobjects.testing.AcceptanceTest;
import org.nakedobjects.testing.View;
import org.nakedobjects.utility.ComponentException;
import org.nakedobjects.utility.ConfigurationException;

public class ExampleStories extends AcceptanceTest {

	public ExampleStories(String name) {
		super(name);
	}

	public void runStories() {
		story1();
	}

	private void story1() {
		story("Make claim for new employee");

		step("Set up employee");
		View robert = getClassView("Employees").newInstance();
		robert.fieldEntry("Surname", "Matthews");

		step("Set up project");
		View ait = getClassView("Projects").newInstance();
		ait.fieldEntry("Name", "AIT phone system");
		robert.drop("Projects", ait.drag());
		
		step("Create claim for employee");
		View claim = getClassView("Claims").drop(robert.drag());
		claim.checkField("Total", "$0.00");
	}


	public void setUp() {
		Locale.setDefault(Locale.US);
		registerClass(Employee.class);
		registerClass(Claim.class);
		registerClass(Expense.class);
		registerClass(Project.class);

	}



	public static void main(String[] args) throws ConfigurationException, ComponentException {
        ExampleStories st = new ExampleStories("Example Stories");

        st.start();
	}
}
