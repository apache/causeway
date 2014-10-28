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

package org.apache.isis.core.objectstore.internal;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;
import java.util.Set;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

/**
 * Tested in style of <i>Working Effectively with Legacy Code</i> (Feathers) and
 * <i>Growing Object-Oriented Software</i> (Freeman &amp; Pryce).
 */
@RunWith(JMock.class)
public class ObjectStoreInstances_init {

    private ObjectStoreInstances instances;

    private final Mockery context = new JUnit4Mockery();

    private ObjectSpecId mockSpecId;

    @Before
    public void setUp() throws Exception {
        mockSpecId = context.mock(ObjectSpecId.class);
        instances = new ObjectStoreInstances(mockSpecId);
    }

    @Test
    public void initiallyEmpty() throws Exception {
        final Map<Oid, Object> objectInstances = instances.getObjectInstances();
        assertThat(objectInstances.size(), is(0));

        final Set<Oid> oids = instances.getOids();
        assertThat(oids.size(), is(0));

        assertThat(instances.hasInstances(), is(false));
    }

}
