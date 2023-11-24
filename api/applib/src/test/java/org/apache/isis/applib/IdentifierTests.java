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
package org.apache.isis.applib;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.commons.collections.Can;

import lombok.val;

class IdentifierTests {

    private Identifier identifier;

    @Test
    void canInstantiateClassIdentifier() {
        identifier = Identifier.classIdentifier(LogicalType.fqcn(SomeDomainClass.class));
        assertThat(identifier, is(not(nullValue())));
    }

    @Test
    void classIdentifierClassNameIsSet() {
        val domainClass = SomeDomainClass.class;
        final String domainClassFullyQualifiedName = domainClass.getCanonicalName();
        identifier = Identifier.classIdentifier(LogicalType.fqcn(domainClass));
        assertThat(identifier.getClassName(), is(domainClassFullyQualifiedName));
    }

    @Test
    void memberParameterNames() {
        val domainClass = SomeDomainClass.class;
        identifier = Identifier.actionIdentifier(LogicalType.fqcn(domainClass), "placeOrder", int.class, String.class);
        assertThat(identifier.getMemberParameterClassNames(), is(Can.of("int", "java.lang.String")));
    }

    @Test
    void paramsIdentityString() {
        val domainClass = SomeDomainClass.class;
        identifier = Identifier.actionIdentifier(LogicalType.fqcn(domainClass), "placeOrder", int.class, String.class, BigDecimal.class);
        assertThat(
                identifier.getFullIdentityString(),
                is("org.apache.isis.applib.SomeDomainClass#placeOrder(int,java.lang.String,java.math.BigDecimal)"));
    }


}
