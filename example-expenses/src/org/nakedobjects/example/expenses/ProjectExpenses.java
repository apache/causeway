
package org.nakedobjects.example.expenses;

import org.nakedobjects.application.Title;
import org.nakedobjects.application.value.Money;

import java.util.Enumeration;
import java.util.Vector;


public class ProjectExpenses extends BaseObject {
    private Vector expenseItems;

    /** Creates a new instance of Account */
    public ProjectExpenses() {
        expenseItems = new Vector();
    }

    public static String fieldOrder() {
        return "expense items, total";
    }

    public Title title() {
        return deriveTotal().title();
    }

    public Money deriveTotal() {
        Money total = new Money();
        Enumeration e = getExpenseItems().elements();

        while (e.hasMoreElements()) {
            Expense newExpenseItem = (Expense) e.nextElement();
            total.add(newExpenseItem.getAmount());
        }

        return total;
    }

    public Vector getExpenseItems() {
        return expenseItems;
    }
    
    public void addToExpenseItems(Expense expense) {
        expenseItems.addElement(expense);
        objectChanged();
    }

    public void removeFromExpenseItems(Expense expense) {
        expenseItems.removeElement(expense);
        objectChanged();
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
