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

package org.apache.isis.core.metamodel.facets.actions;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.jmock.Expectations;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.When;
import org.apache.isis.applib.security.UserMemento;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.actions.debug.DebugFacet;
import org.apache.isis.core.metamodel.facets.actions.defaults.ActionDefaultsFacet;
import org.apache.isis.core.metamodel.facets.actions.defaults.method.ActionDefaultsFacetViaMethod;
import org.apache.isis.core.metamodel.facets.actions.defaults.method.ActionDefaultsFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.actions.exploration.ExplorationFacet;
import org.apache.isis.core.metamodel.facets.actions.interaction.ActionNamedDebugExplorationFacetFactory;
import org.apache.isis.core.metamodel.facets.actions.validate.ActionValidationFacet;
import org.apache.isis.core.metamodel.facets.actions.validate.method.ActionValidationFacetViaMethod;
import org.apache.isis.core.metamodel.facets.actions.validate.method.ActionValidationFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.all.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.facets.all.describedas.DescribedAsFacetAbstract;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacetAbstract;
import org.apache.isis.core.metamodel.facets.members.describedas.staticmethod.DescribedAsFacetStaticMethodFactory;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacet;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacetAbstract;
import org.apache.isis.core.metamodel.facets.members.disabled.forsession.DisableForSessionFacet;
import org.apache.isis.core.metamodel.facets.members.disabled.forsession.DisableForSessionFacetViaMethod;
import org.apache.isis.core.metamodel.facets.members.disabled.forsession.DisableForSessionFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.members.disabled.staticmethod.DisabledFacetStaticMethodFacetFactory;
import org.apache.isis.core.metamodel.facets.members.hidden.forsession.HideForSessionFacet;
import org.apache.isis.core.metamodel.facets.members.hidden.forsession.HideForSessionFacetViaMethod;
import org.apache.isis.core.metamodel.facets.members.hidden.forsession.HideForSessionFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.members.hidden.staticmethod.HiddenFacetOnStaticMethod;
import org.apache.isis.core.metamodel.facets.members.hidden.staticmethod.HiddenFacetStaticMethodFactory;
import org.apache.isis.core.metamodel.facets.members.named.staticmethod.NamedFacetStaticMethodFactory;
import org.apache.isis.core.metamodel.facets.param.autocomplete.ActionParameterAutoCompleteFacet;
import org.apache.isis.core.metamodel.facets.param.autocomplete.method.ActionParameterAutoCompleteFacetViaMethod;
import org.apache.isis.core.metamodel.facets.param.autocomplete.method.ActionParameterAutoCompleteFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.param.choices.ActionChoicesFacet;
import org.apache.isis.core.metamodel.facets.param.choices.ActionParameterChoicesFacet;
import org.apache.isis.core.metamodel.facets.param.choices.method.ActionChoicesFacetViaMethod;
import org.apache.isis.core.metamodel.facets.param.choices.method.ActionChoicesFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.param.choices.methodnum.ActionParameterChoicesFacetViaMethod;
import org.apache.isis.core.metamodel.facets.param.choices.methodnum.ActionParameterChoicesFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.param.defaults.ActionParameterDefaultsFacet;
import org.apache.isis.core.metamodel.facets.param.defaults.methodnum.ActionParameterDefaultsFacetViaMethod;
import org.apache.isis.core.metamodel.facets.param.defaults.methodnum.ActionParameterDefaultsFacetViaMethodFactory;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.testspec.ObjectSpecificationStub;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;

public class ActionMethodsFacetFactoryTest extends AbstractFacetFactoryTest {

    private JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    private final ObjectSpecification voidSpec = new ObjectSpecificationStub("VOID");
    private final ObjectSpecification stringSpec = new ObjectSpecificationStub("java.lang.String");
    private final ObjectSpecification customerSpec = new ObjectSpecificationStub("Customer");

    private ServicesInjector mockServicesInjector;
    private TranslationService mockTranslationService;

