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
package org.apache.causeway.viewer.wicket.ui.test.components.widgets.choices;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.valuesemantics.BigDecimalValueSemantics;
import org.apache.causeway.core.metamodel.valuesemantics.IntValueSemantics;
import org.apache.causeway.core.metamodel.valuesemantics.UUIDValueSemantics;
import org.apache.causeway.viewer.wicket.model.models.ScalarModel;

import lombok.val;

abstract class ChoiceProviderTestAbstract {

    protected MetaModelContext mmc;

    protected void setUp() {
        mmc = MetaModelContext_forTesting.builder()
                .build()
                .withValueSemantics(new BigDecimalValueSemantics())
                .withValueSemantics(new IntValueSemantics())
                .withValueSemantics(new UUIDValueSemantics())
                ;
    }

    protected ScalarModel mockScalarModel(final Can<ManagedObject> choices, final boolean isRequired) {
        val mockScalarModel = mock(ScalarModel.class);
        when(mockScalarModel.getChoices()).thenReturn(choices);
        when(mockScalarModel.isRequired()).thenReturn(isRequired);
        when(mockScalarModel.hasChoices()).thenReturn(true);
        when(mockScalarModel.getMetaModelContext()).thenReturn(mmc);
        return mockScalarModel;
    }

}
