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

package org.apache.isis.core.bytecode.cglib;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.runtime.persistence.objectfactory.ObjectChanger;
import org.apache.isis.core.runtime.persistence.objectfactory.ObjectResolver;

@RunWith(JMock.class)
public class ClassEnhancerTest {

    private final Mockery mockery = new JUnit4Mockery();

    private ObjectResolveAndObjectChangedEnhancer classEnhancer;
    private ObjectResolver mockObjectResolver;
    private ObjectChanger mockObjectChanger;
    private SpecificationLoaderSpi mockSpecificationLoader;

    private SomeDomainObject sdo;

    @Before
    public void setUp() {
        mockObjectResolver = mockery.mock(ObjectResolver.class);
        mockObjectChanger = mockery.mock(ObjectChanger.class);
        mockSpecificationLoader = mockery.mock(SpecificationLoaderSpi.class);

        classEnhancer = new ObjectResolveAndObjectChangedEnhancer(mockObjectResolver, mockObjectChanger, mockSpecificationLoader);

        sdo = classEnhancer.newInstance(SomeDomainObject.class);
    }

    @After
    public void tearDown() {
        classEnhancer = null;
    }

    @Test
    public void canCreateNewInstance() throws Exception {
        assertThat(sdo, is(not(nullValue())));
    }

    @Ignore("TO COMPLETE")
    @Test
    public void passesThrough() throws Exception {
        mockery.checking(new Expectations() {
            {
                ignoring(mockObjectResolver);
            }
        });
        sdo.setName("Fred");
        assertThat(sdo.getName(), equalTo("Fred"));
    }

    @Ignore("TO COMPLETE")
    @Test
    public void callsResolveOnGetter() throws Exception {

        mockery.checking(new Expectations() {
            {
                one(mockObjectResolver).resolve(sdo, "name");
            }
        });

        sdo.getName();
    }

    @Ignore("TO COMPLETE")
    @Test
    public void callsResolveOnSetterAndThenObjectChanged() throws Exception {

        mockery.checking(new Expectations() {
            {
                final Sequence sequence = mockery.sequence("set");

                one(mockObjectResolver).resolve(sdo, "name");
                inSequence(sequence);

                one(mockObjectChanger).objectChanged(sdo);
                inSequence(sequence);

            }
        });

        sdo.setName("Joe");
    }

}
