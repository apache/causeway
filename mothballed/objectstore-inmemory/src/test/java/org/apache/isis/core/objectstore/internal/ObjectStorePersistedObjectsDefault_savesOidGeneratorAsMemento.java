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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.apache.isis.core.runtime.system.persistence.IdentifierGeneratorDefault;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class ObjectStorePersistedObjectsDefault_savesOidGeneratorAsMemento {

    private ObjectStorePersistedObjects persistedObjects;

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    private IdentifierGeneratorDefault.Memento mockMemento;

    @Before
    public void setUp() throws Exception {
        persistedObjects = new ObjectStorePersistedObjects();
        mockMemento = context.mock(IdentifierGeneratorDefault.Memento.class);
    }

    @Test
    public void noOidGeneratorInitially() throws Exception {
        final IdentifierGeneratorDefault.Memento oidGeneratorMemento = persistedObjects.getOidGeneratorMemento();
        assertThat(oidGeneratorMemento, is(nullValue()));
    }

    @Test
    public void oidGeneratorStoredOnceSaved() throws Exception {
        persistedObjects.saveOidGeneratorMemento(mockMemento);
        final IdentifierGeneratorDefault.Memento oidGeneratorMemento = persistedObjects.getOidGeneratorMemento();
        assertThat(oidGeneratorMemento, is(mockMemento));
    }

}
