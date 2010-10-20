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


package org.apache.isis.progmodel.java5.reflect;

import java.util.Enumeration;

import junit.framework.TestSuite;

import org.apache.isis.noa.adapter.ObjectAdapter;
import org.apache.isis.noa.adapter.ResolveState;
import org.apache.isis.noa.facets.Facet;
import org.apache.isis.metamodel.facets.When;
import org.apache.isis.metamodel.facets.actions.debug.DebugFacet;
import org.apache.isis.metamodel.facets.actions.executed.ExecutedFacet;
import org.apache.isis.metamodel.facets.actions.executed.ExecutedFacet.Where;
import org.apache.isis.metamodel.facets.actions.exploration.ExplorationFacet;
import org.apache.isis.metamodel.facets.collections.typeof.TypeOfFacet;
import org.apache.isis.metamodel.facets.disable.DisabledFacet;
import org.apache.isis.metamodel.facets.hide.HiddenFacet;
import org.apache.isis.metamodel.facets.naming.describedas.DescribedAsFacet;
import org.apache.isis.metamodel.facets.naming.named.NamedFacet;
import org.apache.isis.metamodel.facets.object.bounded.BoundedFacet;
import org.apache.isis.metamodel.facets.object.immutable.ImmutableFacet;
import org.apache.isis.metamodel.facets.object.plural.PluralFacet;
import org.apache.isis.metamodel.facets.ordering.actionorder.ActionOrderFacet;
import org.apache.isis.metamodel.facets.ordering.fieldorder.FieldOrderFacet;
import org.apache.isis.metamodel.facets.propcoll.notpersisted.NotPersistedFacet;
import org.apache.isis.metamodel.facets.propparam.multiline.MultiLineFacet;
import org.apache.isis.metamodel.facets.propparam.validate.mandatory.OptionalFacet;
import org.apache.isis.progmodel.java5.annotations.JavaObjectWithAnnotations;
import org.apache.isis.progmodel.java5.facets.ordering.OrderSet;
import org.apache.isis.progmodel.java5.reflect.actions.JavaAction;
import org.apache.isis.progmodel.java5.reflect.propcoll.JavaField;
import org.apache.isis.nof.reflect.peer.ActionParamPeer;
import org.apache.isis.nof.testsystem.ProxyTestCase;
import org.apache.isis.testing.DummyObjectAdapter;
import org.apache.isis.testing.TestSpecification;


public class JavaIntrospectorWithAnnotationsTest extends ProxyTestCase {

    public static void main(final String[] args) {
        junit.textui.TestRunner.run(new TestSuite(JavaIntrospectorWithAnnotationsTest.class));
    }

    private JavaIntrospector introspector;
    private ObjectAdapter adapter;
    private JavaObjectWithBasicProgramConventions target;
    private TestFacetHolder facetHolder;

    private JavaAction findAction(String name) {
        OrderSet objectActions = introspector.getObjectActions();
        return findAction(name, objectActions);
    }

    private JavaAction findClassAction(String name) {
        OrderSet objectActions = introspector.getClassActions();
        return findAction(name, objectActions);
    }

    private JavaAction findAction(String name, OrderSet objectActions) {
        Enumeration elements = objectActions.elements();
        while (elements.hasMoreElements()) {
            JavaAction action = (JavaAction) elements.nextElement();
            if (action.getIdentifier().getName().equals(name)) {
                return action;
            }
        }
        fail("action not found: " + name);
        return null;
    }
    
    private JavaField findField(String name) {
        OrderSet fields = introspector.getFields();
        Enumeration elements = fields.elements();
        while (elements.hasMoreElements()) {
            JavaField field = (JavaField) elements.nextElement();
            if (field.getIdentifier().getName().equals(name)) {
                return field;
            }
        }
        fail("action not found: " + name);
        return null;
    }

    protected void setUp() throws Exception {
        super.setUp();

        facetHolder = new TestFacetHolder();
        introspector = new JavaIntrospector(JavaObjectWithAnnotations.class, null, new DummyBuilder(), new JavaReflector(), facetHolder);
        introspector.introspect();
        
        target = new JavaObjectWithBasicProgramConventions();
        adapter = new DummyObjectAdapter(target, ResolveState.RESOLVED);
        ((DummyObjectAdapter) adapter).setupSpecification(new TestSpecification());
    }

    
    public void testBounded() throws Exception {
        Facet facet = facetHolder.getFacet(BoundedFacet.class);
        assertNotNull(facet);
    }

    

    public void testMethodNotDebugByDefault() throws Exception {
        JavaAction action = findAction("stop");
        Facet facet = action.getFacet(DebugFacet.class);
        assertNull(facet);
    }
    
    public void testDebugType() throws Exception {
        JavaAction action = findAction("start");
        Facet facet = action.getFacet(DebugFacet.class);
        assertNotNull(facet);
    }

    public void testMethodNotExplorationByDefault() throws Exception {
        JavaAction action = findAction("stop");
        Facet facet = action.getFacet(ExplorationFacet.class);
        assertNull(facet);
    }
    
    public void testExplorationType() throws Exception {
        JavaAction action = findAction("top");
        Facet facet = action.getFacet(ExplorationFacet.class);
        assertNotNull(facet);
    }
    
