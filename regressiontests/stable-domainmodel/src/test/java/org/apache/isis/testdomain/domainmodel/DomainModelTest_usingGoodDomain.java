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
package org.apache.isis.testdomain.domainmodel;

import java.util.List;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.isis.applib.annotation.Introspection.EncapsulationPolicy;
import org.apache.isis.applib.annotation.Introspection.MemberAnnotationPolicy;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.services.metamodel.BeanSort;
import org.apache.isis.applib.services.metamodel.Config;
import org.apache.isis.applib.services.metamodel.MetaModelService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.title.TitleService;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.all.named.MemberNamedFacet;
import org.apache.isis.core.metamodel.facets.members.publish.execution.ExecutionPublishingFacet;
import org.apache.isis.core.metamodel.facets.object.icon.IconFacet;
import org.apache.isis.core.metamodel.facets.object.introspection.IntrospectionPolicyFacet;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.core.metamodel.facets.param.choices.ActionParameterChoicesFacet;
import org.apache.isis.core.metamodel.facets.param.defaults.ActionParameterDefaultsFacet;
import org.apache.isis.core.metamodel.postprocessors.collparam.ActionParameterChoicesFacetFromParentedCollection;
import org.apache.isis.core.metamodel.postprocessors.collparam.ActionParameterDefaultsFacetFromAssociatedCollection;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.schema.metamodel.v2.DomainClassDto;
import org.apache.isis.testdomain.conf.Configuration_headless;
import org.apache.isis.testdomain.model.good.Configuration_usingValidDomain;
import org.apache.isis.testdomain.model.good.ElementTypeAbstract;
import org.apache.isis.testdomain.model.good.ElementTypeConcrete;
import org.apache.isis.testdomain.model.good.ElementTypeInterface;
import org.apache.isis.testdomain.model.good.ProperChoicesWhenChoicesFrom;
import org.apache.isis.testdomain.model.good.ProperElementTypeVm;
import org.apache.isis.testdomain.model.good.ProperFullyImpl;
import org.apache.isis.testdomain.model.good.ProperGenericImpl;
import org.apache.isis.testdomain.model.good.ProperInterface2;
import org.apache.isis.testdomain.model.good.ProperMemberInheritanceInterface;
import org.apache.isis.testdomain.model.good.ProperMemberInheritance_usingAbstract;
import org.apache.isis.testdomain.model.good.ProperMemberInheritance_usingInterface;
import org.apache.isis.testdomain.model.good.ProperMemberSupport;
import org.apache.isis.testdomain.model.good.ProperServiceWithMixin;
import org.apache.isis.testdomain.model.good.ViewModelWithAnnotationOptionalUsingPrivateSupport;
import org.apache.isis.testdomain.model.good.ViewModelWithEncapsulatedMembers;
import org.apache.isis.testdomain.util.interaction.DomainObjectTesterFactory;
import org.apache.isis.testing.integtestsupport.applib.validate.DomainModelValidator;

import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_headless.class,
                Configuration_usingValidDomain.class,

        },
        properties = {
                "isis.core.meta-model.introspector.mode=FULL",
                "isis.applib.annotation.domain-object.editing=TRUE",
                "isis.core.meta-model.validator.explicit-object-type=FALSE", // does not override any of the imports
                "logging.level.DependentArgUtils=DEBUG"
        })
@TestPropertySource({
    //IsisPresets.DebugMetaModel,
    //IsisPresets.DebugProgrammingModel,
    IsisPresets.SilenceMetaModel,
    IsisPresets.SilenceProgrammingModel
})
class DomainModelTest_usingGoodDomain {

    @Inject private MetaModelService metaModelService;
    @Inject private JaxbService jaxbService;
    @Inject private ServiceRegistry serviceRegistry;
    @Inject private SpecificationLoader specificationLoader;
    @Inject private TitleService titleService;
    @Inject private IsisConfiguration isisConfig;
    @Inject private DomainObjectTesterFactory testerFactory;

