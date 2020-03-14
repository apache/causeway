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
package org.apache.isis.core.runtimeservices.wrapper;

import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.isis.applib.services.wrapper.control.ExecutionMode;
import org.apache.isis.applib.services.wrapper.WrappingObject;
import org.apache.isis.applib.services.wrapper.control.ExecutionModes;
import org.apache.isis.core.commons.collections.ImmutableEnumSet;
import org.apache.isis.core.commons.internal.plugins.codegen.ProxyFactoryService;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2;

import lombok.RequiredArgsConstructor;

public class WrapperFactoryDefaultTest {

    private static class DomainObject {
    }

    @RequiredArgsConstructor
    private static class WrappedDomainObject extends DomainObject implements WrappingObject {

        private final DomainObject wrappedObject;
        private final ImmutableEnumSet<ExecutionMode> mode;

        @Override
        public void __isis_save() {
        }

        @Override
        public Object __isis_wrapped() {
            return wrappedObject;
        }

        @Override
        public ImmutableEnumSet<ExecutionMode> __isis_executionMode() {
            return mode;
        }
    }

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    @Mock private ProxyFactoryService mockProxyFactoryService;
    private WrapperFactoryDefault wrapperFactory;

    private DomainObject createProxyCalledWithDomainObject;
    private ImmutableEnumSet<ExecutionMode> createProxyCalledWithMode;

    @Before
    public void setUp() throws Exception {
        wrapperFactory = new WrapperFactoryDefault() {

            @Override
            public void init() {
                this.proxyFactoryService = mockProxyFactoryService; 
                super.init();
            }
            
            @Override
            protected <T> T createProxy(T domainObject, ImmutableEnumSet<ExecutionMode> modes) {
                WrapperFactoryDefaultTest.this.createProxyCalledWithMode = modes;
                WrapperFactoryDefaultTest.this.createProxyCalledWithDomainObject = (DomainObject) domainObject;
                return domainObject;
            }
        };
        
        
    }

    @Test
    public void wrap_ofUnwrapped_delegates_to_createProxy() throws Exception {
        final DomainObject domainObject = new DomainObject();
        wrapperFactory.wrap(domainObject);

        assertThat(createProxyCalledWithDomainObject, is(domainObject));
        assertThat(createProxyCalledWithMode, is(ExecutionModes.EXECUTE));
    }


    @Test
    public void wrap_ofWrapped_sameMode_returnsUnchanged() throws Exception {
        // given
        final DomainObject wrappedObject = new DomainObject();
        final DomainObject domainObject = new WrappedDomainObject(wrappedObject, ExecutionModes.EXECUTE);

        // when
        final DomainObject wrappingObject = wrapperFactory.wrap(domainObject, ExecutionModes.EXECUTE);

        // then
        assertThat(wrappingObject, is(domainObject));
        assertThat(createProxyCalledWithDomainObject, is(nullValue()));
    }

    @Test
    public void wrap_ofWrapped_differentMode_delegates_to_createProxy() throws Exception {
        // given
        final DomainObject wrappedObject = new DomainObject();
        final DomainObject domainObject = new WrappedDomainObject(wrappedObject, ExecutionModes.EXECUTE);

        // when
        final DomainObject wrappingObject = wrapperFactory.wrap(domainObject, ExecutionModes.SKIP_RULES);

        // then
        assertThat(wrappingObject, is(not(domainObject)));
        assertThat(createProxyCalledWithDomainObject, is(wrappedObject));
        assertThat(createProxyCalledWithMode, is(ExecutionModes.SKIP_RULES));
    }

}