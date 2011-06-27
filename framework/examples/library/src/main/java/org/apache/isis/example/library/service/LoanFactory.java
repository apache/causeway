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


package org.apache.isis.example.library.service;

import java.util.List;

import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.annotation.Exploration;
import org.apache.isis.example.library.dom.Book;
import org.apache.isis.example.library.dom.Loan;
import org.apache.isis.example.library.dom.Member;

public class LoanFactory extends AbstractFactoryAndRepository {

    public Loan createLoan(final Book book, final Member member) {
            Loan loan = (Loan) newPersistentInstance(Loan.class);
            loan.modifyBook(book);
            loan.modifyLentTo(member);
            return loan;
    }

    public String validateCreateLoan(final Book book, final Member member) {
        if (member.isCurrent()) {
            return null;
        } else {
            return "Member is not currently able to take out book";
        }
    }
    
    public List<Loan> loansFor(Member member) {
        Loan loanPattern = newTransientInstance(Loan.class);
        loanPattern.setLentTo(member);
        loanPattern.setDate(null);
        loanPattern.setReturnBy(null);
        loanPattern.setLoanState(null);
        return allMatches(Loan.class, loanPattern);
    }
    
    @Exploration
    public List<Loan> allInstances() {
        return allInstances(Loan.class);
    }
    
    public String getId() {
        return "loans";
    }
    
    public String iconName() {
        return "Loan";
    }
}

