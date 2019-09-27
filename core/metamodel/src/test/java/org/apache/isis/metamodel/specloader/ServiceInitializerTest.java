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
package org.apache.isis.metamodel.specloader;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.config.IsisConfigurationLegacy;
import org.apache.isis.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.unittestsupport.jmocking.JUnitRuleMockery2.Mode;

import static org.apache.isis.commons.internal.base._NullSafe.stream;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ServiceInitializerTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_ONLY);

    private Map<String,String> props;

    @Mock
    private IsisConfigurationLegacy configuration;
    private ServiceInitializer serviceInitializer;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        props = _Maps.newHashMap();
        context.checking(new Expectations() {
            {
                allowing(configuration).copyToMap();
                will(returnValue(props));
            }
        });
    }

    @After
    public void tearDown() throws Exception {
        serviceInitializer = null;
    }


    private static List<Object> listOf(Object... elements) {
        return stream(elements)
                .collect(Collectors.toList());
    }

    public static class DomainServiceWithPostConstruct {
        boolean called;
        @PostConstruct
        public void x() {
            this.called = true;
        }
    }

    public static class DomainServiceWithPostConstructWithProperties {
        boolean called;
        Map<String,String> properties;
        @PostConstruct
        public void x(Map<String,String> properties) {
            this.properties = properties;
            this.called = true;
        }
    }

    @Test
    public void postConstruct() {
        final DomainServiceWithPostConstruct d1 = new DomainServiceWithPostConstruct();
        final DomainServiceWithPostConstructWithProperties d2 = new DomainServiceWithPostConstructWithProperties();

        serviceInitializer = new ServiceInitializer(configuration, listOf(d1, d2));
        serviceInitializer.validate();

        // when
        serviceInitializer.postConstruct();

        // then
        assertThat(d1.called, is(true));
        assertThat(d2.called, is(true));
        assertThat(d2.properties, is(props));
    }


    public static class DomainServiceWithPreDestroy {
        boolean called;
        @PreDestroy
        public void x() {
            this.called = true;
        }
    }

    @Test
    public void preDestroy() {
        final DomainServiceWithPreDestroy d1 = new DomainServiceWithPreDestroy();
        final DomainServiceWithPreDestroy d2 = new DomainServiceWithPreDestroy();

        serviceInitializer = new ServiceInitializer(configuration, listOf(d1, d2));
        serviceInitializer.validate();

        // when
        serviceInitializer.preDestroy();

        // then
        assertThat(d1.called, is(true));
        assertThat(d2.called, is(true));
    }

    public static class DomainServiceWithMultiplePostConstruct {
        boolean called;
        @PostConstruct
        public void x() {
            this.called = true;
        }
        @PostConstruct
        public void y() {
            this.called = true;
        }
    }

    @Test
    public void init_when_postConstructMultiple() {
        final DomainServiceWithMultiplePostConstruct d1 = new DomainServiceWithMultiplePostConstruct();
        expectedException.expectMessage(
                containsString(
                        "Found more than one @PostConstruct method; service is: org.apache.isis.metamodel.specloader.ServiceInitializerTest$DomainServiceWithMultiplePostConstruct, found"));
        serviceInitializer = new ServiceInitializer(configuration, listOf(d1));

        // when
        serviceInitializer.validate();
    }

    public static class DomainServiceWithMultiplePreDestroy {
        boolean called;
        @PreDestroy
        public void x() {
            this.called = true;
        }
        @PreDestroy
        public void y() {
            this.called = true;
        }
    }

    @Test
    public void init_when_preDestroyMultiple() {
        final DomainServiceWithMultiplePreDestroy d1 = new DomainServiceWithMultiplePreDestroy();
        expectedException.expectMessage(
                containsString(
                        "Found more than one @PreDestroy method; service is: org.apache.isis.metamodel.specloader.ServiceInitializerTest$DomainServiceWithMultiplePreDestroy, found"));
        serviceInitializer = new ServiceInitializer(configuration, listOf(d1));

        // when
        serviceInitializer.validate();
    }

    public static class DomainServiceWithPostConstructOneArgWrongType {
        boolean called;
        @PostConstruct
        public void y(Object o) {
            this.called = true;
        }
    }

    @Test
    public void init_when_postConstructWrongType() {
        final DomainServiceWithPostConstructOneArgWrongType d1 = new DomainServiceWithPostConstructOneArgWrongType();
        expectedException.expectMessage(
                "@PostConstruct method must be no-arg or 1-arg accepting java.util.Map; method is: org.apache.isis.metamodel.specloader.ServiceInitializerTest$DomainServiceWithPostConstructOneArgWrongType#y");
        serviceInitializer = new ServiceInitializer(configuration, listOf(d1));

        // when
        serviceInitializer.validate();
    }

    public static class DomainServiceWithPostConstructTwoArgs {
        boolean called;
        @PostConstruct
        public void y(Map<String,String> p, Object o) {
            this.called = true;
        }
    }

    @Test
    public void init_when_postConstructWrongArgs() {
        final DomainServiceWithPostConstructTwoArgs d1 = new DomainServiceWithPostConstructTwoArgs();
        expectedException.expectMessage(
                "@PostConstruct method must be no-arg or 1-arg accepting java.util.Map; method is: org.apache.isis.metamodel.specloader.ServiceInitializerTest$DomainServiceWithPostConstructTwoArgs#y");
        serviceInitializer = new ServiceInitializer(configuration, listOf(d1));

        // when
        serviceInitializer.validate();
    }

    public static class DomainServiceWithPreDestroyOneArgs {
        boolean called;
        @PreDestroy
        public void y(Object o) {
            this.called = true;
        }
    }


    @Test
    public void init_when_preDestroyWrongArgs() {
        final DomainServiceWithPreDestroyOneArgs d1 = new DomainServiceWithPreDestroyOneArgs();
        expectedException.expectMessage(
                "@PreDestroy method must be no-arg; method is: org.apache.isis.metamodel.specloader.ServiceInitializerTest$DomainServiceWithPreDestroyOneArgs#y");
        serviceInitializer = new ServiceInitializer(configuration, listOf(d1));

        // when
        serviceInitializer.validate();
    }


}
