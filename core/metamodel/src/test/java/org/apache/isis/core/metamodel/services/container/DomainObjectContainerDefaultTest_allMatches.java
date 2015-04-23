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

package org.apache.isis.core.metamodel.services.container;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.applib.query.Query;
import org.apache.isis.core.metamodel.adapter.DomainObjectServices;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DomainObjectContainerDefaultTest_allMatches {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    private DomainObjectContainerDefault container;

    @Mock
    private DomainObjectServices mockDomainObjectServices;

    private boolean flushCalled;

    @Before
    public void setUp() throws Exception {
        container = new DomainObjectContainerDefault() {
            @Override public boolean flush() {
                flushCalled = true;
                return true;
            }

            @Override <T> List<T> submitQuery(final Query<T> query) {
                return null;
            }
        };
        container.setDomainObjectServices(mockDomainObjectServices);
        context.allowing(mockDomainObjectServices);
    }

    @Test
    public void whenAutoflush() throws Exception {
        // given
        Map map = new HashMap();
        container.init(map);
        // when
        container.allMatches(null);
        // then
        assertThat(flushCalled, is(true));
    }

    @Test
    public void whenDisableAutoflush() throws Exception {
        // given
        Map map = new HashMap() {{
            put("isis.services.container.disableAutoFlush", "true");
        }};
        container.init(map);
        // when
        container.allMatches(null);
        // then
        assertThat(flushCalled, is(false));
    }

    @Test
    public void whenDisableAutoflushSetToFalse() throws Exception {
        // given
        Map map = new HashMap() {{
            put("isis.services.container.disableAutoFlush", "false");
        }};
        container.init(map);
        // when
        container.allMatches(null);
        // then
        assertThat(flushCalled, is(true));
    }

}
