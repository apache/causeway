package org.nakedobjects.example;

import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.defaults.AbstractNakedObject;
import org.nakedobjects.object.defaults.value.Date;
import org.nakedobjects.object.defaults.value.TextString;


public class Account extends AbstractNakedObject {
    private final TextString account = new TextString();
    private final Date dateOpened = new Date();
    private Customer customer;
    private final InternalCollection transactions = createInternalCollection(Transaction.class);

    public TextString getAccount() {
        return account;
    }

    public Customer getCustomer() {
        resolve(customer);
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
        objectChanged();
    }

    public Date getDateOpened() {
        return dateOpened;
    }

    public InternalCollection getTransactions() {
        return transactions;
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2004 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */