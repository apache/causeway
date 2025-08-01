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

import org.apache.causeway.applib.services.wrapper.control.SyncControl;
import org.apache.causeway.commons.internal.proxy.ProxyFactoryService;
import org.apache.causeway.core.metamodel.execution.MemberExecutorService;
import org.apache.causeway.core.mmtestsupport.MetaModelContext_forTesting;
import org.apache.causeway.core.runtime.wrap.WrappingObject;

import lombok.RequiredArgsConstructor;

class WrapperFactoryDefaultTest {

    private static class DomainObject {
    }

    @RequiredArgsConstructor
    private static class WrappingDomainObject extends DomainObject implements WrappingObject {
        private final DomainObject wrappedObject;

        @Override
        public void __causeway_save() {
        }

        @Override
        public WrappingObject.Origin __causeway_origin() {
            return new WrappingObject.Origin(wrappedObject, SyncControl.defaults());
        }
    }

    private WrapperFactoryDefault wrapperFactory;

    private DomainObject createProxyCalledWithDomainObject;
    private SyncControl createProxyCalledWithSyncControl;

    @BeforeEach
    public void setUp() throws Exception {

        var mmc = MetaModelContext_forTesting.builder()
                .memberExecutor(Mockito.mock(MemberExecutorService.class))
                .build();

        wrapperFactory = new WrapperFactoryDefault() {

            @Override
            public void init() {
                this.metaModelContext = mmc;
                this.proxyFactoryService = Mockito.mock(ProxyFactoryService.class);
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
        final DomainObject domainObject = new WrappingDomainObject(wrappedObject);

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
        final DomainObject domainObject = new WrappingDomainObject(wrappedObject);

        // when
        final DomainObject wrappingObject = wrapperFactory.wrap(domainObject, SyncControl.defaults().withSkipRules());

        // then
        assertThat(wrappingObject, is(not(domainObject)));
        assertThat(createProxyCalledWithDomainObject, is(wrappedObject));
        assertThat(createProxyCalledWithSyncControl.isSkipRules(), is(true));
    }

}
