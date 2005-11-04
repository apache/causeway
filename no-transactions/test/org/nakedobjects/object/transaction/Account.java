
package org.nakedobjects.object.transaction;


import java.util.Vector;


public class Account {
    private static final long serialVersionUID = 1L;

    public static String fieldOrder() {
        return "name, balance";
    }

    public final Money balance = new Money();
    public final TextString name = new TextString();
    public final Vector transfers = new Vector();

    public void aboutTransactions(FieldAbout about, Transfer element, boolean add) {
        about.unmodifiable("Transactions are only added by the system");
    }

    public Transfer actionCreateTransactionFrom(Account fromAccount) {
        Transfer t = new Transfer();
        t.created();
        t.setFromAccount(fromAccount);
        t.setToAccount(this);

        return t;
    }

    public boolean canWithdraw(Transfer transfer) {
        return getBalance().floatValue() - transfer.getAmount().floatValue() >= 0.0;
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

    public Vector getTransfers() {
        return transfers;
    }
    
    public void addToTransfers(Transfer transfer) {
        transfers.addElement(transfer);
        // objectChanged();
    }
    
    public void removeFromTransfers(Transfer transfer) {
        transfers.removeElement(transfer);
        // objectChanged();
    }

    public Title title() {
        return getName().title().append(getBalance().titleString());
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

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
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
