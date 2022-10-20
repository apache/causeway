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
package org.apache.causeway.applib.spec;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.causeway.applib.spec.AbstractSpecification.Nullability;
import org.apache.causeway.applib.spec.AbstractSpecification.TypeChecking;

class AbstractSpecificationTest {

    private static class SomeDomainObject {
    }

    private static class SomeOtherDomainObject {
    }

    private AbstractSpecification<SomeDomainObject> specAbstractSomeDomainObject;

    @Test
    public void shouldSatisfyByDefaultForNull() {
        specAbstractSomeDomainObject = new AbstractSpecification<SomeDomainObject>() {
            @Override
            public String satisfiesSafely(final SomeDomainObject obj) {
                return null;
            }
        };
        assertThat(specAbstractSomeDomainObject.satisfies(null), is(nullValue()));
    }

    @Test
    public void shouldNotSatisfyForNullIfConfiguredAsSuch() {
        specAbstractSomeDomainObject = new AbstractSpecification<SomeDomainObject>(Nullability.ENSURE_NOT_NULL, TypeChecking.IGNORE_INCORRECT_TYPE) {
            @Override
            public String satisfiesSafely(final SomeDomainObject obj) {
                return null;
            }
        };
        assertThat(specAbstractSomeDomainObject.satisfies(null), is(not(nullValue())));
    }

    @Test
    public void shouldSatisfyByDefaultForIncorrectType() {
        specAbstractSomeDomainObject = new AbstractSpecification<SomeDomainObject>() {
            @Override
            public String satisfiesSafely(final SomeDomainObject obj) {
                return null;
            }
        };
        assertThat(specAbstractSomeDomainObject.satisfies(new SomeOtherDomainObject()), is(nullValue()));
    }

    @Test
    public void shouldNotSatisfyForIncorrectTypeIfConfiguredAsSuch() {
        specAbstractSomeDomainObject = new AbstractSpecification<SomeDomainObject>(Nullability.IGNORE_IF_NULL, TypeChecking.ENSURE_CORRECT_TYPE) {
            @Override
            public String satisfiesSafely(final SomeDomainObject obj) {
                return null;
            }
        };
        assertThat(specAbstractSomeDomainObject.satisfies(new SomeOtherDomainObject()), is(not(nullValue())));
    }

    @Test
    public void shouldSatisfyForNonNullCorrectTypeIfConfiguredAsSuch() {
        specAbstractSomeDomainObject = new AbstractSpecification<SomeDomainObject>(Nullability.ENSURE_NOT_NULL, TypeChecking.ENSURE_CORRECT_TYPE) {
            @Override
            public String satisfiesSafely(final SomeDomainObject obj) {
                return null;
            }
        };
        assertThat(specAbstractSomeDomainObject.satisfies(new SomeDomainObject()), is(nullValue()));
    }

}
