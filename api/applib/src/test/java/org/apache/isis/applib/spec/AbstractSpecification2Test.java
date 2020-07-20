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

package org.apache.isis.applib.spec;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.isis.applib.services.i18n.TranslatableString;
import org.apache.isis.applib.spec.AbstractSpecification2.Nullability;
import org.apache.isis.applib.spec.AbstractSpecification2.TypeChecking;

public class AbstractSpecification2Test {

    private AbstractSpecification2<String> specAbstractSomeDomainObject;

    @Test
    public void shouldSatisfyByDefaultForNull() {
        specAbstractSomeDomainObject = new AbstractSpecification2<String>() {
            @Override
            public TranslatableString satisfiesTranslatableSafely(final String obj) {
                return null;
            }
        };
        assertThat(specAbstractSomeDomainObject.satisfiesTranslatable(null), is(nullValue()));
    }

    @Test
    public void shouldNotSatisfyForNullIfConfiguredAsSuch() {
        specAbstractSomeDomainObject = new AbstractSpecification2<String>(Nullability.ENSURE_NOT_NULL, TypeChecking.IGNORE_INCORRECT_TYPE) {
            @Override
            public TranslatableString satisfiesTranslatableSafely(final String obj) {
                return null;
            }
        };
        assertThat(specAbstractSomeDomainObject.satisfiesTranslatable(null), is(not(nullValue())));
    }

    @Test
    public void shouldSatisfyByDefaultForIncorrectType() {
        specAbstractSomeDomainObject = new AbstractSpecification2<String>() {
            @Override
            public TranslatableString satisfiesTranslatableSafely(final String obj) {
                return null;
            }
        };
        assertThat(specAbstractSomeDomainObject.satisfiesTranslatable(Integer.valueOf(1)), is(nullValue()));
    }

    @Test
    public void shouldNotSatisfyForIncorrectTypeIfConfiguredAsSuch() {
        specAbstractSomeDomainObject = new AbstractSpecification2<String>(Nullability.IGNORE_IF_NULL, TypeChecking.ENSURE_CORRECT_TYPE) {
            @Override
            public TranslatableString satisfiesTranslatableSafely(final String obj) {
                return null;
            }
        };
        assertThat(specAbstractSomeDomainObject.satisfiesTranslatable(Integer.valueOf(1)), is(not(nullValue())));
    }

    @Test
    public void shouldSatisfyForNonNullCorrectTypeIfConfiguredAsSuch() {
        specAbstractSomeDomainObject = new AbstractSpecification2<String>(Nullability.ENSURE_NOT_NULL, TypeChecking.ENSURE_CORRECT_TYPE) {
            @Override
            public TranslatableString satisfiesTranslatableSafely(final String obj) {
                return null;
            }
        };
        assertThat(specAbstractSomeDomainObject.satisfiesTranslatable(new String()), is(nullValue()));
    }

}
