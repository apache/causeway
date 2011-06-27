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

import org.apache.isis.applib.annotation.Optional;




public class Book extends AbstractLibraryObject {
    public static String fieldOrder() {
        return "title, author, code, on loan";
    }

    private String author;
    private String code;
    private Loan onLoan;
    private String coverTitle;

    public void clearOnLoan() {
        onLoan.clearBook();
    }

    @Optional
    public String getAuthor() {
        load(author);
        return author;
    }

    public String getCode() {
        load(code);
        return code;
    }

    @Optional
    public Loan getOnLoan() {
        load(onLoan);
        return onLoan;
    }

    public String getCoverTitle() {
        load(coverTitle);
        return coverTitle;
    }

    public void modifyOnLoan(final Loan loan) {
        loan.clearBook();
    }

    public void setAuthor(final String author) {
        this.author = author;
        changed();
    }

    public void setCode(final String code) {
        this.code = code;
        changed();
    }

    public void setOnLoan(final Loan loan) {
        onLoan = loan;
        changed();
    }

    public void setCoverTitle(final String title) {
        this.coverTitle = title;
        changed();
    }

    public String title() {
        String title = getCoverTitle();
        String code = getCode();
        return (title == null ? "no title" : title) + (code == null ? "" : " (" + code + ")");
    }

}
