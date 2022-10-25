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
package org.apache.causeway.core.runtimeservices.wrapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import org.apache.causeway.applib.services.wrapper.WrappingObject;
import org.apache.causeway.applib.services.wrapper.control.ExecutionMode;
import org.apache.causeway.applib.services.wrapper.control.SyncControl;
import org.apache.causeway.commons.collections.ImmutableEnumSet;
import org.apache.causeway.commons.internal.proxy._ProxyFactoryService;
import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting;

import lombok.RequiredArgsConstructor;
import lombok.val;

class WrapperFactoryDefaultTest {

    private static class DomainObject {
    }

    @RequiredArgsConstructor
    private static class WrappingDomainObject extends DomainObject implements WrappingObject {

        private final DomainObject wrappedObject;
        private final ImmutableEnumSet<ExecutionMode> modes;

        @Override
        public void __causeway_save() {
        }

        @Override
        public Object __causeway_wrapped() {
            return wrappedObject;
        }

        @Override
        public ImmutableEnumSet<ExecutionMode> __causeway_executionModes() {
            return modes;
        }
    }

    private WrapperFactoryDefault wrapperFactory;

    private DomainObject createProxyCalledWithDomainObject;
    private SyncControl createProxyCalledWithSyncControl;

    @BeforeEach
    public void setUp() throws Exception {

        val mmc = MetaModelContext_forTesting.buildDefault();

        wrapperFactory = new WrapperFactoryDefault() {

            @Override
            public void init() {
                this.metaModelContext = mmc;
                this.proxyFactoryService = Mockito.mock(_ProxyFactoryService.class);
                super.init();
            }

            @Override
            protected <T> T createProxy(final T domainObject, final SyncControl syncControl) {
                WrapperFactoryDefaultTest.this.createProxyCalledWithSyncControl = syncControl;
                WrapperFactoryDefaultTest.this.createProxyCalledWithDomainObject = (DomainObject) domainObject;
                return domainObject;
            }
        };

        wrapperFactory.init();

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
        assertThat(createProxyCalledWithSyncControl.getExecutionModes(), contains(ExecutionMode.SKIP_RULE_VALIDATION));
    }

}
