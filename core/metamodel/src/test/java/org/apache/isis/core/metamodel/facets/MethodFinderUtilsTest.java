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
package org.apache.isis.core.metamodel.facets;

import java.lang.reflect.Method;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.isis.applib.annotation.Encapsulation.EncapsulationPolicy;
import org.apache.isis.core.metamodel.methods.MethodByClassMap;
import org.apache.isis.core.metamodel.methods.MethodFinderUtils;

import lombok.val;

public class MethodFinderUtilsTest {

    public static class NoPostConstruct {
        public void thisDoesNotHaveAnyAnnotation(){}
    }
    public static class WithPostConstruct {
        @PostConstruct // @PostConstruct is allowed to appear on non-public methods
        private void thisDoesHaveAnnotation(){}
    }

    @Test
    public void whenExists() throws Exception {

        val cache = new MethodByClassMap();
        final Method method = MethodFinderUtils
                .findAnnotatedMethod(
                        EncapsulationPolicy.ENCAPSULATED_MEMBERS_SUPPORTED,
                        new WithPostConstruct(), PostConstruct.class, cache );

        assertThat(method, is(not(nullValue())));
        final Optional<Method> actual = cache.get(WithPostConstruct.class);
        assertThat(actual, is(not(nullValue())));
        assertThat(actual.isPresent(), is(true));
        assertThat(actual.orElse(null), is(method));
    }

    @Test
    public void whenDoesNotExist() throws Exception {

        val cache = new MethodByClassMap();
        final Method method = MethodFinderUtils
                .findAnnotatedMethod(
                        EncapsulationPolicy.ENCAPSULATED_MEMBERS_SUPPORTED,
                        new NoPostConstruct(), PostConstruct.class, cache);

        assertThat(method, is(nullValue()));
        final Optional<Method> actual = cache.get(NoPostConstruct.class);
        assertThat(actual, is(not(nullValue())));
        assertThat(actual.isPresent(), is(false));
        assertThat(actual.orElse(null), is(nullValue()));
    }

}