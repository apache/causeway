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
package org.apache.isis.core.metamodel.facets.properties.property;

import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Pattern;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.apache.isis.applib.annotation.MementoSerialization;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.events.domain.PropertyDomainEvent;
import org.apache.isis.applib.spec.Specification;
import org.apache.isis.core.metamodel.commons.matchers.IsisMatchers;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryJUnit4TestCase;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacet;
import org.apache.isis.core.metamodel.facets.object.domainobject.domainevents.PropertyDomainEventDefaultFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.objectvalue.mandatory.MandatoryFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.maxlen.MaxLengthFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.mustsatisfyspec.MustSatisfySpecificationFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.regex.RegExFacet;
import org.apache.isis.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacetAbstract;
import org.apache.isis.core.metamodel.facets.propcoll.notpersisted.NotPersistedFacet;
import org.apache.isis.core.metamodel.facets.properties.property.disabled.DisabledFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.hidden.HiddenFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.mandatory.MandatoryFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.maxlength.MaxLengthFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyClearFacetForDomainEventFromDefault;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyClearFacetForDomainEventFromPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyDomainEventFacet;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyDomainEventFacetDefault;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyDomainEventFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertySetterFacetForDomainEventFromDefault;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertySetterFacetForDomainEventFromPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.mustsatisfy.MustSatisfySpecificationFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.notpersisted.NotPersistedFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.regex.RegExFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.update.clear.PropertyClearFacet;
import org.apache.isis.core.metamodel.facets.properties.update.clear.PropertyClearFacetAbstract;
import org.apache.isis.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.isis.core.metamodel.facets.properties.update.modify.PropertySetterFacetAbstract;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.unittestsupport.config._Config;

import lombok.val;

public class PropertyAnnotationFacetFactoryTest extends AbstractFacetFactoryJUnit4TestCase {

    PropertyAnnotationFacetFactory facetFactory;
    Method propertyMethod;

    @Mock
    ObjectSpecification mockTypeSpec;
    @Mock ObjectSpecification mockReturnTypeSpec;

    void expectRemoveMethod(final Method actionMethod) {
        context.checking(new Expectations() {{
            oneOf(mockMethodRemover).removeMethod(actionMethod);
        }});
    }

    void allowingLoadSpecificationRequestsFor(final Class<?> cls, final Class<?> returnType) {
        context.checking(new Expectations() {{
            allowing(mockSpecificationLoader).loadSpecification(cls);
            will(returnValue(mockTypeSpec));

            allowing(mockSpecificationLoader).loadSpecification(returnType);
            will(returnValue(mockReturnTypeSpec));

            allowing(mockTypeSpec).getFacet(PropertyDomainEventDefaultFacetForDomainObjectAnnotation.class);
            will(returnValue(null));

        }});
    }
    
    private static void processModify(
            PropertyAnnotationFacetFactory facetFactory, FacetFactory.ProcessMethodContext processMethodContext) {
        val propertyIfAny = processMethodContext.synthesizeOnMethod(Property.class);
        facetFactory.processModify(processMethodContext, propertyIfAny);
    }
    
    private static void processHidden(
            PropertyAnnotationFacetFactory facetFactory, FacetFactory.ProcessMethodContext processMethodContext) {
        val propertyIfAny = processMethodContext.synthesizeOnMethod(Property.class);
        facetFactory.processHidden(processMethodContext, propertyIfAny);
    }
    
    private static void processOptional(
            PropertyAnnotationFacetFactory facetFactory, FacetFactory.ProcessMethodContext processMethodContext) {
        val propertyIfAny = processMethodContext.synthesizeOnMethod(Property.class);
        facetFactory.processOptional(processMethodContext, propertyIfAny);
    }
    
    private static void processRegEx(
            PropertyAnnotationFacetFactory facetFactory, FacetFactory.ProcessMethodContext processMethodContext) {
        val propertyIfAny = processMethodContext.synthesizeOnMethod(Property.class);
        facetFactory.processRegEx(processMethodContext, propertyIfAny);
    }
    
