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

import java.util.Enumeration;

import org.nakedobjects.object.AbstractNakedObject;
import org.nakedobjects.object.Title;
import org.nakedobjects.object.collection.InternalCollection;
import org.nakedobjects.object.value.Money;
import org.nakedobjects.object.value.TextString;


public class Account extends AbstractNakedObject {
    private final TextString accountNumber;
    private final InternalCollection outStandingClaims;
    private final InternalCollection claims;
    private final InternalCollection payments;

    public Account() {
        accountNumber = new TextString();
        outStandingClaims = createInternalCollection(Claim.class);
        claims = createInternalCollection(Claim.class);
        payments = createInternalCollection(Payment.class);
    }

    public Title title() {
        return new Title(getAccountNumber(), "New Account");
    }

    public Money deriveBalance() {
        Money balance = new Money();
        Enumeration claims = getClaims().elements();

        while (claims.hasMoreElements()) {
            Claim newClaim = (Claim) claims.nextElement();
            balance.add(newClaim.deriveTotal());
        }

        Enumeration payments = getPayments().elements();

        while (payments.hasMoreElements()) {
            Payment newPayment = (Payment) payments.nextElement();
            balance.subtract(newPayment.getPaymentAmount());
        }

        return balance;
    }

    public InternalCollection getClaims() {
        return claims;
    }

    public InternalCollection getPayments() {
        return payments;
    }

    public TextString getAccountNumber() {
        return accountNumber;
    }

    public InternalCollection getOutStandingClaims() {
        return outStandingClaims;
    }

    public void actionBalanceAccount() {
        Payment newPayment = (Payment) createInstance(Payment.class);
        newPayment.getPaymentAmount().setValue(deriveBalance());
        getPayments().add(newPayment);

        Enumeration claims = getClaims().elements();

        while (claims.hasMoreElements()) {
            Claim newClaim = (Claim) claims.nextElement();
            Enumeration expenses = newClaim.getExpenses().elements();

            while (expenses.hasMoreElements()) {
                Expense newExpenseItem = (Expense) expenses.nextElement();
                newExpenseItem.getStatus().setValue("Paid");
                newExpenseItem.objectChanged();
            }
        }
    }

    public Payment actionGenerateAdvance() {
        Payment newPayment = (Payment) createInstance(Payment.class);

        getPayments().add(newPayment);

        return newPayment;
    }
}
