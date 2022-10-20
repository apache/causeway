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
package org.apache.causeway.core.metamodel.specloader.specimpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;

import lombok.val;

class ObjectActionParameterAbstractTest_getId_and_getName {

    @DomainObject(nature = Nature.VIEW_MODEL)
    private static class Customer {
        @Action
        public void aMethod(final Object someParameterName, final Object arg1) {}
    }

    private ObjectAction action;

    @BeforeEach
    public void setUp() {
        val mmc = MetaModelContext_forTesting.buildDefault();
        val spec = mmc.getSpecificationLoader().loadSpecification(Customer.class);
        action = spec.getAction("aMethod").orElseThrow();
    }

    @Test
    public void shouldProperlyDetectParamIdAndName() {
        val param0 = action.getParameters().getElseFail(0);
        assertThat(param0.getId(), is("someParameterName"));
        assertThat(param0.getStaticFriendlyName().get(), is("Some Parameter Name"));
    }

}