    void debug() {
        val config = new Config()
//              .withIgnoreNoop()
//              .withIgnoreAbstractClasses()
//              .withIgnoreBuiltInValueTypes()
//              .withIgnoreInterfaces()
                //.withPackagePrefix("*")
                .withNamespacePrefix("org.apache.isis.testdomain.")
                ;

        System.out.println("=== listing MM");
        val metamodelDto = metaModelService.exportMetaModel(config);
        for (DomainClassDto domainClass : metamodelDto.getDomainClassDto()) {
            System.out.println("dc: " + domainClass.getId());
            val xmlString = jaxbService.toXml(domainClass);
            System.out.println(xmlString);
        }
        System.out.println("==============");
    }

    @Test
    void goodDomain_shouldPassValidation() {
        //debug();
        assertFalse(specificationLoader.snapshotSpecifications().isEmpty());

        val validateDomainModel = new DomainModelValidator(serviceRegistry);
        validateDomainModel.throwIfInvalid(); // should not throw
    }

    @Test
    void reservedPrefixShouldBeAllowed_onExplicitAction() {

        val tester = testerFactory.actionTester(ProperMemberSupport.class, "hideMe");
        tester.assertExists(true);
        tester.assertMemberId("hideMe");
    }

    @Test
    void typeLevelAnnotations_shouldBeHonored_onMixins() {

        val holderSpec = specificationLoader.specForTypeElseFail(ProperMemberSupport.class);

        val mx_action = holderSpec.getActionElseFail("action"); // when @Action at type level
        assertNotNull(mx_action);
        assertEquals("action", mx_action.getId());
        assertEquals("foo", mx_action.getStaticFriendlyName().get());
        assertEquals("bar", mx_action.getStaticDescription().get());
        assertHasPublishedActionFacet(mx_action);

        val mx_action2 = holderSpec.getActionElseFail("action2"); // proper mixed-in action support
        assertNotNull(mx_action2);
        assertHasPublishedActionFacet(mx_action2);

        val mx_property = holderSpec.getAssociationElseFail("property"); // when @Property at type level
        assertNotNull(mx_property);
        assertEquals("property", mx_property.getId());
        assertEquals("foo", mx_property.getStaticFriendlyName().get());
        assertEquals("bar", mx_property.getStaticDescription().get());

        val mx_property2 = holderSpec.getAssociationElseFail("property2"); // when @Property at method level
        assertNotNull(mx_property2);
        assertEquals("property2", mx_property2.getId());
        assertEquals("foo", mx_property2.getStaticFriendlyName().get());
        assertEquals("bar", mx_property2.getStaticDescription().get());

        val mx_collection = holderSpec.getAssociationElseFail("collection"); // when @Collection at type level
        assertNotNull(mx_collection);
        assertEquals("collection", mx_collection.getId());
        assertEquals("foo", mx_collection.getStaticFriendlyName().get());
        assertEquals("bar", mx_collection.getStaticDescription().get());

        val mx_collection2 = holderSpec.getAssociationElseFail("collection2"); // when @Collection at method level
        assertNotNull(mx_collection2);
        assertEquals("collection2", mx_collection2.getId());
        assertEquals("foo", mx_collection2.getStaticFriendlyName().get());
        assertEquals("bar", mx_collection2.getStaticDescription().get());

    }

    @Test
    void memberLevelAnnotations_shouldResolveUnambiguous_onMixins() {

        val holderSpec = specificationLoader.specForTypeElseFail(ProperMemberSupport.class);

        val mx_openRestApi = holderSpec.getDeclaredAction("openRestApi"); // built-in mixin support
        assertNotNull(mx_openRestApi);

        assertThrows(Exception.class, ()->holderSpec.getAssociationElseFail("openRestApi")); // should not be picked up as a property

    }

    @Test
    void fullyAbstractObject_whenImplemented_shouldBeSupported() {
        val tester = testerFactory.objectTester(ProperFullyImpl.class);
        tester.assertTitle("title");
        tester.assertIcon("icon");
        tester.assertCssClass("css");
        tester.assertLayout("layout");
    }

    @Test
    void genericInterface_whenImplemented_shouldBeSupported() {
        val tester = testerFactory.propertyTester(ProperGenericImpl.class, "value");
        tester.assertExists(true);
        tester.assertValue("aValue");
    }

