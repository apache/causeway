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
package org.apache.causeway.core.metamodel.tabular.simple;

import javax.inject.Named;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.causeway.applib.ViewModel;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.testing._SerializationTester;
import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.execution.MemberExecutorService;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

class DataTableSerializationTest implements HasMetaModelContext {

    @BeforeEach
    final void setUp() throws Exception {
        var memberExecutorService = Mockito.mock(MemberExecutorService.class);
        MetaModelContext_forTesting.builder()
            .singleton(memberExecutorService)
            .build();
    }

    @Named("DataTableSerializationTest.Customer")
    @AllArgsConstructor
    public static class Customer implements ViewModel {

        @Property
        @Getter @Setter
        private String memento;

        @Override
        public String viewModelMemento() {
            return memento;
        }

    }

    @Test
    void roundtripOnEmptyTable() {
        var original = DataTable.forDomainType(Customer.class);
        var afterRoundtrip = _SerializationTester.roundtrip(original);

        assertNotNull(afterRoundtrip);
        assertEquals(
                "DataTableSerializationTest.Customer",
                afterRoundtrip.getElementType().getLogicalTypeName());
        assertEquals(0, afterRoundtrip.getElementCount());
        assertEquals(1, afterRoundtrip.getDataColumns().size());
        assertEquals(0, afterRoundtrip.getDataRows().size());
    }

    @Test
    void roundtripOnPopulatedTable() {
        var original = DataTable.forDomainType(Customer.class);

        original.setDataElements(Can.of(
                getObjectManager().adapt(new Customer("cus-1")),
                getObjectManager().adapt(new Customer("cus-2"))
                ));

        var afterRoundtrip = _SerializationTester.roundtrip(original);
        assertNotNull(afterRoundtrip);
        assertEquals(2, afterRoundtrip.getDataRows().size());

        var cus1 = (Customer) afterRoundtrip.getDataRows().getElseFail(0).getRowElement().getPojo();
        var cus2 = (Customer) afterRoundtrip.getDataRows().getElseFail(1).getRowElement().getPojo();

        assertEquals("cus-1", cus1.getMemento());
        assertEquals("cus-2", cus2.getMemento());

    }

}