    private static void processEditing(
            PropertyAnnotationFacetFactory facetFactory, FacetFactory.ProcessMethodContext processMethodContext) {
        val propertyIfAny = processMethodContext.synthesizeOnMethod(Property.class);
        facetFactory.processEditing(processMethodContext, propertyIfAny);
    }
    
    private static void processMaxLength(
            PropertyAnnotationFacetFactory facetFactory, FacetFactory.ProcessMethodContext processMethodContext) {
        val propertyIfAny = processMethodContext.synthesizeOnMethod(Property.class);
        facetFactory.processMaxLength(processMethodContext, propertyIfAny);
    }
    
    private static void processMustSatisfy(
            PropertyAnnotationFacetFactory facetFactory, FacetFactory.ProcessMethodContext processMethodContext) {
        val propertyIfAny = processMethodContext.synthesizeOnMethod(Property.class);
        facetFactory.processMustSatisfy(processMethodContext, propertyIfAny);
    }
    
    private static void processNotPersisted(
            PropertyAnnotationFacetFactory facetFactory, FacetFactory.ProcessMethodContext processMethodContext) {
        val propertyIfAny = processMethodContext.synthesizeOnMethod(Property.class);
        facetFactory.processNotPersisted(processMethodContext, propertyIfAny);
    }
    
    

    @Before
    public void setUp() throws Exception {
        _Config.clear();
        facetFactory = new PropertyAnnotationFacetFactory();
        facetFactory.setMetaModelContext(super.metaModelContext);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        facetFactory = null;
    }

    public static class Modify extends PropertyAnnotationFacetFactoryTest {

        private void addGetterFacet(final FacetHolder holder) {
            FacetUtil.addFacet(new PropertyOrCollectionAccessorFacetAbstract(mockOnType, holder ) {
                @Override
                public Object getProperty(
                        final ManagedObject inObject,
                        final InteractionInitiatedBy interactionInitiatedBy) {
                    return null;
                }
            });
        }

        private void addSetterFacet(final FacetHolder holder) {
            FacetUtil.addFacet(new PropertySetterFacetAbstract(holder) {
                @Override
                public void setProperty(
                        final OneToOneAssociation owningAssociation,
                        final ManagedObject inObject,
                        final ManagedObject value,
                        final InteractionInitiatedBy interactionInitiatedBy) {
                }
            });
        }

        private void addClearFacet(final FacetHolder holder) {
            FacetUtil.addFacet(new PropertyClearFacetAbstract(holder) {
                @Override
                public void clearProperty(
                        final OneToOneAssociation owningProperty, 
                        final ManagedObject targetAdapter,
                        final InteractionInitiatedBy interactionInitiatedBy) {
                }
            });
        }


