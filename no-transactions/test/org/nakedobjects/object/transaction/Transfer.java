
package org.nakedobjects.object.transaction;

import Title;

import org.nakedobjects.object.TransactionException;
import org.nakedobjects.object.control.ActionAbout;
import org.nakedobjects.object.control.FieldAbout;
import org.nakedobjects.object.defaults.value.Date;
import org.nakedobjects.object.defaults.value.Money;

import java.util.Vector;


public class Transfer {
    private static final long serialVersionUID = 1L;

    public static String fieldOrder() {
        return "number, date, from account, to account, amount";
    }

    public final Money amount = new Money();
    public final Date date = new Date();
    public Account fromAccount;
     public Account toAccount;

    public void aboutActionApply(ActionAbout about) {
        if (isPosted()) {
            about.unusable("Transaction already posted");
        } else {
            about.unusableOnCondition(amount.doubleValue() == 0.0, "An amount must be specified");

            if (fromAccount != null) {
                about.unusableOnCondition(!fromAccount.canWithdraw(this), "Not enough funds in account");
            } else {
                about.unusable("No 'from' account");
            }

            about.unusableOnCondition(toAccount == null, "No 'to' account");
        }
    }

    public void aboutActionApplyButFail(ActionAbout about) {}

    public void aboutActionReverse(ActionAbout about) {
        about.unusableOnCondition(!isPosted(), "Must be a posted Transaction");
    }

    public void aboutFieldDefault(FieldAbout about) {
        about.unmodifiableOnCondition(isPosted(), "No changes allowed once posted");
    }

    public void actionApply() throws TransactionException {
        Vector transfers = getFromAccount().getTransfers();
        transfers.add(this);
        
        transfers = getToAccount().getTransfers();
        transfers.add(this);

        // withdraw from one account
        getFromAccount().withdraw(this);

        // deposit into another account
        getToAccount().deposit(this);

        // set values
        date.today();
  
        // if overdrawn then rollback
        if (getFromAccount().getBalance().doubleValue() < 0) { throw new TransactionException(); }

 //       objectChanged();
    }

    public void actionApplyButFail() throws TransactionException {
        actionApply();
        throw new TransactionException();
    }

    public Transfer actionReverse() throws TransactionException {
        Transfer reverse = new Transfer();
        reverse.created();
        reverse.setFromAccount(getToAccount());
        reverse.setToAccount(getFromAccount());
        reverse.getAmount().setValue(getAmount());

        reverse.actionApply();

        return reverse;
    }

    public void created() {
        date.clear();
     }

    public Money getAmount() {
        return amount;
    }

    public Date getDate() {
        return date;
    }

    public Account getFromAccount() {
      //  resolve(fromAccount);

        return fromAccount;
    }

    public Account getToAccount() {
   //     resolve(toAccount);

        return toAccount;
    }

    private boolean isPosted() {
        return !date.isEmpty();
    }

    public void setFromAccount(Account account) {
        fromAccount = account;
 //       objectChanged();
    }

    public void setToAccount(Account account) {
        toAccount = account;
    //    objectChanged();
    }

    public Title title() {
        return date.title().append(amount.titleString());
    }

    public void aboutAmount(FieldAbout about) {
        about.unusable();
    }

    public void aboutDate(FieldAbout about) {
        about.unusable();
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
