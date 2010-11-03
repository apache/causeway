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
import java.util.HashMap;

import junit.framework.TestSuite;

import org.apache.isis.noa.adapter.ObjectAdapter;
import org.apache.isis.noa.adapter.ResolveState;
import org.apache.isis.noa.facets.Facet;
import org.apache.isis.metamodel.facets.FacetHolderMutable;
import org.apache.isis.noa.facets.FacetUtil;
import org.apache.isis.metamodel.facets.When;
import org.apache.isis.metamodel.facets.collections.typeof.TypeOfFacet;
import org.apache.isis.metamodel.facets.hide.HiddenFacet;
import org.apache.isis.metamodel.facets.naming.named.NamedFacet;
import org.apache.isis.metamodel.facets.object.bounded.BoundedFacet;
import org.apache.isis.metamodel.facets.object.immutable.ImmutableFacet;
import org.apache.isis.metamodel.facets.object.notpersistable.NotPersistableFacet;
import org.apache.isis.metamodel.facets.object.notpersistable.NotPersistableFacet.By;
import org.apache.isis.metamodel.facets.object.plural.PluralFacet;
import org.apache.isis.metamodel.facets.propcoll.notpersisted.NotPersistedFacet;
import org.apache.isis.noa.reflect.Consent;
import org.apache.isis.noa.reflect.ObjectAction;
import org.apache.isis.noa.reflect.facet.AddToCollectionFacet;
import org.apache.isis.noa.reflect.facet.HideForSessionFacet;
import org.apache.isis.noa.reflect.facet.RemoveFromCollectionFacet;
import org.apache.isis.nof.core.reflect.Allow;
import org.apache.isis.nof.core.util.NotImplementedException;
import org.apache.isis.progmodel.java5.facets.collections.write.AddToCollectionMethodFacet;
import org.apache.isis.progmodel.java5.facets.collections.write.AddToJavaCollectionViaAccessorFacet;
import org.apache.isis.progmodel.java5.facets.collections.write.RemoveFromCollectionMethodFacet;
import org.apache.isis.progmodel.java5.facets.collections.write.RemoveFromJavaCollectionViaAccessorFacet;
import org.apache.isis.progmodel.java5.facets.ordering.OrderSet;
import org.apache.isis.progmodel.java5.reflect.actions.JavaAction;
import org.apache.isis.progmodel.java5.reflect.collections.JavaCollectionAssociation;
import org.apache.isis.progmodel.java5.reflect.propcoll.JavaField;
import org.apache.isis.progmodel.java5.reflect.properties.JavaValueAssociation;
import org.apache.isis.nof.reflect.peer.ActionParamPeer;
import org.apache.isis.nof.testsystem.ProxyTestCase;
import org.apache.isis.testing.DummyObjectAdapter;
import org.apache.isis.testing.TestSession;
import org.apache.isis.testing.TestSpecification;


public class JavaIntrospectorTest extends ProxyTestCase {

