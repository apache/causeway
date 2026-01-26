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

import jakarta.inject.Named;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.causeway.applib.ViewModel;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.testing._SerializationTester;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.execution.MemberExecutorService;
import org.apache.causeway.core.mmtestsupport.MetaModelContext_forTesting;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

class DataTableSerializationTest implements HasMetaModelContext {

    @BeforeEach
    final void setUp() throws Exception {
        var memberExecutorService = Mockito.mock(MemberExecutorService.class);
        MetaModelContext_forTesting.builder()
            .singleton(memberExecutorService)
            .build();
    }

    @Named("DataTableSerializationTest.CustomerClass")
    @AllArgsConstructor
    public static class CustomerClass implements ViewModel {

        @Property @Getter @Setter private String memento;

        @Override public String viewModelMemento() { return memento; }
    }

    @Named("DataTableSerializationTest.CustomerRecord")
    public record CustomerRecord(@Property String memento) implements ViewModel {

        @Override public String viewModelMemento() { return memento; }
    }

    @ParameterizedTest
    @ValueSource(classes = {CustomerClass.class, CustomerRecord.class})
    void roundtripOnEmptyTable(final Class<? extends ViewModel> viewmodelClass) {
        var original = DataTable.forDomainType(viewmodelClass);
        var afterRoundtrip = _SerializationTester.roundtrip(original);

        assertNotNull(afterRoundtrip);
        assertEquals(
                "DataTableSerializationTest." + viewmodelClass.getSimpleName(),
                afterRoundtrip.elementType().logicalTypeName());
        assertEquals(0, afterRoundtrip.getElementCount());
        assertEquals(1, afterRoundtrip.dataColumns().size());
        assertEquals(0, afterRoundtrip.dataRows().size());
    }

    @ParameterizedTest
    @ValueSource(classes = {CustomerClass.class, CustomerRecord.class})
    void roundtripOnPopulatedTable(final Class<? extends ViewModel> viewmodelClass) {
        var original = DataTable.forDomainType(viewmodelClass)
            .withDataElementPojos(Can.of("cus-1", "cus-2")
                .map(name->newInstance(viewmodelClass, name))
                .map(getObjectManager()::adapt));

        var afterRoundtrip = _SerializationTester.roundtrip(original);
        assertNotNull(afterRoundtrip);
        assertEquals(2, afterRoundtrip.dataRows().size());

        var cus1 = (ViewModel) afterRoundtrip.dataRows().getElseFail(0).rowElement().getPojo();
        var cus2 = (ViewModel) afterRoundtrip.dataRows().getElseFail(1).rowElement().getPojo();

        assertEquals("cus-1", cus1.viewModelMemento());
        assertEquals("cus-2", cus2.viewModelMemento());
    }

    @SneakyThrows
    private static ViewModel newInstance(final Class<? extends ViewModel> viewmodelClass, final String memento) {
        return viewmodelClass.getConstructor(new Class<?>[]{String.class}).newInstance(memento);
    }

}