    public void testFieldNotPersisted() throws Exception {
        JavaField action = findField("one");
        Facet facet = action.getFacet(NotPersistedFacet.class);
        assertNotNull(facet);
    } 
    
    public void testMethodHidden() throws Exception {
        JavaAction action = findAction("stop");
        Facet facet = action.getFacet(HiddenFacet.class);
        assertNotNull(facet);
    }
    
    public void testImmutableOncePersisted() throws Exception {
        ImmutableFacet facet = (ImmutableFacet)facetHolder.getFacet(ImmutableFacet.class);
        assertEquals(When.ONCE_PERSISTED, facet.value());
    }

    public void testExecutionDefault() throws Exception {
        JavaAction action = findAction("start");
        ExecutedFacet facet = (ExecutedFacet) action.getFacet(ExecutedFacet.class);
        assertEquals(Where.DEFAULT, facet.value());
    }

    public void testLocal() throws Exception {
        JavaAction action = findAction("left");
        ExecutedFacet facet = (ExecutedFacet) action.getFacet(ExecutedFacet.class);
        assertEquals(Where.LOCALLY, facet.value());
    }

    public void testRemote() throws Exception {
        JavaAction action = findAction("right");
        ExecutedFacet facet = (ExecutedFacet) action.getFacet(ExecutedFacet.class);
        assertEquals(Where.REMOTELY, facet.value());
    }
    
    public void testActionNotDisableByDefault() throws Exception {
        JavaAction action = findAction("start");
        DisabledFacet facet = (DisabledFacet) action.getFacet(DisabledFacet.class);
        assertEquals(When.NEVER, facet.value());
    }
        
    public void testActionDisabled() throws Exception {
        JavaAction action = findAction("bottom");
        DisabledFacet facet = (DisabledFacet) action.getFacet(DisabledFacet.class);
        assertEquals(When.ALWAYS, facet.value());
    }

    public void testClassActionOrder() throws Exception {
        ActionOrderFacet facet = (ActionOrderFacet)facetHolder.getFacet(ActionOrderFacet.class);
        assertEquals("1, 2, 3", facet.value());
    }
    
    public void testCollectionType() throws Exception {
        JavaField field = findField("collection");
        TypeOfFacet facet =  (TypeOfFacet) field.getFacet(TypeOfFacet.class);
        assertEquals(Long.class, facet.value());
    }

    public void testFieldOrder() throws Exception {
        FieldOrderFacet facet = (FieldOrderFacet)facetHolder.getFacet(FieldOrderFacet.class);
        assertEquals("4, 5, 6", facet.value());
    }

    public void testFieldDescription() throws Exception {
        JavaField field = findField("collection");
        DescribedAsFacet facet =  (DescribedAsFacet) field.getFacet(DescribedAsFacet.class);
        assertEquals("description text", facet.value());
    }

    public void testMemberName() throws Exception {
        JavaField field = findField("collection");
        NamedFacet facet =  (NamedFacet) field.getFacet(NamedFacet.class);
        assertEquals("name text", facet.value());
    }

    public void testParameterNamesDefault() throws Exception {
        JavaAction action = findAction("complete");
        ActionParamPeer actionParamPeer = action.getParameters()[0];
        NamedFacet facet = (NamedFacet) actionParamPeer.getFacet(NamedFacet.class);
        assertEquals(null, facet.value());
    }
    
    public void testParameterNames() throws Exception {
        JavaAction action = findAction("side");
        ActionParamPeer actionParamPeer = action.getParameters()[0];
        NamedFacet facet = (NamedFacet) actionParamPeer.getFacet(NamedFacet.class);
        assertEquals("one", facet.value());
   }
    
    public void testParameterMandatoryByDefault() throws Exception {
        JavaAction action = findAction("complete");
        ActionParamPeer actionParamPeer = action.getParameters()[0];
        OptionalFacet facet = (OptionalFacet) actionParamPeer.getFacet(OptionalFacet.class);
        assertNull(facet);
    }

    public void testOptionalParameter() throws Exception {
        JavaAction action = findAction("side");
        ActionParamPeer actionParamPeer = action.getParameters()[0];
        OptionalFacet facet = (OptionalFacet) actionParamPeer.getFacet(OptionalFacet.class);
        assertNotNull(facet);
    }

    public void testSingularName() throws Exception {
        NamedFacet facet = (NamedFacet)facetHolder.getFacet(NamedFacet.class);
        assertEquals("singular name", facet.value());
    }

    public void testPluralName() throws Exception {
        PluralFacet facet = (PluralFacet)facetHolder.getFacet(PluralFacet.class);
        assertEquals("plural name", facet.value());
    }
    
    public void testDelfault1LineForParameter() throws Exception {
        JavaAction action = findAction("complete");
        ActionParamPeer actionParamPeer = action.getParameters()[0];
        MultiLineFacet facet = (MultiLineFacet) actionParamPeer.getFacet(MultiLineFacet.class);
        assertEquals(1, facet.numberOfLines());
    }
    
    public void testMultiLineForActionParamIfNot() throws Exception {
        JavaAction action = findAction("complete");
        ActionParamPeer actionParamPeer = action.getParameters()[1];
        MultiLineFacet facet = (MultiLineFacet) actionParamPeer.getFacet(MultiLineFacet.class);
        assertEquals(10, facet.numberOfLines());
    }

    
}
