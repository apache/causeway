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
package org.apache.isis.core.metamodel.services.appfeat;

import java.util.List;

import org.hamcrest.Matchers;
import org.jmock.Expectations;
import org.jmock.Sequence;
import org.jmock.auto.Mock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;

import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.appfeat.ApplicationFeature;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.members.hidden.HiddenFacetAbstract;
import org.apache.isis.core.metamodel.facets.objectvalue.maxlen.MaxLengthFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.maxlen.MaxLengthFacetAbstract;
import org.apache.isis.core.metamodel.facets.objectvalue.typicallen.TypicalLengthFacet;
import org.apache.isis.core.metamodel.facets.properties.typicallen.annotation.TypicalLengthFacetOnPropertyAnnotation;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

public class ApplicationFeatureRepositoryDefaultTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    @Mock ObjectSpecification mockSpec;
    @Mock OneToOneAssociation mockProp;
    @Mock OneToManyAssociation mockColl;
    @Mock ObjectAction mockAct;
    
    ObjectAction mockActThatIsHidden;

    @Mock FactoryService mockFactoryService;
    @Mock ServiceRegistry mockServiceRegistry;
    @Mock SpecificationLoader mockSpecificationLoader;

    protected ApplicationFeatureRepositoryDefault applicationFeatureRepository;
    
    @Before
    public void setUp() throws Exception {

        final ApplicationFeatureFactory applicationFeatureFactory = new ApplicationFeatureFactory();

        applicationFeatureRepository = new ApplicationFeatureRepositoryDefault(
                /*configuration*/ null, 
                mockSpecificationLoader, 
                applicationFeatureFactory);

        mockActThatIsHidden = context.mock(ObjectAction.class, "mockActThatIsHidden");
    }

    public static class Load extends ApplicationFeatureRepositoryDefaultTest {

        public static class Bar {}
        
        private static ApplicationFeature newApplicationFeature(ApplicationFeatureId featId) {
            return new ApplicationFeatureDefault(featId);
        }

        @Ignore // considering deleting this test, it's too long and too fragile.  integ tests ought to suffice.
        @Test
        public void happyCase() throws Exception {

            final List<ObjectAssociation> properties = _Lists.<ObjectAssociation>of(mockProp);
            final List<ObjectAssociation> collections = _Lists.<ObjectAssociation>of(mockColl);
            final List<ObjectAction> actions = _Lists.of(mockAct, mockActThatIsHidden);

            context.checking(new Expectations() {{
                allowing(mockSpec).isAbstract();
                will(returnValue(false));

                allowing(mockSpec).getFullIdentifier();
                will(returnValue(Bar.class.getName()));

                allowing(mockSpec).streamDeclaredAssociations(with(MixedIn.INCLUDED));//, with(ObjectAssociation.Predicates.PROPERTIES));
                will(returnValue(properties.stream().filter(ObjectAssociation.Predicates.PROPERTIES)));

                allowing(mockSpec).streamDeclaredAssociations(with(MixedIn.INCLUDED));//, with(ObjectAssociation.Predicates.COLLECTIONS));
                will(returnValue(collections.stream().filter(ObjectAssociation.Predicates.COLLECTIONS)));

                allowing(mockSpec).getFacet(HiddenFacet.class);
                will(returnValue(new HiddenFacetAbstract(Where.EVERYWHERE, mockSpec) {
                    @Override
                    protected String hiddenReason(final ManagedObject target, final Where whereContext) {
                        return null;
                    }
                }));

                allowing(mockSpec).getCorrespondingClass();
                will(returnValue(Bar.class));

                allowing(mockSpec).streamDeclaredActions(with(MixedIn.INCLUDED));
                will(returnValue(actions.stream()));
                
                allowing(mockSpec).streamActions(with(MixedIn.INCLUDED));
                will(returnValue(actions.stream()));

                allowing(mockProp).getId();
                will(returnValue("someProperty"));

                allowing(mockProp).getFacet(MaxLengthFacet.class);
                will(returnValue(new MaxLengthFacetAbstract(30, mockProp){}));

                allowing(mockProp).getFacet(TypicalLengthFacet.class);
                will(returnValue(new TypicalLengthFacetOnPropertyAnnotation(15, mockProp)));

                allowing(mockProp).isAlwaysHidden();
                will(returnValue(false));

                allowing(mockColl).getId();
                will(returnValue("someCollection"));

                allowing(mockColl).isAlwaysHidden();
                will(returnValue(false));

                allowing(mockAct).getId();
                will(returnValue("someAction"));

                allowing(mockAct).isAlwaysHidden();
                will(returnValue(false));

                allowing(mockAct).getSemantics();
                will(returnValue(SemanticsOf.SAFE));

                allowing(mockActThatIsHidden).getId();
                will(returnValue("someActionThatIsHidden"));

                allowing(mockActThatIsHidden).isAlwaysHidden();
                will(returnValue(true));

                allowing(mockActThatIsHidden).getSemantics();
                will(returnValue(SemanticsOf.SAFE));

                //                allowing(mockServiceRegistry).streamServices();
                //                will(returnValue(_Lists.newArrayList().stream()));
            }});

            // then
            final Sequence sequence = context.sequence("loadSequence");
            context.checking(new Expectations() {{
                oneOf(mockFactoryService).create(ApplicationFeature.class);
                inSequence(sequence);
                will(returnValue(newApplicationFeature(ApplicationFeatureId.newType(Bar.class.getName()))));

                oneOf(mockFactoryService).create(ApplicationFeature.class);
                inSequence(sequence);
                will(returnValue(newApplicationFeature(ApplicationFeatureId.newMember(Bar.class.getName(), "someProperty"))));

                oneOf(mockFactoryService).create(ApplicationFeature.class);
                inSequence(sequence);
                will(returnValue(newApplicationFeature(ApplicationFeatureId.newMember(Bar.class.getName(), "someCollection"))));

                oneOf(mockFactoryService).create(ApplicationFeature.class);
                inSequence(sequence);
                will(returnValue(newApplicationFeature(ApplicationFeatureId.newMember(Bar.class.getName(), "someAction"))));

                oneOf(mockFactoryService).create(ApplicationFeature.class);
                inSequence(sequence);
                will(returnValue(newApplicationFeature(ApplicationFeatureId.newNamespace("org.isisaddons.module.security.dom.feature"))));

                oneOf(mockFactoryService).create(ApplicationFeature.class);
                inSequence(sequence);
                will(returnValue(newApplicationFeature(ApplicationFeatureId.newNamespace("org.isisaddons.module.security.dom"))));

                oneOf(mockFactoryService).create(ApplicationFeature.class);
                inSequence(sequence);
                will(returnValue(newApplicationFeature(ApplicationFeatureId.newNamespace("org.isisaddons.module.security"))));

                oneOf(mockFactoryService).create(ApplicationFeature.class);
                inSequence(sequence);
                will(returnValue(newApplicationFeature(ApplicationFeatureId.newNamespace("org.isisaddons.module"))));

                oneOf(mockFactoryService).create(ApplicationFeature.class);
                inSequence(sequence);
                will(returnValue(newApplicationFeature(ApplicationFeatureId.newNamespace("org.isisaddons"))));

                oneOf(mockFactoryService).create(ApplicationFeature.class);
                inSequence(sequence);
                will(returnValue(newApplicationFeature(ApplicationFeatureId.newNamespace("org"))));
            }});

            // when
            applicationFeatureRepository.createApplicationFeaturesFor(mockSpec);

            // then
            final ApplicationFeature orgPkg = applicationFeatureRepository.findNamespace(ApplicationFeatureId.newNamespace("org"));
            assertThat(orgPkg, is(notNullValue()));
            final ApplicationFeature orgIsisaddonsPkg = applicationFeatureRepository.findNamespace(ApplicationFeatureId.newNamespace("org.isisaddons"));
            assertThat(orgPkg, is(notNullValue()));
            final ApplicationFeature featurePkg = applicationFeatureRepository.findNamespace(ApplicationFeatureId.newNamespace("org.isisaddons.module.security.dom.feature"));
            assertThat(orgPkg, is(notNullValue()));
            assertThat(orgPkg.getContents(), contains(orgIsisaddonsPkg.getFeatureId()));
            assertThat(featurePkg.getContents(), contains(ApplicationFeatureId.newType(Bar.class.getName())));

            // then
            final ApplicationFeature barClass = applicationFeatureRepository.findLogicalType(ApplicationFeatureId.newType(Bar.class.getName()));
            assertThat(barClass, is(Matchers.notNullValue()));

            // then the mockActThatIsHidden is not listed.
            assertThat(barClass.getProperties().size(), is(1));
            assertThat(barClass.getCollections().size(), is(1));
            assertThat(barClass.getActions().size(), is(1));
            assertThat(barClass.getProperties(),
                    containsInAnyOrder(
                            ApplicationFeatureId.newMember(Bar.class.getName(), "someProperty")
                            ));
            assertThat(barClass.getCollections(),
                    containsInAnyOrder(
                            ApplicationFeatureId.newMember(Bar.class.getName(), "someCollection")
                            ));
            assertThat(barClass.getActions(),
                    containsInAnyOrder(
                            ApplicationFeatureId.newMember(Bar.class.getName(), "someAction")
                            ));
        }

    }

    public static class AddClassParent extends ApplicationFeatureRepositoryDefaultTest {

        private static ApplicationFeature newApplicationFeature(ApplicationFeatureId featId) {
            return new ApplicationFeatureDefault(featId);
        }
        
        @Override
        @Before
        public void setUp() throws Exception {
            super.setUp();

            context.checking(new Expectations() {{
                allowing(mockServiceRegistry).streamRegisteredBeans();
                will(returnValue(_Lists.newArrayList().stream()));

                allowing(mockSpecificationLoader).snapshotSpecifications();
                will(returnValue(Can.empty()));
            }});

        }

        @Test
        public void parentAlreadyEncountered() throws Exception {

            // given
            final ApplicationFeatureId packageId = ApplicationFeatureId.newNamespace("com.mycompany");
            final ApplicationFeature pkg = newApplicationFeature(packageId);
            applicationFeatureRepository.namespaceFeatures.put(packageId, pkg);

            final ApplicationFeatureId classFeatureId = ApplicationFeatureId.newType("com.mycompany.Bar");



            // when
            final ApplicationFeatureId applicationFeatureId = 
                    applicationFeatureRepository.addClassParent(classFeatureId);

            // then
            Assert.assertThat(applicationFeatureId, is(equalTo(packageId)));
        }

    }

}