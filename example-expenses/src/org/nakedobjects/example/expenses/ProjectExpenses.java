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

/*
 * ProjectSummary.java
 *
 * Created on 04 September 2002, 17:00
 */
package org.nakedobjects.example.expenses;

import java.util.Enumeration;

/**
 *
 * @author  DBEHAN
 */
import org.nakedobjects.object.AbstractNakedObject;
import org.nakedobjects.object.Title;
import org.nakedobjects.object.collection.InternalCollection;
import org.nakedobjects.object.value.*;


public class ProjectExpenses extends AbstractNakedObject {
    private InternalCollection expenseItems;

    /** Creates a new instance of Account */
    public ProjectExpenses() {
        expenseItems = createInternalCollection(Expense.class);
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

    public InternalCollection getExpenseItems() {
        resolve(expenseItems);

        return expenseItems;
    }
}