    public void setUp() throws Exception {
        super.setUp();
        mockServicesInjector = context.mock(ServicesInjector.class);
        mockTranslationService = context.mock(TranslationService.class);

        context.checking(new Expectations() {{
            allowing(mockServicesInjector).lookupService(TranslationService.class);
            will(returnValue(mockTranslationService));
        }});
    }

    public void testProvidesDefaultNameForActionButIgnoresAnyNamedAnnotation() {
        final ActionNamedDebugExplorationFacetFactory facetFactory = new ActionNamedDebugExplorationFacetFactory();
        facetFactory.setSpecificationLookup(programmableReflector);
        programmableReflector.setLoadSpecificationStringReturn(voidSpec);

        class Customer {
            @SuppressWarnings("unused")
            @Named("Renamed an action with a named annotation")
            public void anActionWithNamedAnnotation() {
            }
        }
        final Method method = findMethod(Customer.class, "anActionWithNamedAnnotation");

        facetFactory.process(new ProcessMethodContext(Customer.class, null, null, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(NamedFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof NamedFacet);
        final NamedFacet namedFacet = (NamedFacet) facet;
        assertEquals("An Action With Named Annotation", namedFacet.value());
    }

    public void testPicksUpDebugPrefixAndSetsNameAppropriatelyAlso() {
        final ActionNamedDebugExplorationFacetFactory facetFactory = new ActionNamedDebugExplorationFacetFactory();
        facetFactory.setSpecificationLookup(programmableReflector);
        programmableReflector.setLoadSpecificationStringReturn(voidSpec);

        class Customer {
            @SuppressWarnings("unused")
            public void debugAnActionWithDebugPrefix() {
            }
        }
        final Method method = findMethod(Customer.class, "debugAnActionWithDebugPrefix");
        facetFactory.process(new ProcessMethodContext(Customer.class, null, null, method, methodRemover, facetedMethod));

        Facet facet = facetedMethod.getFacet(DebugFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof DebugFacet);

        facet = facetedMethod.getFacet(NamedFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof NamedFacet);
        final NamedFacet namedFacet = (NamedFacet) facet;
        assertEquals("An Action With Debug Prefix", namedFacet.value());
    }

    public void testPicksUpExplorationPrefixAndSetsNameAppropriatelyAlso() {
        final ActionNamedDebugExplorationFacetFactory facetFactory = new ActionNamedDebugExplorationFacetFactory();
        facetFactory.setSpecificationLookup(programmableReflector);
        programmableReflector.setLoadSpecificationStringReturn(voidSpec);

        class Customer {
            @SuppressWarnings("unused")
            public void explorationAnActionWithExplorationPrefix() {
            }
        }
        final Method method = findMethod(Customer.class, "explorationAnActionWithExplorationPrefix");
        facetFactory.process(new ProcessMethodContext(Customer.class, null, null, method, methodRemover, facetedMethod));

        Facet facet = facetedMethod.getFacet(ExplorationFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof ExplorationFacet);

        facet = facetedMethod.getFacet(NamedFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof NamedFacet);
        final NamedFacet namedFacet = (NamedFacet) facet;
        assertEquals("An Action With Exploration Prefix", namedFacet.value());
    }

    public void testCannotHaveBothDebugAndThenExplorationPrefix() {
        final ActionNamedDebugExplorationFacetFactory facetFactory = new ActionNamedDebugExplorationFacetFactory();
        facetFactory.setSpecificationLookup(programmableReflector);
        programmableReflector.setLoadSpecificationStringReturn(voidSpec);

        class Customer {
            @SuppressWarnings("unused")
            public void debugExplorationAnActionWithDebugAndExplorationPrefix() {
            }
        }
        final Method method = findMethod(Customer.class, "debugExplorationAnActionWithDebugAndExplorationPrefix");
        facetFactory.process(new ProcessMethodContext(Customer.class, null, null, method, methodRemover, facetedMethod));

        Facet facet = facetedMethod.getFacet(DebugFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof DebugFacet);

        facet = facetedMethod.getFacet(ExplorationFacet.class);
        assertNull(facet);
    }

    public void testCannotHaveBothExplorationAndThenDebugPrefix() {
        final ActionNamedDebugExplorationFacetFactory facetFactory = new ActionNamedDebugExplorationFacetFactory();
        facetFactory.setSpecificationLookup(programmableReflector);
        programmableReflector.setLoadSpecificationStringReturn(voidSpec);

        class Customer {
            @SuppressWarnings("unused")
            public void explorationDebugAnActionWithExplorationAndDebugPrefix() {
            }
        }
        final Method method = findMethod(Customer.class, "explorationDebugAnActionWithExplorationAndDebugPrefix");
        facetFactory.process(new ProcessMethodContext(Customer.class, null, null, method, methodRemover, facetedMethod));

        Facet facet = facetedMethod.getFacet(ExplorationFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof ExplorationFacet);

        facet = facetedMethod.getFacet(DebugFacet.class);
        assertNull(facet);
    }

    public void testInstallsValidateMethodNoArgsFacetAndRemovesMethod() {
        final ActionValidationFacetViaMethodFactory facetFactory = new ActionValidationFacetViaMethodFactory();
        facetFactory.setSpecificationLookup(programmableReflector);
        facetFactory.setServicesInjector(mockServicesInjector);
        programmableReflector.setLoadSpecificationStringReturn(voidSpec);

        class Customer {
            @SuppressWarnings("unused")
            public void someAction() {
            }

            @SuppressWarnings("unused")
            public String validateSomeAction() {
                return null;
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");
        final Method validateMethod = findMethod(Customer.class, "validateSomeAction");

        facetFactory.process(new ProcessMethodContext(Customer.class, null, null, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(ActionValidationFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof ActionValidationFacetViaMethod);
        final ActionValidationFacetViaMethod actionValidationFacetViaMethod = (ActionValidationFacetViaMethod) facet;
        assertEquals(validateMethod, actionValidationFacetViaMethod.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(validateMethod));
    }

    public void testInstallsValidateMethodSomeArgsFacetAndRemovesMethod() {
        final ActionValidationFacetViaMethodFactory facetFactory = new ActionValidationFacetViaMethodFactory();
        facetFactory.setSpecificationLookup(programmableReflector);
        facetFactory.setServicesInjector(mockServicesInjector);
        programmableReflector.setLoadSpecificationStringReturn(voidSpec);

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(final int x, final int y) {
            }

            @SuppressWarnings("unused")
            public String validateSomeAction(final int x, final int y) {
                return null;
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction", new Class[] { int.class, int.class });
        final Method validateMethod = findMethod(Customer.class, "validateSomeAction", new Class[] { int.class, int.class });

        facetFactory.process(new ProcessMethodContext(Customer.class, null, null, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(ActionValidationFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof ActionValidationFacetViaMethod);
        final ActionValidationFacetViaMethod actionValidationFacetViaMethod = (ActionValidationFacetViaMethod) facet;
        assertEquals(validateMethod, actionValidationFacetViaMethod.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(validateMethod));
    }

    public void testInstallsParameterDefaultsMethodNoArgsFacetAndRemovesMethod() {
        final ActionDefaultsFacetViaMethodFactory facetFactory = new ActionDefaultsFacetViaMethodFactory();
        facetFactory.setSpecificationLookup(programmableReflector);
        programmableReflector.setLoadSpecificationStringReturn(voidSpec);

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(final int x, final Long y) {
            }

            @SuppressWarnings("unused")
            public Object[] defaultSomeAction() {
                return null;
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction", new Class[] { int.class, Long.class });
        final Method defaultMethod = findMethod(Customer.class, "defaultSomeAction", new Class[] {});

        facetFactory.process(new ProcessMethodContext(Customer.class, null, null, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(ActionDefaultsFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof ActionDefaultsFacetViaMethod);
        final ActionDefaultsFacetViaMethod actionDefaultFacetViaMethod = (ActionDefaultsFacetViaMethod) facet;
        assertEquals(defaultMethod, actionDefaultFacetViaMethod.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(defaultMethod));
    }

    public void testInstallsParameterDefaultsMethodSomeArgsIsIgnored() {
        final ActionDefaultsFacetViaMethodFactory facetFactory = new ActionDefaultsFacetViaMethodFactory();
        facetFactory.setSpecificationLookup(programmableReflector);
        programmableReflector.setLoadSpecificationStringReturn(voidSpec);

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(final int x, final Long y) {
            }

            @SuppressWarnings("unused")
            public Object[] defaultSomeAction(final int x, final Long y) {
                return null;
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction", new Class[] { int.class, Long.class });

        facetFactory.process(new ProcessMethodContext(Customer.class, null, null, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(ActionDefaultsFacet.class);
        assertNull(facet);
    }

    public void testInstallsParameterChoicesMethodNoArgsFacetAndRemovesMethod() {
        final ActionChoicesFacetViaMethodFactory facetFactory = new ActionChoicesFacetViaMethodFactory();
        facetFactory.setSpecificationLookup(programmableReflector);

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(final int x, final Long y) {
            }

            @SuppressWarnings("unused")
            public Object[] choicesSomeAction() {
                return null;
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction", new Class[] { int.class, Long.class });
        final Method choicesMethod = findMethod(Customer.class, "choicesSomeAction", new Class[] {});
        programmableReflector.setLoadSpecificationStringReturn(voidSpec);

        facetFactory.process(new ProcessMethodContext(Customer.class, null, null, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(ActionChoicesFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof ActionChoicesFacetViaMethod);
        final ActionChoicesFacetViaMethod actionChoicesFacetViaMethod = (ActionChoicesFacetViaMethod) facet;
        assertEquals(choicesMethod, actionChoicesFacetViaMethod.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(choicesMethod));
    }

    public void testInstallsParameterChoicesMethodSomeArgsIsIgnored() {
        final ActionChoicesFacetViaMethodFactory facetFactory = new ActionChoicesFacetViaMethodFactory();
        facetFactory.setSpecificationLookup(programmableReflector);
        programmableReflector.setLoadSpecificationStringReturn(voidSpec);

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(final int x, final Long y) {
            }

            @SuppressWarnings("unused")
            public Object[] choicesSomeAction(final int x, final Long y) {
                return null;
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction", new Class[] { int.class, Long.class });

        facetFactory.process(new ProcessMethodContext(Customer.class, null, null, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(ActionChoicesFacet.class);
        assertNull(facet);
    }

    public static class CustomerStatic {
        public void someAction(final int x, final Long y) {
        }

        public static String nameSomeAction() {
            return "Another Name";
        }

        public static String descriptionSomeAction() {
            return "Some old description";
        }

        public static boolean alwaysHideSomeAction() {
            return true;
        }

        public static boolean protectSomeAction() {
            return true;
        }

        public static boolean hideSomeAction(final UserMemento userMemento) {
            return true;
        }

        public static String disableSomeAction(final UserMemento userMemento) {
            return "disabled for this user";
        }

        public static void otherAction(final int x, final Long y) {
        }

        public static boolean alwaysHideOtherAction() {
            return false;
        }

        public static boolean protectOtherAction() {
            return false;
        }
    }

    public void testInstallsNamedFacetUsingNameMethodAndRemovesMethod() {
        final NamedFacetStaticMethodFactory facetFactory = new NamedFacetStaticMethodFactory();
        facetFactory.setSpecificationLookup(programmableReflector);
        programmableReflector.setLoadSpecificationStringReturn(voidSpec);

        final Method actionMethod = findMethod(CustomerStatic.class, "someAction", new Class[] { int.class, Long.class });
        final Method nameMethod = findMethod(CustomerStatic.class, "nameSomeAction");

        facetFactory.process(new ProcessMethodContext(CustomerStatic.class, null, null, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(NamedFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof NamedFacetAbstract);
        final NamedFacetAbstract namedFacetAbstract = (NamedFacetAbstract) facet;
        assertEquals("Another Name", namedFacetAbstract.value());

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(nameMethod));
    }

    public void testInstallsDescribedAsFacetUsingDescriptionAndRemovesMethod() {
        final DescribedAsFacetStaticMethodFactory facetFactory = new DescribedAsFacetStaticMethodFactory();
        facetFactory.setSpecificationLookup(programmableReflector);
        programmableReflector.setLoadSpecificationStringReturn(voidSpec);

        final Method actionMethod = findMethod(CustomerStatic.class, "someAction", new Class[] { int.class, Long.class });
        final Method descriptionMethod = findMethod(CustomerStatic.class, "descriptionSomeAction");

        facetFactory.process(new ProcessMethodContext(CustomerStatic.class, null, null, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(DescribedAsFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof DescribedAsFacetAbstract);
        final DescribedAsFacetAbstract describedAsFacetAbstract = (DescribedAsFacetAbstract) facet;
        assertEquals("Some old description", describedAsFacetAbstract.value());

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(descriptionMethod));
    }

    public void testInstallsHiddenFacetUsingAlwaysHideAndRemovesMethod() {
        final HiddenFacetStaticMethodFactory facetFactory = new HiddenFacetStaticMethodFactory();
        facetFactory.setSpecificationLookup(programmableReflector);

        final Method actionMethod = findMethod(CustomerStatic.class, "someAction", new Class[] { int.class, Long.class });
        final Method alwaysHideMethod = findMethod(CustomerStatic.class, "alwaysHideSomeAction", new Class[] {});
        programmableReflector.setLoadSpecificationStringReturn(voidSpec);

        facetFactory.process(new ProcessMethodContext(CustomerStatic.class, null, null, actionMethod, methodRemover, facetedMethod));

        final HiddenFacetOnStaticMethod facet = facetedMethod.getFacet(HiddenFacetOnStaticMethod.class);
        assertNotNull(facet);
        assertEquals(When.ALWAYS, facet.when());

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(alwaysHideMethod));
    }

    public void testInstallsHiddenFacetUsingAlwaysHideWhenNotAndRemovesMethod() {
        final HiddenFacetStaticMethodFactory facetFactory = new HiddenFacetStaticMethodFactory();
        facetFactory.setSpecificationLookup(programmableReflector);
        programmableReflector.setLoadSpecificationStringReturn(voidSpec);

        final Method actionMethod = findMethod(CustomerStatic.class, "otherAction", new Class[] { int.class, Long.class });
        final Method alwaysHideMethod = findMethod(CustomerStatic.class, "alwaysHideOtherAction", new Class[] {});

        facetFactory.process(new ProcessMethodContext(CustomerStatic.class, null, null, actionMethod, methodRemover, facetedMethod));

        assertNull(facetedMethod.getFacet(HiddenFacetOnStaticMethod.class));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(alwaysHideMethod));
    }

    public void testInstallsDisabledFacetUsingProtectAndRemovesMethod() {
        final DisabledFacetStaticMethodFacetFactory facetFactory = new DisabledFacetStaticMethodFacetFactory();
        facetFactory.setSpecificationLookup(programmableReflector);
        programmableReflector.setLoadSpecificationStringReturn(voidSpec);

        final Method actionMethod = findMethod(CustomerStatic.class, "someAction", new Class[] { int.class, Long.class });
        final Method protectMethod = findMethod(CustomerStatic.class, "protectSomeAction", new Class[] {});

        facetFactory.process(new ProcessMethodContext(CustomerStatic.class, null, null, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(DisabledFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof DisabledFacetAbstract);
        final DisabledFacetAbstract disabledFacetAbstract = (DisabledFacetAbstract) facet;
        assertEquals(When.ALWAYS, disabledFacetAbstract.when());

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(protectMethod));
    }

    public void testDoesNotInstallDisabledFacetUsingProtectWhenNotAndRemovesMethod() {
        final DisabledFacetStaticMethodFacetFactory facetFactory = new DisabledFacetStaticMethodFacetFactory();
        facetFactory.setSpecificationLookup(programmableReflector);
        programmableReflector.setLoadSpecificationStringReturn(voidSpec);

        final Method actionMethod = findMethod(CustomerStatic.class, "otherAction", new Class[] { int.class, Long.class });
        final Method protectMethod = findMethod(CustomerStatic.class, "protectOtherAction", new Class[] {});

        facetFactory.process(new ProcessMethodContext(CustomerStatic.class, null, null, actionMethod, methodRemover, facetedMethod));

        assertNull(facetedMethod.getFacet(DisabledFacet.class));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(protectMethod));
    }

    public void testInstallsHiddenForSessionFacetAndRemovesMethod() {

        final HideForSessionFacetViaMethodFactory facetFactory = new HideForSessionFacetViaMethodFactory();
        facetFactory.setSpecificationLookup(programmableReflector);
        programmableReflector.setLoadSpecificationStringReturn(voidSpec);

        final Method actionMethod = findMethod(CustomerStatic.class, "someAction", new Class[] { int.class, Long.class });
        final Method hideMethod = findMethod(CustomerStatic.class, "hideSomeAction", new Class[] { UserMemento.class });

        facetFactory.process(new ProcessMethodContext(CustomerStatic.class, null, null, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(HideForSessionFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof HideForSessionFacetViaMethod);
        final HideForSessionFacetViaMethod hideForSessionFacetViaMethod = (HideForSessionFacetViaMethod) facet;
        assertEquals(hideMethod, hideForSessionFacetViaMethod.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(hideMethod));
    }

    public void testInstallsDisabledForSessionFacetAndRemovesMethod() {
        final DisableForSessionFacetViaMethodFactory facetFactory = new DisableForSessionFacetViaMethodFactory();
        facetFactory.setSpecificationLookup(programmableReflector);
        programmableReflector.setLoadSpecificationStringReturn(voidSpec);

        final Method actionMethod = findMethod(CustomerStatic.class, "someAction", new Class[] { int.class, Long.class });
        final Method disableMethod = findMethod(CustomerStatic.class, "disableSomeAction", new Class[] { UserMemento.class });

        facetFactory.process(new ProcessMethodContext(CustomerStatic.class, null, null, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(DisableForSessionFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof DisableForSessionFacetViaMethod);
        final DisableForSessionFacetViaMethod disableForSessionFacetViaMethod = (DisableForSessionFacetViaMethod) facet;
        assertEquals(disableMethod, disableForSessionFacetViaMethod.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(disableMethod));
    }

    public void testInstallsParameterDefaultsMethodAndRemovesMethod() {
        final ActionParameterDefaultsFacetViaMethodFactory facetFactory = new ActionParameterDefaultsFacetViaMethodFactory();
        facetFactory.setSpecificationLookup(programmableReflector);
        programmableReflector.setLoadSpecificationStringReturn(voidSpec);

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(final int x, final long y) {
            }

            @SuppressWarnings("unused")
            public int default0SomeAction() {
                return 0;
            }

            @SuppressWarnings("unused")
            public long default1SomeAction() {
                return 0;
            }
        }

        final Method actionMethod = findMethod(Customer.class, "someAction", new Class[] { int.class, long.class });
        final Method default0Method = findMethod(Customer.class, "default0SomeAction", new Class[] {});
        final Method default1Method = findMethod(Customer.class, "default1SomeAction", new Class[]{});

        final FacetedMethod facetHolderWithParms = FacetedMethod.createForAction(Customer.class, actionMethod);

        facetFactory.process(new ProcessMethodContext(Customer.class, null, null, actionMethod, methodRemover, facetHolderWithParms));

        final Facet facet0 = facetHolderWithParms.getParameters().get(0).getFacet(ActionParameterDefaultsFacet.class);
        assertNotNull(facet0);
        assertTrue(facet0 instanceof ActionParameterDefaultsFacetViaMethod);
        final ActionParameterDefaultsFacetViaMethod actionDefaultFacetViaMethod0 = (ActionParameterDefaultsFacetViaMethod) facet0;
        assertEquals(default0Method, actionDefaultFacetViaMethod0.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(default0Method));

        final Facet facet1 = facetHolderWithParms.getParameters().get(1).getFacet(ActionParameterDefaultsFacet.class);
        assertNotNull(facet1);
        assertTrue(facet1 instanceof ActionParameterDefaultsFacetViaMethod);
        final ActionParameterDefaultsFacetViaMethod actionDefaultFacetViaMethod1 = (ActionParameterDefaultsFacetViaMethod) facet1;
        assertEquals(default1Method, actionDefaultFacetViaMethod1.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(default1Method));

    }

    public void testInstallsParameterChoicesMethodAndRemovesMethod() {
        final ActionParameterChoicesFacetViaMethodFactory facetFactory = new ActionParameterChoicesFacetViaMethodFactory();
        facetFactory.setSpecificationLookup(programmableReflector);
        programmableReflector.setLoadSpecificationStringReturn(voidSpec);

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(final int x, final long y, final long z) {
            }

            @SuppressWarnings("unused")
            public Collection<Integer> choices0SomeAction() {
                return Collections.emptyList();
            }

            @SuppressWarnings("unused")
            public List<Long> choices1SomeAction() {
                return Collections.emptyList();
            }
            
            @SuppressWarnings("unused")
            public Set<Long> choices2SomeAction() {
                return Collections.emptySet();
            }
        }

        final Method actionMethod = findMethod(Customer.class, "someAction", new Class[] { int.class, long.class, long.class });
        final Method choices0Method = findMethod(Customer.class, "choices0SomeAction", new Class[] {});
        final Method choices1Method = findMethod(Customer.class, "choices1SomeAction", new Class[] {});
        final Method choices2Method = findMethod(Customer.class, "choices2SomeAction", new Class[] {});
        
        final FacetedMethod facetHolderWithParms = FacetedMethod.createForAction(Customer.class, actionMethod);

        facetFactory.process(new ProcessMethodContext(Customer.class, null, null, actionMethod, methodRemover, facetHolderWithParms));

        final Facet facet0 = facetHolderWithParms.getParameters().get(0).getFacet(ActionParameterChoicesFacet.class);
        assertNotNull(facet0);
        assertTrue(facet0 instanceof ActionParameterChoicesFacetViaMethod);
        final ActionParameterChoicesFacetViaMethod actionChoicesFacetViaMethod0 = (ActionParameterChoicesFacetViaMethod) facet0;
        assertEquals(choices0Method, actionChoicesFacetViaMethod0.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(choices0Method));

        final Facet facet1 = facetHolderWithParms.getParameters().get(1).getFacet(ActionParameterChoicesFacet.class);
        assertNotNull(facet1);
        assertTrue(facet1 instanceof ActionParameterChoicesFacetViaMethod);
        final ActionParameterChoicesFacetViaMethod actionChoicesFacetViaMethod1 = (ActionParameterChoicesFacetViaMethod) facet1;
        assertEquals(choices1Method, actionChoicesFacetViaMethod1.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(choices1Method));

        final Facet facet2 = facetHolderWithParms.getParameters().get(2).getFacet(ActionParameterChoicesFacet.class);
        assertNotNull(facet2);
        assertTrue(facet2 instanceof ActionParameterChoicesFacetViaMethod);
        final ActionParameterChoicesFacetViaMethod actionChoicesFacetViaMethod2 = (ActionParameterChoicesFacetViaMethod) facet2;
        assertEquals(choices2Method, actionChoicesFacetViaMethod2.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(choices2Method));


    }

    public void testInstallsParameterAutoCompleteMethodAndRemovesMethod() {
        final ActionParameterAutoCompleteFacetViaMethodFactory facetFactory = new ActionParameterAutoCompleteFacetViaMethodFactory();
        facetFactory.setSpecificationLookup(programmableReflector);
        programmableReflector.setLoadSpecificationStringReturn(voidSpec);

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(final int x, final long y) {
            }

            @SuppressWarnings("unused")
            public List<Integer> autoComplete0SomeAction(String searchArg) {
                return Collections.emptyList();
            }
        }

        final Method actionMethod = findMethod(Customer.class, "someAction", new Class[] { int.class, long.class });
        final Method autoComplete0Method = findMethod(Customer.class, "autoComplete0SomeAction", new Class[] {String.class});

        final FacetedMethod facetHolderWithParms = FacetedMethod.createForAction(Customer.class, actionMethod);

        facetFactory.process(new ProcessMethodContext(Customer.class, null, null, actionMethod, methodRemover, facetHolderWithParms));

        final Facet facet0 = facetHolderWithParms.getParameters().get(0).getFacet(ActionParameterAutoCompleteFacet.class);
        assertNotNull(facet0);
        assertTrue(facet0 instanceof ActionParameterAutoCompleteFacetViaMethod);
        final ActionParameterAutoCompleteFacetViaMethod actionAutoCompleteFacetViaMethod0 = (ActionParameterAutoCompleteFacetViaMethod) facet0;
        assertEquals(autoComplete0Method, actionAutoCompleteFacetViaMethod0.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(autoComplete0Method));
    }

    public void testBothChoicesMethodCausesException() {

        final ActionChoicesFacetViaMethodFactory facetFactory = new ActionChoicesFacetViaMethodFactory();
        facetFactory.setSpecificationLookup(programmableReflector);
        programmableReflector.setLoadSpecificationStringReturn(voidSpec);

        final ActionParameterChoicesFacetViaMethodFactory facetFactoryForParams = new ActionParameterChoicesFacetViaMethodFactory();
        facetFactoryForParams.setSpecificationLookup(programmableReflector);
        programmableReflector.setLoadSpecificationStringReturn(voidSpec);

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(final int x, final long y) {
            }

            @SuppressWarnings("unused")
            public int[] choices0SomeAction() {
                return new int[0];
            }

            @SuppressWarnings("unused")
            public long[] choices1SomeAction() {
                return new long[0];
            }

            @SuppressWarnings("unused")
            public Object[][] choicesSomeAction() {
                return null;
            }
        }

        final Method actionMethod = findMethod(Customer.class, "someAction", new Class[] { int.class, long.class });
        final FacetedMethod facetHolderWithParms = FacetedMethod.createForAction(Customer.class, actionMethod);

        final ProcessMethodContext processMethodContext = new ProcessMethodContext(Customer.class, null, null, actionMethod, methodRemover, facetHolderWithParms);
        facetFactory.process(processMethodContext);
        try {
            facetFactoryForParams.process(processMethodContext);
            fail("exception expected");
        } catch (final org.apache.isis.core.metamodel.exceptions.MetaModelException expected) {
            // ignore
        }
    }

    public void testBothDefaultMethodCausesException() {
        final ActionDefaultsFacetViaMethodFactory facetFactory = new ActionDefaultsFacetViaMethodFactory();
        facetFactory.setSpecificationLookup(programmableReflector);
        programmableReflector.setLoadSpecificationStringReturn(voidSpec);

        final ActionParameterDefaultsFacetViaMethodFactory facetFactoryForParams = new ActionParameterDefaultsFacetViaMethodFactory();
        facetFactoryForParams.setSpecificationLookup(programmableReflector);
        programmableReflector.setLoadSpecificationStringReturn(voidSpec);

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(final int x, final long y) {
            }

            @SuppressWarnings("unused")
            public int default0SomeAction() {
                return 0;
            }

            @SuppressWarnings("unused")
            public long default1SomeAction() {
                return 0;
            }

            @SuppressWarnings("unused")
            public Object[] defaultSomeAction() {
                return null;
            }
        }

        final Method actionMethod = findMethod(Customer.class, "someAction", new Class[] { int.class, long.class });
        final FacetedMethod facetHolderWithParms = FacetedMethod.createForAction(Customer.class, actionMethod);

        final ProcessMethodContext processMethodContext = new ProcessMethodContext(Customer.class, null, null, actionMethod, methodRemover, facetHolderWithParms);
        facetFactory.process(processMethodContext);
        try {
            facetFactoryForParams.process(processMethodContext);
            fail("exception expected");
        } catch (final org.apache.isis.core.metamodel.exceptions.MetaModelException expected) {

        }
    }

}