    @ParameterizedTest
    @MethodSource("provideProperMemberInheritanceTypes")
    void titleAndIconName_shouldBeInheritable(final Class<?> type) throws Exception {

        val spec = specificationLoader.specForTypeElseFail(type);

        val titleFacet = spec.getFacet(TitleFacet.class);
        assertNotNull(titleFacet);

        val iconFacet = spec.getFacet(IconFacet.class);
        assertNotNull(iconFacet);

        if(!spec.isAbstract()) {
            val instance = type.getDeclaredConstructor().newInstance();
            assertEquals("inherited title", titleService.titleOf(instance));
            assertEquals("inherited icon", titleService.iconNameOf(instance));

            val domainObject = ManagedObject.of(spec, instance);
            assertEquals("inherited title", titleFacet.title(domainObject));
            assertEquals("inherited icon", iconFacet.iconName(domainObject));
        }

    }

    @ParameterizedTest
    @MethodSource("provideProperMemberInheritanceTypes")
    void metamodelContributingMembers_shouldBeInheritable(final Class<?> type) {

        val holderSpec = specificationLoader.specForTypeElseFail(type);

        val action = holderSpec.getActionElseFail("sampleAction");
        assertNotNull(action);
        assertEquals("sampleAction", action.getId());
        assertEquals("foo", action.getStaticFriendlyName().get());
        assertEquals("bar", action.getStaticDescription().get());

        val property = holderSpec.getAssociationElseFail("sampleProperty");
        assertNotNull(property);
        assertEquals("sampleProperty", property.getId());
        assertEquals("foo", property.getStaticFriendlyName().get());
        assertEquals("bar", property.getStaticDescription().get());

        val collection = holderSpec.getAssociationElseFail("sampleCollection");
        assertNotNull(collection);
        assertEquals("sampleCollection", collection.getId());
        assertEquals("foo", collection.getStaticFriendlyName().get());
        assertEquals("bar", collection.getStaticDescription().get());

    }

    @ParameterizedTest
    @MethodSource("provideProperMemberInheritanceTypes")
    void metamodelContributingActions_shouldBeUnique_whenOverridden(final Class<?> type) {

        if(type.isInterface()
                && type.getSuperclass()==null) {
            return; // not implemented for interface that don't extend from others
        }

        val holderSpec = specificationLoader.specForTypeElseFail(type);

        val super_action = holderSpec.getActionElseFail("sampleActionOverride");
        assertNotNull(super_action);
        assertEquals("sampleActionOverride", super_action.getId());
        assertEquals("foo", super_action.getStaticFriendlyName().get());
        assertEquals("bar", super_action.getStaticDescription().get());

        assertEquals(1L, holderSpec.streamAnyActions(MixedIn.EXCLUDED)
                .filter(prop->prop.getId().equals("sampleActionOverride"))
                .count());

    }

    @ParameterizedTest
    @MethodSource("provideProperMemberInheritanceTypes")
    void metamodelContributingProperties_shouldBeUnique_whenOverridden(final Class<?> type) {

        if(type.isInterface()
                && type.getSuperclass()==null) {
            return; // not implemented for interface that don't extend from others
        }

        val holderSpec = specificationLoader.specForTypeElseFail(type);

        val super_property = holderSpec.getAssociationElseFail("samplePropertyOverride");
        assertNotNull(super_property);
        assertEquals("samplePropertyOverride", super_property.getId());
        assertEquals("foo", super_property.getStaticFriendlyName().get());
        assertEquals("bar", super_property.getStaticDescription().get());

        assertEquals(1L, holderSpec.streamProperties(MixedIn.EXCLUDED)
                .filter(prop->prop.getId().equals("samplePropertyOverride"))
                .count());

    }

