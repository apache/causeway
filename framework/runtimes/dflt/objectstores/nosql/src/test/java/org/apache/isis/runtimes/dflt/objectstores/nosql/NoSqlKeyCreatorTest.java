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

package org.apache.isis.runtimes.dflt.objectstores.nosql;

import static org.junit.Assert.assertEquals;

import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.metamodel.adapter.oid.RootOidDefault;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.progmodel.app.IsisMetaModel;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2.Mode;
import org.apache.isis.progmodels.dflt.ProgrammingModelFacetsJava5;
import org.apache.isis.runtimes.dflt.objectstores.nosql.keys.KeyCreatorDefault;
import org.apache.isis.tck.dom.eg.ExamplePojoRepository;
import org.apache.isis.tck.dom.eg.ExamplePojoWithReferences;

public class NoSqlKeyCreatorTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    private final String id = "3";
    private final String reference = ExamplePojoWithReferences.class.getName() + "@" + id;
    private final RootOidDefault oid3 = RootOidDefault.create("ERP", id);
    
    private ObjectSpecification specification;
    
    private KeyCreatorDefault keyCreatorDefault;

    private IsisMetaModel isisMetaModel;

    @Mock
    private RuntimeContext mockRuntimeContext;

    @Before
    public void setup() {
        isisMetaModel = new IsisMetaModel(mockRuntimeContext, new ProgrammingModelFacetsJava5(), new ExamplePojoRepository());
        specification = isisMetaModel.getSpecificationLoader().loadSpecification(ExamplePojoWithReferences.class);

        keyCreatorDefault = new KeyCreatorDefault();
    }

    @Test
    public void oid() throws Exception {
        final RootOidDefault oid = (RootOidDefault) keyCreatorDefault.oidFromReference(reference);
        assertEquals(oid3.getIdentifier(), oid.getIdentifier());
        assertEquals(oid3.getObjectType(), oid.getObjectType());
    }

    @Test
    public void specification() throws Exception {
        final ObjectSpecification spec = keyCreatorDefault.specificationFromReference(reference);
        assertEquals(specification, spec);
    }
}
