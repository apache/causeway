/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2003 Naked Objects Group
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

package org.nakedobjects.object.transaction;

import org.nakedobjects.object.AbstractNakedObject;
import org.nakedobjects.object.Title;
import org.nakedobjects.object.TransactionException;
import org.nakedobjects.object.collection.InternalCollection;
import org.nakedobjects.object.control.FieldAbout;
import org.nakedobjects.object.value.Money;
import org.nakedobjects.object.value.TextString;


public class Account extends AbstractNakedObject {
    private static final long serialVersionUID = 1L;

    public static String fieldOrder() {
        return "name, balance";
    }

    public final Money balance = new Money();
    public final TextString name = new TextString();
    public final InternalCollection transfers = new InternalCollection(Transfer.class, this);

    public void aboutTransactions(FieldAbout about, Transfer element, boolean add) {
        about.unmodifiable("Transactions are only added by the system");
    }

    public Transfer actionCreateTransactionFrom(Account fromAccount) {
        Transfer t = (Transfer) createInstance(Transfer.class);
        t.setFromAccount(fromAccount);
        t.setToAccount(this);

        return t;
    }

    public boolean canWithdraw(Transfer transfer) {
        return getBalance().doubleValue() - transfer.getAmount().doubleValue() >= 0.0;
    }

    public void deposit(Transfer transfer) {
        balance.add(transfer.getAmount());
        // objectChanged();
    }

    public Money getBalance() {
        return balance;
    }

    public TextString getName() {
        return name;
    }

    public InternalCollection getTransfers() {
        return transfers;
    }

    public Title title() {
        return getName().title().append(getBalance());
    }

    public void withdraw(Transfer transfer) throws TransactionException {
        if (canWithdraw(transfer)) {
            balance.subtract(transfer.getAmount());
            //objectChanged(); // should this be necessary?
        } else {
            throw new TransactionException("Not enough funds in " + title());
        }
    }
}