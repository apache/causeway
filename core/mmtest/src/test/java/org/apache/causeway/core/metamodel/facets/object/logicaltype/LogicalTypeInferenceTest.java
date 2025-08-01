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
package org.apache.causeway.core.metamodel.facets.object.logicaltype;

import jakarta.inject.Named;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.causeway.applib.annotation.Value;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.core.metamodel.facets.FacetFactoryTestAbstract;

public class LogicalTypeInferenceTest
extends FacetFactoryTestAbstract {

    public static class Customer {
    }

    @Test
    void installsFacet_passedThroughClassSubstitutor() {
        assertThat(LogicalType.infer(Customer.class).logicalName(),
                is(Customer.class.getCanonicalName()));
    }

    @Value
    public static class ValueExample1 {
    }

    @Test
    void installsFacet_onValues() {
        assertThat(LogicalType.infer(ValueExample1.class).logicalName(),
                is(ValueExample1.class.getCanonicalName()));
    }

    @Named("xxx.ValueExample")
    @Value
    static class ValueExample2 {
    }

    @Test
    void installsFacet_onValuesUsingLogicalTypeName() {
        assertThat(LogicalType.infer(ValueExample2.class).logicalName(),
                is("xxx.ValueExample"));
    }

}

