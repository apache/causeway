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
package org.apache.causeway.testdomain.jdo;

import java.util.Collection;
import java.util.List;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.annotation.PromptStyle;
import org.apache.causeway.testdomain.jdo.entities.JdoBook;

import lombok.RequiredArgsConstructor;

@Action
@ActionLayout(associateWith = "books", promptStyle = PromptStyle.DIALOG_MODAL)
@RequiredArgsConstructor
public class JdoInventoryJaxbVm_selectBooksForSale {

    private final JdoInventoryJaxbVm mixee;

    // typed tuple made of all the action parameters
    public record Parameters(
            String filter,
            List<JdoBook> booksForSale) {
    }

    @MemberSupport public List<JdoBook> act(
            @Parameter final String filter,
            @Parameter final List<JdoBook> booksForSale) {
        return booksForSale;
    }

    @MemberSupport public List<JdoBook> defaultBooksForSale(final Parameters p) {
        return p.booksForSale();
    }

    @MemberSupport public Collection<JdoBook> choicesBooksForSale(final Parameters p) {
        return mixee.getBooks();
    }

}
