/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */


package org.apache.isis.example.library.dom;

import org.apache.isis.applib.annotation.DescribedAs;
import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.value.Date;


public class Loan extends AbstractLibraryObject {
    static final String EXTENDED = "Extended";
    static final String ON_LOAN = "On-Loan";
    static final String OVERDUE = "Overdue";
    static final String RETURNED = "Returned";

    public static String fieldOrder() {
        return "book, lent to, date, return by";
    }

    private Book book;
    private Date date;
    private Member lentTo;
    private String loanState;
    private Date returnBy;
    private Date returned;

    public void modifyBook(final Book book) {
        setBook(book);
        book.setOnLoan(this);
    }

    public void modifyLentTo(final Member lentTo) {
        lentTo.addToLoans(this);
    }

    public void created() {
        date = new Date();
        returnBy = new Date();
        returnBy = returnBy.add(0, 0, 21);
        loanState = ON_LOAN;
    }

    public void clearBook() {
        setBook(null);
        book.setOnLoan(null);
    }

    public void clearLentTo() {
        lentTo.removeFromLoans(this);
    }

    @Disabled
    public Book getBook() {
        load(book);
        return book;
    }

    @Disabled
    public Date getDate() {
        load(date);
        return date;
    }

    @Disabled
    public Member getLentTo() {
        load(lentTo);
        return lentTo;
    }

    @Disabled
    public String getLoanState() {
        return loanState;
    }

    @Disabled
    public Date getReturnBy() {
        load(returnBy);
        return returnBy;
    }

    @Disabled
    public Date getReturned() {
        load(returned);
        return returned;
    }

    @DescribedAs("Extend the loan period by a further three weeks")
    public void renew() {
        returnBy = returnBy.add(0, 0, 21);
        loanState = EXTENDED;
        message("Book loan extended until " + returnBy.title());
        changed();
    }

    @DescribedAs("Check the book back in after a loan")
    public void returnBook() {
        getLentTo().removeFromLoans(this);
        getBook().setOnLoan(null);
        returned = new Date();
        loanState = RETURNED;
        message("Book returned");
        changed();
    }

    public void setBook(final Book book) {
        this.book = book;
        changed();
    }

    public void setDate(final Date date) {
        this.date = date;
        changed();
    }

    public void setLentTo(final Member lentTo) {
        this.lentTo = lentTo;
        changed();
    }

    public void setLoanState(final String loanState) {
        this.loanState = loanState;
        changed();
    }

    public void setReturnBy(final Date returnBy) {
        load(returnBy);
        this.returnBy = returnBy;
    }

    public void setReturned(final Date returned) {
        this.returned = returned;
        changed();
    }

    public String title() {
        Book book = getBook();
        return (book == null ? "" : book.title() + ", ") + loanState;
    }

    public String disableReturnBook() {
        if (getBook() == null) {
            return "Loan must be on a book";
        }
        if (getLentTo() == null) {
            return "Loan must be on a member";
        }
        if (loanState.equals(RETURNED)) {
            return "Can only return a book that is out on loan";
        }
        return null;
    }

    public String disableRenew() {
        if (getBook() == null) {
            return "Loan must be on a book";
        }
        if (getLentTo() == null) {
            return "Loan must be on a member";
        }
        if (loanState.equals(RETURNED)) {
            return "Can only review a book that is out on loan";
        }
        return null;
    }
}
