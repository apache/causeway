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

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.applib.annotation.DescribedAs;
import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.annotation.MultiLine;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.value.Date;
import org.apache.isis.example.library.service.LoanFactory;


public class Member extends AbstractLibraryObject {
    static final String EXPIRED ="Expired";
    static final String NEW = "New member";
    static final String VALID = "Valid";
    static final String SUSPENDED ="Suspended";
    
    public static String fieldOrder() {
        return "name, junior, address, phone, email, loans, status";
    }
    
    private String address;
    private String code;
    private String email;
    private Date joined;
    private boolean junior;
    private List loans = new ArrayList();
    private String name;
    private String phone;
    private String state;
    private LoanFactory loanFactory;

    public void addToLoans(final Loan loan) {
        loans.add(loan);
        loan.setLentTo(this);
        changed();
    }

    public void created() {
        state = NEW;
        junior = false;
        code = "M" +  1;
        joined = new Date();
    }
    
    public void suspend() {
        setStatus(SUSPENDED);
    }
    
    public String disableSuspend() {
        if (getStatus().equals(SUSPENDED)) {
            return "Member already suspended";
        } else if (getStatus().equals(EXPIRED)) {
            return "Member expired";
        } else {
            return null;
        }
    }
    
    public void reinstate() {
        setStatus(VALID);
    }
    
    public String disableReinstate() {
        if (!getStatus().equals(SUSPENDED)) {
            return "Member mot suspended";
        } else {
            return null;
        }
    }
    
    @MultiLine(numberOfLines=5)
    public String getAddress() {
        load(address);
        return address;
    }
    
    @Disabled
    public String getCode() {
        load(code);
        return code;
    }

    @Optional
    public String getEmail() {
        load(email);
        return email;
    }

    @Disabled
    public Date getJoined() {
        load(joined);
        return joined;
    }

    public List getLoans() {
        return loans;
    }

    public String getName() {
        load(name);
        return name;
    }

    @Optional
    public String getPhone() {
        load(phone);
        return phone;
    }

    @Disabled
    public String getStatus() {
        load(state);
        return state;
    }

    public boolean getJunior() {
        return junior;
    }
    
    public boolean isJunior() {
        load();
        return junior;
    }
    
    public boolean isCurrent() {
        return getStatus().equals(NEW) || getStatus().equals(VALID);
    }

    @DescribedAs("Take out book on a 3 week loan")
    public Loan loan(final Book book) {
        return loanFactory.createLoan(book, this);
    }

    public String disableLoan(final Book book) {
        if(getLoans().size() > 2) return "Reached borrowing limit";
        if(!isCurrent()) return "Member is not currently able to take out book";
        return null;
    }

    public String validateLoan(final Book book) {
        if(book.getOnLoan() != null) return "Book already on loan";
        if(getLoans().size() > 2) return "Reached borrowing limit";
        return null;
    }

    public void removeFromLoans(final Loan loan) {
        load();
        loans.remove(loan);
        changed();
    }

    public void setAddress(String address) {
        this.address = address;
        changed();
    }

    public void setCode(final String code) {
        this.code = code;
        changed();
    }

    public void setEmail(final String email) {
        this.email = email;
        changed();
    }

    public void setJunior(final boolean junior) {
        this.junior = junior;
        changed();
    }

    public void setJoined(Date joined) {
        this.joined = joined;
        changed();
    }
    
    public void setLoanFactory(LoanFactory loanFactory) {
        this.loanFactory = loanFactory;
    }

    public void setName(final String name) {
        this.name = name;
        changed();
    }

    public void setStatus(String state) {
        this.state = state;
        changed();
    }
    
    public void setPhone(final String phone) {
        this.phone = phone;
        changed();
    }

    public String title() {
        return getName();
    }

}