    @Test
    void elementTypes_shouldBeIntrospected_whenDiscoveredViaGenerics_usingNoWildcards() {

        // when using generic type (no wild-cards)

        val vmSpec = specificationLoader.specForTypeElseFail(ProperElementTypeVm.class);

        val concreteColl = vmSpec.getCollectionElseFail("concreteColl");
        val concreteCollSpec = concreteColl.getElementType();

        assertEquals(ElementTypeConcrete.class, concreteCollSpec.getCorrespondingClass());
        assertEquals(BeanSort.VIEW_MODEL, concreteCollSpec.getBeanSort());
        assertHasAction(concreteCollSpec, "abstractAction");
        assertHasAction(concreteCollSpec, "interfaceAction");
        assertHasProperty(concreteCollSpec, "abstractProp");
        assertHasProperty(concreteCollSpec, "interfaceProp");

        val interfaceColl = vmSpec.getCollectionElseFail("interfaceColl");
        val interfaceCollSpec = interfaceColl.getElementType();

        assertEquals(ElementTypeInterface.class, interfaceCollSpec.getCorrespondingClass());
        assertEquals(BeanSort.ABSTRACT, interfaceCollSpec.getBeanSort());
        assertHasAction(interfaceCollSpec, "interfaceAction");
        assertHasProperty(interfaceCollSpec, "interfaceProp");

        val abstractColl = vmSpec.getCollectionElseFail("abstractColl");
        val abstractCollSpec = abstractColl.getElementType();

        assertEquals(ElementTypeAbstract.class, abstractCollSpec.getCorrespondingClass());
        assertEquals(BeanSort.ABSTRACT, abstractCollSpec.getBeanSort());
        assertHasAction(abstractCollSpec, "abstractAction");
        assertHasProperty(abstractCollSpec, "abstractProp");

    }

    @Test
    void elementTypes_shouldBeIntrospected_whenDiscoveredViaGenerics_usingWildcards() {

        // when using generic type (w/ wild-cards)

        val vmSpec = specificationLoader.specForTypeElseFail(ProperElementTypeVm.class);

        val concreteColl = vmSpec.getCollectionElseFail("concreteColl2");
        val concreteCollSpec = concreteColl.getElementType();

        assertEquals(ElementTypeConcrete.class, concreteCollSpec.getCorrespondingClass());
        assertEquals(BeanSort.VIEW_MODEL, concreteCollSpec.getBeanSort());
        assertHasAction(concreteCollSpec, "abstractAction");
        assertHasAction(concreteCollSpec, "interfaceAction");
        assertHasProperty(concreteCollSpec, "abstractProp");
        assertHasProperty(concreteCollSpec, "interfaceProp");

        val interfaceColl = vmSpec.getCollectionElseFail("interfaceColl2");
        val interfaceCollSpec = interfaceColl.getElementType();

        assertEquals(ElementTypeInterface.class, interfaceCollSpec.getCorrespondingClass());
        assertEquals(BeanSort.ABSTRACT, interfaceCollSpec.getBeanSort());
        assertHasAction(interfaceCollSpec, "interfaceAction");
        assertHasProperty(interfaceCollSpec, "interfaceProp");

        val abstractColl = vmSpec.getCollectionElseFail("abstractColl2");
        val abstractCollSpec = abstractColl.getElementType();

        assertEquals(ElementTypeAbstract.class, abstractCollSpec.getCorrespondingClass());
        assertEquals(BeanSort.ABSTRACT, abstractCollSpec.getBeanSort());
        assertHasAction(abstractCollSpec, "abstractAction");
        assertHasProperty(abstractCollSpec, "abstractProp");

    }

    @Test
    void interfaces_shouldSupport_inheritedMembers() {

        val i2Spec = specificationLoader.specForTypeElseFail(ProperInterface2.class);

        assertEquals(BeanSort.ABSTRACT, i2Spec.getBeanSort());
        assertHasProperty(i2Spec, "a");
        assertHasProperty(i2Spec, "b");
        assertHasProperty(i2Spec, "c");
        assertHasProperty(i2Spec, "d");
        assertHasProperty(i2Spec, "e");
        assertHasProperty(i2Spec, "f");
    }

