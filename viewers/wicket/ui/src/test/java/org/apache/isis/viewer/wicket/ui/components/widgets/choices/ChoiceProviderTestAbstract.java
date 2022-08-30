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
package org.apache.isis.viewer.wicket.ui.components.widgets.choices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.ioc._ManagedBeanAdapter;
import org.apache.isis.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.object.ManagedObject;
import org.apache.isis.core.metamodel.objectmanager.memento.ObjectMementoService;
import org.apache.isis.core.metamodel.valuesemantics.BigDecimalValueSemantics;
import org.apache.isis.core.metamodel.valuesemantics.IntValueSemantics;
import org.apache.isis.core.metamodel.valuesemantics.UUIDValueSemantics;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.core.runtimeservices.memento.ObjectMementoServiceDefault;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;

import lombok.val;

abstract class ChoiceProviderTestAbstract {

    protected MetaModelContext mmc;

    private ObjectMementoServiceDefault mementoService() {
        return mmc.getServiceInjector().injectServicesInto(new ObjectMementoServiceDefault());
    }

    protected void setUp() {
        mmc = MetaModelContext_forTesting.builder()
                .singletonProvider(_ManagedBeanAdapter.forTestingLazy(ObjectMementoService.class, this::mementoService))
                .build()
                .withValueSemantics(new BigDecimalValueSemantics())
                .withValueSemantics(new IntValueSemantics())
                .withValueSemantics(new UUIDValueSemantics())
                ;

        // verify
        {
            val mementoService = mmc.getServiceRegistry().lookupServiceElseFail(ObjectMementoService.class);
            assertEquals(ObjectMementoServiceDefault.class, mementoService.getClass());
        }
    }

    protected ScalarModel mockScalarModel(final Can<ManagedObject> choices, final boolean isRequired) {
        val mockScalarModel = mock(ScalarModel.class);
        when(mockScalarModel.getChoices()).thenReturn(choices);
        when(mockScalarModel.isRequired()).thenReturn(isRequired);
        when(mockScalarModel.hasChoices()).thenReturn(true);
        when(mockScalarModel.getCommonContext()).thenReturn(IsisAppCommonContext.of(mmc));
        return mockScalarModel;
    }


}
