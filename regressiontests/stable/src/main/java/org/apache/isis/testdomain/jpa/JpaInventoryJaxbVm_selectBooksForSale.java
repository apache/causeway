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
package org.apache.isis.testdomain.jpa;

import java.util.Collection;
import java.util.List;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.testdomain.jpa.entities.JpaBook;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;

@Action
@ActionLayout(associateWith = "books", promptStyle = PromptStyle.DIALOG_MODAL)
@RequiredArgsConstructor
public class JpaInventoryJaxbVm_selectBooksForSale {

    private final JpaInventoryJaxbVm mixee;

    // typed tuple made of all the action parameters
    @Value @Accessors(fluent = true)
    public static class Parameters {
        String filter;
        List<JpaBook> booksForSale;
    }

    @MemberSupport public List<JpaBook> act(
            @Parameter final String filter,
            @Parameter final List<JpaBook> booksForSale) {
        return booksForSale;
    }

    @MemberSupport public List<JpaBook> defaultBooksForSale(final Parameters p) {
        return p.booksForSale();
    }

    @MemberSupport public Collection<JpaBook> choicesBooksForSale(final Parameters p) {
        return mixee.getBooks();
    }

}
