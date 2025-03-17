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

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;

import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.testdomain.fixtures.BookView;
import org.apache.causeway.testdomain.jdo.entities.JdoBook;

import lombok.Getter;
import org.jspecify.annotations.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

@XmlRootElement(name = "bookView")
@XmlType(
        propOrder = {
                "book"}
)
@XmlAccessorType(XmlAccessType.FIELD)
@Named("testdomain.jdo.JdoBookView")
public class JdoBookView implements BookView<JdoBook> {

    public static JdoBookView createForBook(final @NonNull FactoryService factory, final @NonNull JdoBook book) {
        var view = factory.viewModel(new JdoBookView());
        view.setBook(book);
        return view;
    }

    @XmlTransient
    @Getter(onMethod_={@Override}) @Accessors(fluent=true)
    @Inject private SpecificationLoader specLoader;

    @Getter(onMethod_={@Override}) @Setter
    private JdoBook book;

}