    @Test
    void actionParamChoices_shouldBeAllowed_toBeDerivedFromChoicesFrom() {

        val spec = specificationLoader.specForTypeElseFail(ProperChoicesWhenChoicesFrom.class);

        val action = spec.getActionElseFail("appendACharacterToCandidates");
        val param0 = action.getParameters().getFirstOrFail();

        assertEquals(
                ActionParameterChoicesFacetFromParentedCollection.class,
                param0.lookupFacet(ActionParameterChoicesFacet.class)
                    .map(Object::getClass)
                    .orElse(null));

        assertEquals(
                ActionParameterDefaultsFacetFromAssociatedCollection.class,
                param0.lookupFacet(ActionParameterDefaultsFacet.class)
                    .map(Object::getClass)
                    .orElse(null));

    }

    @ParameterizedTest
    @MethodSource("provideImperativelyNamed")
    void imperativelyNamedMembers(final String memberId, final String named, final String described) {

        val objectSpec = specificationLoader.specForTypeElseFail(ProperMemberSupport.class);
        val member = objectSpec.getMemberElseFail(memberId);
        val sampleObject = ManagedObject.of(objectSpec, new ProperMemberSupport());

        assertEquals(named, member.getFriendlyName(()->sampleObject));
        assertEquals(described, member.getDescription(()->sampleObject).orElse(null));
    }

    @Test
    void mixinsOnDomainServices_shouldBeAllowed() {

        val objectSpec = specificationLoader.specForTypeElseFail(ProperServiceWithMixin.class);

        val mixinSpec = specificationLoader.specForTypeElseFail(ProperServiceWithMixin.Now.class);

        assertTrue(mixinSpec.isMixin());

        assertEquals(
                1L,
                objectSpec.streamRuntimeActions(MixedIn.INCLUDED)
                .filter(ObjectAction::isMixedIn)
                .peek(act->{
                    //System.out.println("act: " + act);
                    val memberNamedFacet = act.getFacet(MemberNamedFacet.class);
                    assertNotNull(memberNamedFacet);
                    assertTrue(memberNamedFacet.getSpecialization().isLeft());
                })
                .count());

    }



    @Test
    void viewmodelWithEncapsulatedMembers() {

        // OBJECT

        val objectSpec = specificationLoader.specForTypeElseFail(ViewModelWithEncapsulatedMembers.class);

        val introspectionPolicyFacet = objectSpec.getFacet(IntrospectionPolicyFacet.class);
        assertNotNull(introspectionPolicyFacet);

        val introspectionPolicy = introspectionPolicyFacet.getIntrospectionPolicy(isisConfig);
        assertEquals(
                EncapsulationPolicy.ENCAPSULATED_MEMBERS_SUPPORTED,
                introspectionPolicy.getEncapsulationPolicy());
        assertEquals(
                MemberAnnotationPolicy.MEMBER_ANNOTATIONS_REQUIRED,
                introspectionPolicy.getMemberAnnotationPolicy());

        // PRIVATE ACTION

        val act = testerFactory
                .actionTester(ViewModelWithEncapsulatedMembers.class, "myAction");
        act.assertExists(true);
        act.assertIsExplicitlyAnnotated(true);
        act.assertVisibilityIsNotVetoed();
        act.assertUsabilityIsVetoedWith("action disabled for testing purposes");
        act.assertInvocationResultNoRules("Hallo World!");

        // -- PROPERTY WITH PRIVATE GETTER AND SETTER

        val prop = testerFactory
                .propertyTester(ViewModelWithEncapsulatedMembers.class, "propWithPrivateAccessors");
        prop.assertExists(true);
        prop.assertIsExplicitlyAnnotated(true);
        prop.assertVisibilityIsNotVetoed();
        prop.assertUsabilityIsVetoedWith("property disabled for testing purposes");
        prop.assertValue("Foo");
        prop.assertValueUpdate("Bar");

        // -- COLLECTION WITH PRIVATE GETTER AND SETTER

        val coll = testerFactory
                .collectionTester(ViewModelWithEncapsulatedMembers.class, "collWithPrivateAccessors");
        coll.assertExists(true);
        coll.assertIsExplicitlyAnnotated(true);
        coll.assertVisibilityIsNotVetoed();
        coll.assertUsabilityIsVetoedWith("collection disabled for testing purposes");
        coll.assertCollectionElements(List.of("Foo"));
    }