    public static void main(final String[] args) {
        junit.textui.TestRunner.run(new TestSuite(JavaIntrospectorTest.class));
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

    private JavaAction findAction(final String name, final OrderSet objectActions) {
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
        introspector = new JavaIntrospector(JavaObjectWithBasicProgramConventions.class, null, new DummyBuilder(), new JavaReflector(), facetHolder);
        introspector.introspect();
        
        target = new JavaObjectWithBasicProgramConventions();
        adapter = new DummyObjectAdapter(target, ResolveState.RESOLVED);
        ((DummyObjectAdapter) adapter).setupSpecification(new TestSpecification());
    }
    
    public void testActionNameFromMethod() throws Exception {
        JavaAction action = findAction("start");
        assertEquals("Start", action.getIdentifier().getName());
    }
    
    public void testActionNameFromHelperMethod() throws Exception {
        JavaAction action = findAction("stop");
        assertEquals("object action name", action.getIdentifier().getName());
    }

    public void testActionNameFromMethodWithDebugPrefix() throws Exception {
        JavaAction action = findAction("debugTwo");
        assertEquals("Two", action.getIdentifier().getName());
    }

    public void testActionNameFromMethodWithLocalPrefix() throws Exception {
        JavaAction action = findAction("localRunOnClient");
        assertEquals("Run On Client", action.getIdentifier().getName());
    }
    
    public void testActionParameterCountWithNoParameters() {
        JavaAction action = findAction("stop");
        assertEquals(0, action.getParameterCount());
    }
    
    public void testActionParameterCountWithOneParameter() {
        JavaAction action = findAction("start");
        assertEquals(1, action.getParameterCount());
    }
    
    public void testActionParameterArrayRightSize() {
        JavaAction action = findAction("start");
        ActionParamPeer[] parameters = action.getParameters();
        assertEquals(1, parameters.length);
    }
    
    public void testActionParameterDefaults() {
        JavaAction action = findAction("start");
        Object[] defaults = action.getParameterDefaults(adapter);
        assertEquals(1, defaults.length);
        assertEquals("default param", defaults[0]);
    }        
      
    public void testNoActionParameterDefaults() {
        JavaAction action = findAction("stop");
        assertEquals(null, action.getParameterDefaults(adapter));
    }
    
    public void testActionParameterNames() {
        JavaAction action = findAction("start");
        ActionParamPeer[] parameters = action.getParameters();
        assertEquals(1, parameters.length);
        assertEquals("parameter name", parameters[0].getName());
    }
    
    public void testNoActionParameterNames() {
        JavaAction action = findAction("stop");
        String[] parameterNames = action.getParameterNames();
        assertEquals(0, parameterNames.length);
    }

    public void testActionParameterOptionsViaTypedArray() {
        JavaAction action = findAction("start");
        Object[][] parameterOptions = action.getParameterOptions(adapter);
        assertEquals(1, parameterOptions.length);
        assertEquals(3, parameterOptions[0].length);
        assertEquals("one", parameterOptions[0][0]);
        assertEquals("two", parameterOptions[0][1]);
        assertEquals("three", parameterOptions[0][2]);
    }
    
    public void testActionParameterOptionsViaObjectArray() {
        JavaAction action = findAction("start2");
        Object[][] parameterOptions = action.getParameterOptions(adapter);
        assertEquals(1, parameterOptions.length);
        assertEquals(3, parameterOptions[0].length);
        assertEquals("three", parameterOptions[0][0]);
        assertEquals("two", parameterOptions[0][1]);
        assertEquals("one", parameterOptions[0][2]);
    }

    public void testActionParameterNoOptions() {
        JavaAction action = findAction("stop");
        Object[][] parameterOptions = action.getParameterOptions(adapter);
        assertTrue(parameterOptions instanceof Object[][]);
        assertEquals(0, parameterOptions.length);
    }

    public void testActionParametersOptional() {
        JavaAction action = findAction("start");
        assertEquals(true, action.getParameters()[0]);
        assertEquals(1, action.getOptionalParameters().length);
    }
    
    public void testActionParameterDefaultsToMandatory() {
        JavaAction action = findAction("debugTwo");
        assertEquals(false, action.getOptionalParameters()[0]);
        assertEquals(1, action.getOptionalParameters().length);
    }

    public void testActionReturnTypeVoid() throws Exception {
        JavaAction action = findAction("stop");
        assertEquals(null, action.getReturnType());
    }
    
    public void testActionReturnType() throws Exception {
        JavaAction action = findAction("start");
        assertEquals(system.getSpecification(int.class), action.getReturnType());
    }

    public void testActionSortOrder() {
        OrderSet actions = introspector.getObjectActions();
        Enumeration elements = actions.elements();
        assertEquals("start", ((JavaAction) elements.nextElement()).getIdentifier().getName());
        assertEquals("stop", ((JavaAction) elements.nextElement()).getIdentifier().getName());
        assertNotNull
        
        
        (((JavaAction) elements.nextElement()).getIdentifier().getName());
    }

    public void testActionWithDefaultTarget() throws Exception {
        JavaAction action = findAction("debugTwo");;
        assertEquals(ObjectAction.DEFAULT, action.getTarget());
    }

    public void testActionWithRemoteTarget() {
        JavaAction action = findAction("remoteRunOnServer");
        assertEquals(ObjectAction.REMOTE, action.getTarget());
    }

    public void testActionWithTypeOfDebug() throws Exception {
        JavaAction action = findAction("debugTwo");;
        assertEquals(ObjectAction.DEBUG, action.getType());
    }

    public void testActionWithTypeOfExploration() throws Exception {
        JavaAction action = findAction("explorationSetUp");
        assertEquals(ObjectAction.EXPLORATION, action.getType());
    }

    public void testActionWithTypeOfUser() throws Exception {
        JavaAction action = findAction("stop");
        assertEquals(ObjectAction.USER, action.getType());
    }


    public void testName() throws Exception {
        Facet facet = facetHolder.getFacet(BoundedFacet.class);
        assertNull(facet);
    }
    
    public void testNotImmutableByDefault() throws Exception {
        ImmutableFacet facet = (ImmutableFacet)facetHolder.getFacet(ImmutableFacet.class);
        assertNull(facet);
    }
    
    public void testClassActionList() {
        OrderSet actions = introspector.getClassActions();
        assertEquals(2, actions.size());
    }

    public void testClassActionNameFromMethodSignature() {
        JavaAction action = findClassAction("bottom");
        assertEquals("Bottom", action.getIdentifier().getName());
    }

    public void testClassActionNameFromHelperMethod() {
        JavaAction action = findClassAction("top");
        assertEquals("class action name", action.getIdentifier().getName());
    }

    public void testClassActionSortOrder() {
        OrderSet actions = introspector.getClassActions();
        Enumeration elements = actions.elements();

        assertEquals("top", ((JavaAction) elements.nextElement()).getIdentifier().getName());
        assertEquals("bottom", ((JavaAction) elements.nextElement()).getIdentifier().getName());
    }

    public void testClassActionValidatesWhenNoValidateMethodExists() {
        JavaAction action = findClassAction("bottom");
        assertEquals(Allow.DEFAULT, action.isParameterSetValidImperatively(null, new ObjectAdapter[0]));
    }
    
    public void testClassActionValidate() {
        JavaAction action = findClassAction("top");
        assertEquals(Allow.DEFAULT, action.isParameterSetValidImperatively(null, new ObjectAdapter[0]));
    }

    public void testClassActionValidateFails() {
        JavaAction action = findClassAction("top");
        JavaObjectWithBasicProgramConventions.classActionValid = "not now";
        assertEquals(true, action.isParameterSetValidImperatively(null, new ObjectAdapter[0]).isVetoed());
        assertEquals("not now", action.isParameterSetValidImperatively(null, new ObjectAdapter[0]).getReason());
    }

    public void testCollectionReturnCollectionFieldProxy() throws Exception {
        JavaField field = findField("five");
        assertTrue(field instanceof JavaCollectionAssociation);
    }

    public void testCollectionFieldPicksUpHelperMethods() throws Exception {
        JavaField field = findField("five");
        assertEquals(AddToCollectionMethodFacet.class, field.getFacet(AddToCollectionFacet.class).getClass());
        assertEquals(RemoveFromCollectionMethodFacet.class, field.getFacet(RemoveFromCollectionFacet.class).getClass());
    }

    public void testCollectionField() throws Exception {
        JavaField field = findField("five");
        TypeOfFacet typeOf = (TypeOfFacet) field.getFacet(TypeOfFacet.class);
        assertEquals(JavaReferencedObject.class, typeOf.value());
    }
    
    public void testCollectionFieldWithOutHelperUsesAccessorToAddAndRemove() throws Exception {
        JavaField field = findField("nine");
        assertEquals(AddToJavaCollectionViaAccessorFacet.class, field.getFacet(AddToCollectionFacet.class).getClass());
        assertEquals(RemoveFromJavaCollectionViaAccessorFacet.class, field.getFacet(RemoveFromCollectionFacet.class).getClass());
    }
    
    public void testCollectionFieldWithOutHelperHasNoTypeOf() throws Exception {
        JavaField field = findField("nine");
        assertNull(field.getFacet(TypeOfFacet.class));
    }
    
    public void testFields() throws Exception {
        OrderSet fields = introspector.getFields();
        assertEquals(8, fields.size());
    }

    public void testNonPersistentFieldWhenNoSetter() throws Exception {
        JavaField field = findField("three");
        assertEquals(false, field.isPersisted());
    }
    
    public void testPersistentFieldWhenHasSetter() throws Exception {
        JavaField field = findField("one");
        assertEquals(true, field.isPersisted());
    }

    public void testHiddenField() throws Exception {
        JavaField field = findField("one");
        TestSession testSession = new TestSession();
        assertEquals(false, field.isUsableForSession(testSession).isAllowed());
        
        field = findField("six");
        assertEquals(true, field.isUsableForSession(testSession).isAllowed());
    }

    public void testProtectedField() throws Exception {
        JavaField field = findField("one");
        assertEquals(Allow.DEFAULT, field.isUsable(adapter));
        
        field = findField("eight");
        Consent usable = field.isUsableDeclaratively();
        assertTrue(usable.isVetoed());
        assertEquals("Field not editable", usable.getReason());
    }
    
    public void testFieldVisibleByDefault() throws Exception {
        JavaField field = findField("one");
        assertEquals(true, field.isVisible(adapter));
    }
    
    public void testHideFieldWithHideInContextHelperMethod() throws Exception {
        JavaField field = findField("fieldTwo");
        assertEquals(true, field.isVisible(adapter));
    }

    
    public void testFieldVisibleForSessionByDefault() throws Exception {
        JavaField field = findField("one");
        Facet facet = field.getFacet(HideForSessionFacet.class);
        assertNull(facet);
    }

    public void testHideFieldWithHideForSessionHelperMethod() throws Exception {
        JavaField field = findField("fieldTwo");
        Facet facet = field.getFacet(HideForSessionFacet.class);
        assertNotNull(facet);
    }
    
    public void testMandatoryField() throws Exception {
        JavaField field = findField("six");
        assertEquals(true, field.isMandatory());
        
        field = findField("one");
        assertEquals(false, field.isMandatory());
    }
    
    public void testFieldDefault() throws Exception {
        JavaValueAssociation field = (JavaValueAssociation) findField("six");
        assertEquals(null, field.getDefault(adapter));
        
        field = (JavaValueAssociation) findField("one");
        assertEquals("default value", field.getDefault(adapter).getObject());
    }
    
    
    public void testFieldOptions() throws Exception {
        JavaField field = findField("six");
        Object[] options = field.getOptions(adapter);
        assertTrue(options instanceof Object[]);
        assertEquals(2, options.length);
    }
    
    public void testNoFieldOptions() throws Exception {
        JavaField field = findField("five");
        Object[] options = field.getOptions(adapter);
        assertNull(options);
    }


    public void testFieldUsableByDefault() throws Exception {
        JavaField field = findField("one");
        assertEquals(Allow.DEFAULT, field.isUsable(adapter));
    }
    
    public void testFieldUnusableWhenFlaggedByHelperMethod() throws Exception {
        JavaField field = findField("seven");
        Consent usable = field.isUsable(adapter);
        assertEquals(true, usable.isVetoed());
        assertEquals("no changes", usable.getReason());
    }

    public void testFieldSortOrder() {
        OrderSet fields = introspector.getFields();
        Enumeration elements = fields.elements();
        assertEquals("one", ((JavaField) elements.nextElement()).getIdentifier().getName());
        assertEquals("fieldTwo", ((JavaField) elements.nextElement()).getIdentifier().getName());
        assertEquals("three", ((JavaField) elements.nextElement()).getIdentifier().getName());
        assertEquals("five", ((JavaField) elements.nextElement()).getIdentifier().getName());
    }

    public void testFieldsUserUnusable() {
        OrderSet fields = introspector.getFields();
        Enumeration elements = fields.elements();

        TestSession session = new TestSession();
        Consent usable = ((JavaField) elements.nextElement()).isUsableForSession(session);
        assertTrue(usable.isVetoed());
        assertEquals("no edits", usable.getReason());
        assertEquals(Allow.DEFAULT, ((JavaField) elements.nextElement()).isUsableForSession(session));
        
        usable = ((JavaField) elements.nextElement()).isUsableDeclaratively();
        assertTrue(usable.isVetoed());
        assertEquals("Field not editable", usable.getReason());
    }

    public void testInterfaces() {
        String[] interfaces = introspector.getInterfaces();
        assertEquals(2, interfaces.length);
        assertEquals(Interface1.class.getName(), interfaces[0]);
        assertEquals(Interface2.class.getName(), interfaces[1]);
    }

    public void testLocalMethod() {
        JavaAction action = findAction("localRunOnClient");
        assertEquals(ObjectAction.LOCAL, action.getTarget());
    }

    public void testLookup() {
        assertEquals(null, getFacet(BoundedFacet.class));
    }

    public void testNameManipulations() {
        assertEquals("CarRegistration", JavaIntrospector.javaBaseName("getCarRegistration"));
        assertEquals("Driver", JavaIntrospector.javaBaseName("Driver"));
        assertEquals("Register", JavaIntrospector.javaBaseName("actionRegister"));
        assertEquals("", JavaIntrospector.javaBaseName("action"));
    }

    public void testObjectActionDescriptionFromHelperMethod() {
        JavaAction action = findAction("stop");
        assertEquals("object action description", action.getDescription());
    }
    
    public void testObjectActionNoDescriptionByDefault() {
        JavaAction action = findAction("start");
        assertEquals("", action.getDescription());
    }

    public void testObjectActionList() {
        OrderSet actions = introspector.getObjectActions();
        assertEquals(8, actions.size());
    }

    public void testActionVisibleForSessionByDefault() throws Exception {
        JavaAction field = findAction("start");
        assertEquals(true, field.isVisibleForSession(null));
    }

    public void testHideActionWithHideForSessionHelperMethod() throws Exception {
        JavaAction field = findAction("hiddenToUser");
        Facet facet = field.getFacet(HideForSessionFacet.class);
        assertNotNull(facet);
    }
    

    public void testOptionParametersDeclaredByHelperMethod() {
        JavaAction action = findAction("start");
        assertEquals(1, action.getOptionalParameters().length);
        assertEquals(true, action.getOptionalParameters()[0]);
    }
    
    public void testMandatoryParametersByDefault() {
        JavaAction action = findAction("start2");
        assertEquals(1, action.getOptionalParameters().length);
        assertEquals(false, action.getOptionalParameters()[0]);
    }
    
    public void testObjectActionValidate() {
        OrderSet actions = introspector.getObjectActions();
        Enumeration elements = actions.elements();

        JavaAction action = ((JavaAction) elements.nextElement());
        assertEquals(Allow.DEFAULT, action.isParameterSetValidImperatively(adapter, new ObjectAdapter[1]));
        target.objectActionValid = "not valid";
        assertEquals(true, action.isParameterSetValidImperatively(adapter, new ObjectAdapter[1]).isVetoed());
        assertEquals("not valid", action.isParameterSetValidImperatively(adapter, new ObjectAdapter[1]).getReason());

        action = ((JavaAction) elements.nextElement());
        assertEquals(Allow.DEFAULT, action.isParameterSetValidImperatively(adapter, new ObjectAdapter[1]));
        assertEquals(Allow.DEFAULT, action.isParameterSetValidImperatively(adapter, new ObjectAdapter[1]));

    }

    public void testPersistable() {
        NotPersistableFacet facet = getFacet(NotPersistableFacet.class);        
        assertEquals(null, facet);
    }
    
    public void testNotPersistable() {
        introspector = new JavaIntrospector(JavaObjectMarkedAsTransient.class, null, new DummyBuilder(), new JavaReflector(), facetHolder);
        introspector.introspect();
        
        NotPersistableFacet facet = getFacet(NotPersistableFacet.class);     
        assertEquals(By.USER_OR_PROGRAM, facet.value());
        //assertEquals(Persistable.TRANSIENT, introspector.persistable());

    }

    public void testPluralName() {
        PluralFacet facet = getFacet(PluralFacet.class);
        assertEquals("Plural", facet.value());
    }

    public void testShortName() {
        assertEquals("JavaObjectWithBasicProgramConventions", introspector.shortName());
    }

    public void testSingularName() {
        NamedFacet facet =getFacet(NamedFacet.class);
        assertEquals("Singular", facet.value());
    }

    public <T> T getFacet(Class<T> cls) {
        return  (T) facetHolder.getFacet(cls);
    }
    
    public void testSuperclass() {
        assertEquals(Object.class.getName(), introspector.getSuperclass());
    }
    
    public void testFieldPersistableByDefault() throws Exception {
        JavaField action = findField("one");
        Facet facet = action.getFacet(NotPersistedFacet.class);
        assertNull(facet);
    }    

    public void testMethodNotHiddenByDefault() throws Exception {
        JavaAction action = findAction("stop");
        HiddenFacet facet = (HiddenFacet) action.getFacet(HiddenFacet.class);
        assertEquals(When.NEVER, facet.value());
    }
}

class TestFacetHolder implements FacetHolderMutable {
    HashMap facetsByClass = new HashMap();
    
    public void addFacet(Facet facet) {
        facetsByClass.put(facet.facetType(), facet);
    }

    public Facet getFacet(Class cls) {
        return (Facet) facetsByClass.get(cls);
    }

	public void removeFacet(Facet facet) {
		FacetUtil.removeFacet(facetsByClass, facet);
	}

    public Class[] getFacetTypes() {
        return FacetUtil.getFacetTypes(facetsByClass);
    }

	public Facet[] getFacets(Facet.Filter filter) {
        return FacetUtil.getFacets(facetsByClass, filter);
	}


}
