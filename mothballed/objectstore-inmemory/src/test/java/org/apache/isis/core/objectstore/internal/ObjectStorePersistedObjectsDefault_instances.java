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

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.isis.core.metamodel.spec.ObjectSpecId;

@RunWith(JMock.class)
public class ObjectStorePersistedObjectsDefault_instances {

    private ObjectStorePersistedObjects persistedObjects;

    private final Mockery context = new JUnit4Mockery();

    private ObjectSpecId mockSpec;

    @Before
    public void setUp() throws Exception {
        persistedObjects = new ObjectStorePersistedObjects();
        mockSpec = context.mock(ObjectSpecId.class);
    }

    @Test
    public void instancesLazilyPopulatedWhenAskForThem() throws Exception {
        neverInteractsWithSpec();

        // no instances
        final Iterable<ObjectStoreInstances> instancesBefore = persistedObjects.instances();
        assertThat(instancesBefore.iterator().hasNext(), is(false));

        ensureThereAreSomeInstances();

        // now there are
        final Iterable<ObjectStoreInstances> instancesAfter = persistedObjects.instances();
        assertThat(instancesAfter.iterator().hasNext(), is(true));
    }

    @Test
    public void clearZapsTheInstances() throws Exception {
        neverInteractsWithSpec();

        ensureThereAreSomeInstances();
        final Iterable<ObjectStoreInstances> instancesAfter = persistedObjects.instances();
        assertThat(instancesAfter.iterator().hasNext(), is(true));

        persistedObjects.clear();

        // now there are no more instances
        final Iterable<ObjectStoreInstances> instancesBefore = persistedObjects.instances();
        assertThat(instancesBefore.iterator().hasNext(), is(false));
    }

    private void ensureThereAreSomeInstances() {
        persistedObjects.instancesFor(mockSpec);
    }

    private void neverInteractsWithSpec() {
        context.checking(new Expectations() {
            {
                never(mockSpec);
            }
        });
    }

}
