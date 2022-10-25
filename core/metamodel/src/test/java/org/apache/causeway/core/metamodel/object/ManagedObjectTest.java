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
package org.apache.causeway.core.metamodel.object;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.domain.DomainObjectList;
import org.apache.causeway.commons.internal._Constants;
import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.causeway.core.metamodel.object.ManagedObject.Specialization;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.core.metamodel.valuesemantics.IntValueSemantics;

import lombok.SneakyThrows;
import lombok.val;

class ManagedObjectTest {

    private MetaModelContext_forTesting mmc;
    private SpecificationLoader specLoader;

    @BeforeEach
    public void setUp() throws Exception {
        mmc = MetaModelContext_forTesting.builder()
                        .valueSemantic(new IntValueSemantics())
                        .build();
        specLoader = mmc.getSpecificationLoader();
    }

    @ParameterizedTest
    @ValueSource(classes = {void.class, Void.class})
    void voidShouldMapToEmptyValue(final Class<?> cls) {
        val spec = specLoader.specForTypeElseFail(cls);
        assertTrue(spec.isVoid(), ()->"isVoid()");
        assertTrue(spec.isValue(), ()->"isValue()");
        assertFalse(spec.isAbstract(), ()->"isAbstract()");
        assertFalse(spec.isInjectable(), ()->"isInjectable()");
        assertFalse(spec.isEntityOrViewModel(), ()->"isEntityOrViewModel()");

        val emptySpez = Specialization.inferFrom(spec, null);
        assertEquals(Specialization.EMPTY, emptySpez);

        val emptyObject = ManagedObject.empty(spec);
        assertNotNull(emptyObject);
    }

    @ParameterizedTest
    @ValueSource(classes = {int.class, Integer.class})
    void intShouldMapToValue(final Class<?> cls) {
        val spec = specLoader.specForTypeElseFail(cls);
        assertFalse(spec.isVoid(), ()->"isVoid()");
        assertTrue(spec.isValue(), ()->"isValue()");
        assertFalse(spec.isAbstract(), ()->"isAbstract()");
        assertFalse(spec.isInjectable(), ()->"isInjectable()");
        assertFalse(spec.isEntityOrViewModel(), ()->"isEntityOrViewModel()");

        val emptySpez = Specialization.inferFrom(spec, null);
        assertEquals(Specialization.EMPTY, emptySpez);

        val emptyObject = ManagedObject.empty(spec);
        assertNotNull(emptyObject);

        val presentObject = ManagedObject.adaptSingular(specLoader, 3);
        assertEquals(Specialization.VALUE, presentObject.getSpecialization());

        presentObject.assertCompliance(6);

        assertThrows(AssertionError.class, ()->{
            presentObject.assertCompliance("incompatible");
        });
    }

    @ParameterizedTest
    @ValueSource(classes = {DomainObjectList.class})
    @SneakyThrows
    void someTypesShouldMapToViewmodel(final Class<?> cls) {
        val spec = specLoader.specForTypeElseFail(cls);
        assertFalse(spec.isVoid(), ()->"isVoid()");
        assertTrue(spec.isViewModel(), ()->"isViewModel()");
        assertFalse(spec.isAbstract(), ()->"isAbstract()");
        assertFalse(spec.isInjectable(), ()->"isInjectable()");

        val emptySpez = Specialization.inferFrom(spec, null);
        assertEquals(Specialization.EMPTY, emptySpez);

        val emptyObject = ManagedObject.empty(spec);
        assertNotNull(emptyObject);

        val constructor = cls.getConstructor(_Constants.emptyClasses);
        val pojo = constructor.newInstance(_Constants.emptyObjects);

        val presentObject = ManagedObject.adaptSingular(specLoader, pojo);
        assertEquals(Specialization.VIEWMODEL, presentObject.getSpecialization());

        presentObject.assertCompliance(pojo);

        //TODO
//        assertThrows(AssertionError.class, ()->{
//            presentObject.assertCompliance("incompatible");
//        });
    }

}