    @Test
    void viewmodelWithAnnotationOptional_usingPrivateSupport() {

        // OBJECT

        val objectSpec = specificationLoader.specForTypeElseFail(ViewModelWithAnnotationOptionalUsingPrivateSupport.class);

        val introspectionPolicyFacet = objectSpec.getFacet(IntrospectionPolicyFacet.class);
        assertNotNull(introspectionPolicyFacet);

        val introspectionPolicy = introspectionPolicyFacet.getIntrospectionPolicy(isisConfig);
        assertEquals(
                EncapsulationPolicy.ONLY_PUBLIC_MEMBERS_SUPPORTED,
                introspectionPolicy.getEncapsulationPolicy());
        assertEquals(
                MemberAnnotationPolicy.MEMBER_ANNOTATIONS_OPTIONAL,
                introspectionPolicy.getMemberAnnotationPolicy());

        // PRIVATE ACTION

        val act = testerFactory
                .actionTester(ViewModelWithAnnotationOptionalUsingPrivateSupport.class, "myAction");
        act.assertExists(true);
        act.assertIsExplicitlyAnnotated(true);
        act.assertVisibilityIsNotVetoed();
        act.assertUsabilityIsVetoedWithAll(
                Can.of("object disabled for testing purposes", "action disabled for testing purposes"));
        act.assertInvocationResultNoRules("Hallo World!");

        // -- PROPERTY WITH PRIVATE GETTER AND SETTER

        val prop = testerFactory
                .propertyTester(ViewModelWithAnnotationOptionalUsingPrivateSupport.class, "propWithPrivateAccessors");
        prop.assertExists(true);
        prop.assertIsExplicitlyAnnotated(true);
        prop.assertVisibilityIsNotVetoed();
        prop.assertUsabilityIsVetoedWithAll(
                Can.of("object disabled for testing purposes", "property disabled for testing purposes"));
        prop.assertValue("Foo");
        prop.assertValueUpdate("Bar");

        // -- COLLECTION WITH PRIVATE GETTER AND SETTER

        val coll = testerFactory
                .collectionTester(ViewModelWithAnnotationOptionalUsingPrivateSupport.class, "collWithPrivateAccessors");
        coll.assertExists(true);
        coll.assertIsExplicitlyAnnotated(true);
        coll.assertVisibilityIsNotVetoed();
        coll.assertUsabilityIsVetoedWithAll(
                Can.of("object disabled for testing purposes", "collection disabled for testing purposes"));
        coll.assertCollectionElements(List.of("Foo"));
    }


    // -- HELPER

    private void assertHasProperty(final ObjectSpecification spec, final String propertyId) {
        spec.getPropertyElseFail(propertyId);
    }

    private void assertHasAction(final ObjectSpecification spec, final String actionId) {
        spec.getActionElseFail(actionId);
    }

    private void assertHasPublishedActionFacet(final FacetHolder facetHolder) {
        val facet = facetHolder.getFacet(ExecutionPublishingFacet.class);
        assertNotNull(facet);
    }

    static Stream<Arguments> provideProperMemberInheritanceTypes() {
        return Stream.of(
                Arguments.of(ProperMemberInheritance_usingAbstract.class),
                Arguments.of(ProperMemberInheritance_usingInterface.class),
                Arguments.of(ProperMemberInheritanceInterface.class)
        );
    }

    static Stream<Arguments> provideImperativelyNamed() {
        return Stream.of(
                // regular on type
                Arguments.of("myAction", "named-imperative[MyAction]", "described-imperative[MyAction]"),
                Arguments.of("myProp", "named-imperative[MyProp]", "described-imperative[MyProp]"),
                Arguments.of("myColl", "named-imperative[MyColl]", "described-imperative[MyColl]"),
                // contributed by mixin(s)
                Arguments.of("action5", "named-imperative[action5]", "described-imperative[action5]"),
                Arguments.of("property3", "named-imperative[property3]", "described-imperative[property3]"),
                Arguments.of("collection3", "named-imperative[collection3]", "described-imperative[collection3]")
        );
    }


}