        @Test
        public void withDeprecatedPostsPropertyChangedEvent_andGetterFacet_andSetterFacet() {

            class Customer {
                class NamedChangedDomainEvent extends PropertyDomainEvent<Customer, String> {
                }
                @Property(domainEvent = NamedChangedDomainEvent.class)
                public String getName() {
                    return null;
                }
                public void setName(final String name) {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            addGetterFacet(facetedMethod);
            addSetterFacet(facetedMethod);
            addClearFacet(facetedMethod);

            // expect
            allowingLoadSpecificationRequestsFor(cls, propertyMethod.getReturnType());
            context.checking(new Expectations() {{
                //[ahuber] never called during this test ...             	
                //oneOf(mockConfiguration).getBoolean("isis.core.meta-model.annotation.property.domain-event.post-for-default", true);
                //will(returnValue(true));

                allowing(mockTypeSpec).getFacet(PropertyDomainEventDefaultFacetForDomainObjectAnnotation.class);
                will(returnValue(null));
            }});

            // when
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls, null,
                    propertyMethod, mockMethodRemover, facetedMethod);
            processModify(facetFactory, processMethodContext);

            // then
            final PropertyDomainEventFacet domainEventFacet = facetedMethod.getFacet(PropertyDomainEventFacet.class);
            Assert.assertNotNull(domainEventFacet);
            Assert.assertTrue(domainEventFacet instanceof PropertyDomainEventFacetForPropertyAnnotation);
            final PropertyDomainEventFacetForPropertyAnnotation domainEventFacetDefault = (PropertyDomainEventFacetForPropertyAnnotation) domainEventFacet;
            assertThat(domainEventFacetDefault.getEventType(), IsisMatchers.classEqualTo(Customer.NamedChangedDomainEvent.class));

            // then
            final Facet setterFacet = facetedMethod.getFacet(PropertySetterFacet.class);
            Assert.assertNotNull(setterFacet);
            Assert.assertTrue(setterFacet instanceof PropertySetterFacetForDomainEventFromPropertyAnnotation);
            final PropertySetterFacetForDomainEventFromPropertyAnnotation setterFacetImpl = (PropertySetterFacetForDomainEventFromPropertyAnnotation) setterFacet;
            assertThat(setterFacetImpl.value(), IsisMatchers.classEqualTo(Customer.NamedChangedDomainEvent.class));

            // then
            final Facet clearFacet = facetedMethod.getFacet(PropertyClearFacet.class);
            Assert.assertNotNull(clearFacet);
            Assert.assertTrue(clearFacet instanceof PropertyClearFacetForDomainEventFromPropertyAnnotation);
            final PropertyClearFacetForDomainEventFromPropertyAnnotation clearFacetImpl = (PropertyClearFacetForDomainEventFromPropertyAnnotation) clearFacet;
            assertThat(clearFacetImpl.value(), IsisMatchers.classEqualTo(Customer.NamedChangedDomainEvent.class));
        }


        @Test
        public void withPropertyInteractionEvent() {

            class Customer {
                class NamedChangedDomainEvent extends PropertyDomainEvent<Customer, String> {
                }
                @Property(domainEvent = NamedChangedDomainEvent.class)
                public String getName() {
                    return null;
                }
                public void setName(final String name) {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            addGetterFacet(facetedMethod);
            addSetterFacet(facetedMethod);
            addClearFacet(facetedMethod);

            // expect
            allowingLoadSpecificationRequestsFor(cls, propertyMethod.getReturnType());

            context.checking(new Expectations() {{
                allowing(mockTypeSpec).getFacet(PropertyDomainEventDefaultFacetForDomainObjectAnnotation.class);
                will(returnValue(null));
            }});

            // when
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls, null,
                    propertyMethod, mockMethodRemover, facetedMethod);
            processModify(facetFactory, processMethodContext);

            // then
            final Facet domainEventFacet = facetedMethod.getFacet(PropertyDomainEventFacet.class);
            Assert.assertNotNull(domainEventFacet);
            Assert.assertTrue(domainEventFacet instanceof PropertyDomainEventFacetForPropertyAnnotation);
            final PropertyDomainEventFacetForPropertyAnnotation domainEventFacetImpl = (PropertyDomainEventFacetForPropertyAnnotation) domainEventFacet;
            assertThat(domainEventFacetImpl.value(), IsisMatchers.classEqualTo(Customer.NamedChangedDomainEvent.class));

            // then
            final Facet setterFacet = facetedMethod.getFacet(PropertySetterFacet.class);
            Assert.assertNotNull(setterFacet);
            Assert.assertTrue(setterFacet instanceof PropertySetterFacetForDomainEventFromPropertyAnnotation);
            final PropertySetterFacetForDomainEventFromPropertyAnnotation setterFacetImpl = (PropertySetterFacetForDomainEventFromPropertyAnnotation) setterFacet;
            assertThat(setterFacetImpl.value(), IsisMatchers.classEqualTo(Customer.NamedChangedDomainEvent.class));

            // then
            final Facet clearFacet = facetedMethod.getFacet(PropertyClearFacet.class);
            Assert.assertNotNull(clearFacet);
            Assert.assertTrue(clearFacet instanceof PropertyClearFacetForDomainEventFromPropertyAnnotation);
            final PropertyClearFacetForDomainEventFromPropertyAnnotation clearFacetImpl = (PropertyClearFacetForDomainEventFromPropertyAnnotation) clearFacet;
            assertThat(clearFacetImpl.value(), IsisMatchers.classEqualTo(Customer.NamedChangedDomainEvent.class));
        }

        @Test
        public void withPropertyDomainEvent() {

            class Customer {
                class NamedChangedDomainEvent extends PropertyDomainEvent<Customer, String> {
                }
                @Property(domainEvent= NamedChangedDomainEvent.class)
                public String getName() {
                    return null;
                }
                public void setName(final String name) {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            addGetterFacet(facetedMethod);
            addSetterFacet(facetedMethod);
            addClearFacet(facetedMethod);

            // expect
            allowingLoadSpecificationRequestsFor(cls, propertyMethod.getReturnType());

            context.checking(new Expectations() {{
                allowing(mockTypeSpec).getFacet(PropertyDomainEventDefaultFacetForDomainObjectAnnotation.class);
                will(returnValue(null));
            }});
            // when
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls, null,
                    propertyMethod, mockMethodRemover, facetedMethod);
            processModify(facetFactory, processMethodContext);

            // then
            final Facet domainEventFacet = facetedMethod.getFacet(PropertyDomainEventFacet.class);
            Assert.assertNotNull(domainEventFacet);
            Assert.assertTrue(domainEventFacet instanceof PropertyDomainEventFacetForPropertyAnnotation);
            final PropertyDomainEventFacetForPropertyAnnotation domainEventFacetImpl = (PropertyDomainEventFacetForPropertyAnnotation) domainEventFacet;
            assertThat(domainEventFacetImpl.value(), IsisMatchers.classEqualTo(Customer.NamedChangedDomainEvent.class));

            // then
            final Facet setterFacet = facetedMethod.getFacet(PropertySetterFacet.class);
            Assert.assertNotNull(setterFacet);
            Assert.assertTrue(setterFacet instanceof PropertySetterFacetForDomainEventFromPropertyAnnotation);
            final PropertySetterFacetForDomainEventFromPropertyAnnotation setterFacetImpl = (PropertySetterFacetForDomainEventFromPropertyAnnotation) setterFacet;
            assertThat(setterFacetImpl.value(), IsisMatchers.classEqualTo(Customer.NamedChangedDomainEvent.class));

            // then
            final Facet clearFacet = facetedMethod.getFacet(PropertyClearFacet.class);
            Assert.assertNotNull(clearFacet);
            Assert.assertTrue(clearFacet instanceof PropertyClearFacetForDomainEventFromPropertyAnnotation);
            final PropertyClearFacetForDomainEventFromPropertyAnnotation clearFacetImpl = (PropertyClearFacetForDomainEventFromPropertyAnnotation) clearFacet;
            assertThat(clearFacetImpl.value(), IsisMatchers.classEqualTo(Customer.NamedChangedDomainEvent.class));
        }

        @Test
        public void withDefaultEvent() {

            class Customer {
                public String getName() {
                    return null;
                }
                public void setName(final String name) {
                }
            }

            // given
            _Config.put("isis.core.meta-model.annotation.property.domain-event.post-for-default", true);

            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            addGetterFacet(facetedMethod);
            addSetterFacet(facetedMethod);
            addClearFacet(facetedMethod);

            // expect
            allowingLoadSpecificationRequestsFor(cls, propertyMethod.getReturnType());

            // when
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls, null,
                    propertyMethod, mockMethodRemover, facetedMethod);
            processModify(facetFactory, processMethodContext);

            // then
            final Facet domainEventFacet = facetedMethod.getFacet(PropertyDomainEventFacet.class);
            Assert.assertNotNull(domainEventFacet);
            Assert.assertTrue(domainEventFacet instanceof PropertyDomainEventFacetDefault);
            final PropertyDomainEventFacetDefault domainEventFacetImpl = (PropertyDomainEventFacetDefault) domainEventFacet;
            assertThat(domainEventFacetImpl.value(), IsisMatchers.classEqualTo(PropertyDomainEvent.Default.class));

            // then
            final Facet setterFacet = facetedMethod.getFacet(PropertySetterFacet.class);
            Assert.assertNotNull(setterFacet);
            Assert.assertTrue(setterFacet instanceof PropertySetterFacetForDomainEventFromDefault);
            final PropertySetterFacetForDomainEventFromDefault setterFacetImpl = (PropertySetterFacetForDomainEventFromDefault) setterFacet;
            assertThat(setterFacetImpl.value(), IsisMatchers.classEqualTo(PropertyDomainEvent.Default.class));

            // then
            final Facet clearFacet = facetedMethod.getFacet(PropertyClearFacet.class);
            Assert.assertNotNull(clearFacet);
            Assert.assertTrue(clearFacet instanceof PropertyClearFacetForDomainEventFromDefault);
            final PropertyClearFacetForDomainEventFromDefault clearFacetImpl = (PropertyClearFacetForDomainEventFromDefault) clearFacet;
            assertThat(clearFacetImpl.value(), IsisMatchers.classEqualTo(PropertyDomainEvent.Default.class));
        }
    }

    public static class Hidden extends PropertyAnnotationFacetFactoryTest {

        @Test
        public void withAnnotation() {

            class Customer {
                @Property(hidden = Where.REFERENCES_PARENT)
                public String getName() {
                    return null;
                }
                public void setName(final String name) {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            // when
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls, null,
                    propertyMethod, mockMethodRemover, facetedMethod);
            processHidden(facetFactory, processMethodContext);

            // then
            final HiddenFacet hiddenFacet = facetedMethod.getFacet(HiddenFacet.class);
            Assert.assertNotNull(hiddenFacet);
            Assert.assertTrue(hiddenFacet instanceof HiddenFacetForPropertyAnnotation);
            final HiddenFacetForPropertyAnnotation hiddenFacetImpl = (HiddenFacetForPropertyAnnotation) hiddenFacet;
            assertThat(hiddenFacetImpl.where(), is(Where.REFERENCES_PARENT));

            final Facet hiddenFacetForProp = facetedMethod.getFacet(HiddenFacetForPropertyAnnotation.class);
            Assert.assertNotNull(hiddenFacetForProp);
            Assert.assertTrue(hiddenFacet == hiddenFacetForProp);
        }

    }

    public static class Editing extends PropertyAnnotationFacetFactoryTest {

        @Test
        public void withAnnotation() {

            class Customer {
                @Property(
                        editing = org.apache.isis.applib.annotation.Editing.DISABLED,
                        editingDisabledReason = "you cannot edit the name property"
                        )
                public String getName() {
                    return null;
                }
                public void setName(final String name) {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            // when
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls, null,
                    propertyMethod, mockMethodRemover, facetedMethod);
            processEditing(facetFactory, processMethodContext);

            // then
            final DisabledFacet disabledFacet = facetedMethod.getFacet(DisabledFacet.class);
            Assert.assertNotNull(disabledFacet);
            Assert.assertTrue(disabledFacet instanceof DisabledFacetForPropertyAnnotation);
            final DisabledFacetForPropertyAnnotation disabledFacetImpl = (DisabledFacetForPropertyAnnotation) disabledFacet;
            assertThat(disabledFacet.where(), is(Where.EVERYWHERE));
            assertThat(disabledFacetImpl.getReason(), is("you cannot edit the name property"));
        }
    }

    public static class MaxLength extends PropertyAnnotationFacetFactoryTest {

        @Test
        public void withAnnotation() {

            class Customer {
                @Property(
                        maxLength = 30
                        )
                public String getName() {
                    return null;
                }
                public void setName(final String name) {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            // when
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls, null,
                    propertyMethod, mockMethodRemover, facetedMethod);
            processMaxLength(facetFactory, processMethodContext);

            // then
            final MaxLengthFacet maxLengthFacet = facetedMethod.getFacet(MaxLengthFacet.class);
            Assert.assertNotNull(maxLengthFacet);
            Assert.assertTrue(maxLengthFacet instanceof MaxLengthFacetForPropertyAnnotation);
            assertThat(maxLengthFacet.value(), is(30));
        }
    }

    public static class MustSatisfy extends PropertyAnnotationFacetFactoryTest {

        public static class NotTooHot implements Specification {
            @Override
            public String satisfies(final Object obj) {
                return null;
            }
        }

        public static class NotTooCold implements Specification {
            @Override
            public String satisfies(final Object obj) {
                return null;
            }
        }


        @Test
        public void withAnnotation() {

            class Customer {
                @Property(
                        mustSatisfy = {NotTooHot.class, NotTooCold.class}
                        )
                public String getName() {
                    return null;
                }
                public void setName(final String name) {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            // expecting
            context.ignoring(mockServiceInjector);


            // when
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls, null,
                    propertyMethod, mockMethodRemover, facetedMethod);
            processMustSatisfy(facetFactory, processMethodContext);

            // then
            final MustSatisfySpecificationFacet mustSatisfySpecificationFacet = facetedMethod.getFacet(MustSatisfySpecificationFacet.class);
            Assert.assertNotNull(mustSatisfySpecificationFacet);
            Assert.assertTrue(mustSatisfySpecificationFacet instanceof MustSatisfySpecificationFacetForPropertyAnnotation);
            final MustSatisfySpecificationFacetForPropertyAnnotation mustSatisfySpecificationFacetImpl = (MustSatisfySpecificationFacetForPropertyAnnotation) mustSatisfySpecificationFacet;
            final List<Specification> specifications = mustSatisfySpecificationFacetImpl.getSpecifications();
            assertThat(specifications.size(), is(2));

            assertTrue(specifications.get(0) instanceof NotTooHot);
            assertTrue(specifications.get(1) instanceof NotTooCold);
        }

    }

    public static class NotPersisted extends PropertyAnnotationFacetFactoryTest {

        @Test
        public void withAnnotation() {

            class Customer {
                @Property(mementoSerialization = MementoSerialization.EXCLUDED)
                public String getName() {
                    return null;
                }
                public void setName(final String name) {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            // when
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls, null,
                    propertyMethod, mockMethodRemover, facetedMethod);
            processNotPersisted(facetFactory, processMethodContext);

            // then
            final NotPersistedFacet notPersistedFacet = facetedMethod.getFacet(NotPersistedFacet.class);
            Assert.assertNotNull(notPersistedFacet);
            Assert.assertTrue(notPersistedFacet instanceof NotPersistedFacetForPropertyAnnotation);
        }

    }

    public static class Mandatory extends PropertyAnnotationFacetFactoryTest {

        @Test
        public void whenOptionalityIsTrue() {

            class Customer {
                @Property(
                        optionality = Optionality.OPTIONAL
                        )
                public String getName() {
                    return null;
                }
                public void setName(final String name) {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            // when
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls, null,
                    propertyMethod, mockMethodRemover, facetedMethod);
            processOptional(facetFactory, processMethodContext);

            // then
            final MandatoryFacet mandatoryFacet = facetedMethod.getFacet(MandatoryFacet.class);
            Assert.assertNotNull(mandatoryFacet);
            Assert.assertTrue(mandatoryFacet instanceof MandatoryFacetForPropertyAnnotation.Optional);
        }

        @Test
        public void whenOptionalityIsFalse() {

            class Customer {
                @Property(
                        optionality = Optionality.MANDATORY
                        )
                public String getName() {
                    return null;
                }
                public void setName(final String name) {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            // when
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls, null,
                    propertyMethod, mockMethodRemover, facetedMethod);
            processOptional(facetFactory, processMethodContext);

            // then
            final MandatoryFacet mandatoryFacet = facetedMethod.getFacet(MandatoryFacet.class);
            Assert.assertNotNull(mandatoryFacet);
            Assert.assertTrue(mandatoryFacet instanceof MandatoryFacetForPropertyAnnotation.Required);
        }

        @Test
        public void whenOptionalityIsDefault() {

            class Customer {
                @Property(
                        optionality = Optionality.DEFAULT
                        )
                public String getName() {
                    return null;
                }
                public void setName(final String name) {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            // when
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls, null,
                    propertyMethod, mockMethodRemover, facetedMethod);
            processOptional(facetFactory, processMethodContext);

            // then
            final MandatoryFacet mandatoryFacet = facetedMethod.getFacet(MandatoryFacet.class);
            Assert.assertNull(mandatoryFacet);
        }

        @Test
        public void whenNone() {

            class Customer {
                @Property(
                        )
                public String getName() {
                    return null;
                }
                public void setName(final String name) {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            // when
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls, null,
                    propertyMethod, mockMethodRemover, facetedMethod);
            processOptional(facetFactory, processMethodContext);

            // then
            final MandatoryFacet mandatoryFacet = facetedMethod.getFacet(MandatoryFacet.class);
            Assert.assertNull(mandatoryFacet);
        }

    }
    public static class RegEx extends PropertyAnnotationFacetFactoryTest {

        @Test
        public void whenHasAnnotation() {

            class Customer {
                @Property(
                        regexPattern = "[123].*",
                        regexPatternFlags = Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
                        )
                public String getName() {
                    return null;
                }
                public void setName(final String name) {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            // when
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls, null,
                    propertyMethod, mockMethodRemover, facetedMethod);
            processRegEx(facetFactory, processMethodContext);

            // then
            final RegExFacet regExFacet = facetedMethod.getFacet(RegExFacet.class);
            Assert.assertNotNull(regExFacet);
            Assert.assertTrue(regExFacet instanceof RegExFacetForPropertyAnnotation);
            assertThat(regExFacet.patternFlags(), is(10));
            assertThat(regExFacet.regexp(), is("[123].*"));
        }

        @Test
        public void whenNone() {

            class Customer {
                @Property(
                        )
                public String getName() {
                    return null;
                }
                public void setName(final String name) {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            // when
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls, null,
                    propertyMethod, mockMethodRemover, facetedMethod);
            processRegEx(facetFactory, processMethodContext);

            // then
            final RegExFacet regExFacet = facetedMethod.getFacet(RegExFacet.class);
            Assert.assertNull(regExFacet);
        }

        @Test
        public void whenEmptyString() {

            class Customer {
                @Property(
                        regexPattern = ""
                        )
                public String getName() {
                    return null;
                }
                public void setName(final String name) {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            // when
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls, null,
                    propertyMethod, mockMethodRemover, facetedMethod);
            processRegEx(facetFactory, processMethodContext);

            // then
            final RegExFacet regExFacet = facetedMethod.getFacet(RegExFacet.class);
            Assert.assertNull(regExFacet);
        }

        @Test
        public void whenNotAnnotatedOnStringProperty() {

            class Customer {
                @Property(
                        regexPattern = "[abc].*"
                        )
                public int getName() {
                    return 0;
                }
                public void setName(final int name) {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            // when
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls, null,
                    propertyMethod, mockMethodRemover, facetedMethod);
            processRegEx(facetFactory, processMethodContext);

            // then
            final RegExFacet regExFacet = facetedMethod.getFacet(RegExFacet.class);
            Assert.assertNull(regExFacet);
        }

    }

}