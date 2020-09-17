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

import org.assertj.core.api.Assertions;
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
import org.apache.isis.applib.services.wrapper.control.SyncControl;
import org.apache.isis.commons.collections.ImmutableEnumSet;
import org.apache.isis.commons.internal.plugins.codegen.ProxyFactoryService;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2;

import lombok.RequiredArgsConstructor;

public class WrapperFactoryDefaultTest {

    private static class DomainObject {
    }

    @RequiredArgsConstructor
    private static class WrappingDomainObject extends DomainObject implements WrappingObject {

        private final DomainObject wrappedObject;
        private final ImmutableEnumSet<ExecutionMode> modes;

        @Override
        public void __isis_save() {
        }

        @Override
        public Object __isis_wrapped() {
            return wrappedObject;
        }

        @Override
        public ImmutableEnumSet<ExecutionMode> __isis_executionModes() {
            return modes;
        }
    }

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    @Mock private ProxyFactoryService mockProxyFactoryService;
    private WrapperFactoryDefault wrapperFactory;

    private DomainObject createProxyCalledWithDomainObject;
    private SyncControl createProxyCalledWithSyncControl;

    @Before
    public void setUp() throws Exception {
        wrapperFactory = new WrapperFactoryDefault() {

            @Override
            public void init() {
                this.proxyFactoryService = mockProxyFactoryService; 
                super.init();
            }
            
            @Override
            protected <T> T createProxy(T domainObject, SyncControl syncControl) {
                WrapperFactoryDefaultTest.this.createProxyCalledWithSyncControl = syncControl;
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
        assertThat(createProxyCalledWithSyncControl, is(not(nullValue())));
    }


    @Test
    public void wrap_ofWrapped_sameMode_returnsUnchanged() throws Exception {
        // given
        final DomainObject wrappedObject = new DomainObject();
        final DomainObject domainObject = new WrappingDomainObject(wrappedObject, ImmutableEnumSet.noneOf(ExecutionMode.class));

        // when
        final DomainObject wrappingObject = wrapperFactory.wrap(domainObject);

        // then
        assertThat(wrappingObject, is(domainObject));
        assertThat(createProxyCalledWithDomainObject, is(nullValue()));
    }

    @Test
    public void wrap_ofWrapped_differentMode_delegates_to_createProxy() throws Exception {
        // given
        final DomainObject wrappedObject = new DomainObject();
        final DomainObject domainObject = new WrappingDomainObject(wrappedObject, ImmutableEnumSet.noneOf(ExecutionMode.class));

        // when
        final DomainObject wrappingObject = wrapperFactory.wrap(domainObject, SyncControl.control().withSkipRules());

        // then
        assertThat(wrappingObject, is(not(domainObject)));
        assertThat(createProxyCalledWithDomainObject, is(wrappedObject));
        Assertions.assertThat(createProxyCalledWithSyncControl.getExecutionModes()).contains(ExecutionMode.SKIP_RULE_VALIDATION);
    }

}