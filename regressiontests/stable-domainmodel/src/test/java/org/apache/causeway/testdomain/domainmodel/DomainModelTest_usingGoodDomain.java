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
package org.apache.causeway.testdomain.domainmodel;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.causeway.applib.annotation.Introspection.EncapsulationPolicy;
import org.apache.causeway.applib.annotation.Introspection.MemberAnnotationPolicy;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.jaxb.JaxbService;
import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.applib.services.metamodel.Config;
import org.apache.causeway.applib.services.metamodel.MetaModelService;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.applib.services.title.TitleService;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.all.named.MemberNamedFacet;
import org.apache.causeway.core.metamodel.facets.members.publish.execution.ExecutionPublishingFacet;
import org.apache.causeway.core.metamodel.facets.object.icon.IconFacet;
import org.apache.causeway.core.metamodel.facets.object.introspection.IntrospectionPolicyFacet;
import org.apache.causeway.core.metamodel.facets.object.title.TitleFacet;
import org.apache.causeway.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.causeway.core.metamodel.facets.param.choices.ActionParameterChoicesFacet;
import org.apache.causeway.core.metamodel.facets.param.choices.ActionParameterChoicesFacetFromAction;
import org.apache.causeway.core.metamodel.facets.param.choices.methodnum.ActionParameterChoicesFacetViaMethod;
import org.apache.causeway.core.metamodel.facets.param.defaults.ActionParameterDefaultsFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.postprocessors.param.ActionParameterDefaultsFacetFromAssociatedCollection;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.schema.metamodel.v2.DomainClassDto;
import org.apache.causeway.testdomain.conf.Configuration_headless;
import org.apache.causeway.testdomain.model.good.Configuration_usingValidDomain;
import org.apache.causeway.testdomain.model.good.ElementTypeAbstract;
import org.apache.causeway.testdomain.model.good.ElementTypeConcrete;
import org.apache.causeway.testdomain.model.good.ElementTypeInterface;
import org.apache.causeway.testdomain.model.good.ProperChoicesWhenActionHasParamSupportingMethodTypeOfReference;
import org.apache.causeway.testdomain.model.good.ProperChoicesWhenActionHasParamSupportingMethodTypeOfString;
import org.apache.causeway.testdomain.model.good.ProperChoicesWhenChoicesFrom;
import org.apache.causeway.testdomain.model.good.ProperElementTypeVm;
import org.apache.causeway.testdomain.model.good.ProperFullyImpl;
import org.apache.causeway.testdomain.model.good.ProperGenericImpl;
import org.apache.causeway.testdomain.model.good.ProperInterface2;
import org.apache.causeway.testdomain.model.good.ProperMemberInheritanceInterface;
import org.apache.causeway.testdomain.model.good.ProperMemberInheritance_usingAbstract;
import org.apache.causeway.testdomain.model.good.ProperMemberInheritance_usingInterface;
import org.apache.causeway.testdomain.model.good.ProperMemberSupport;
import org.apache.causeway.testdomain.model.good.ProperMemberSupportDiscovery;
import org.apache.causeway.testdomain.model.good.ProperMixinContribution;
import org.apache.causeway.testdomain.model.good.ProperMixinContribution_action1;
import org.apache.causeway.testdomain.model.good.ProperMixinContribution_action2;
import org.apache.causeway.testdomain.model.good.ProperMixinContribution_action3;
import org.apache.causeway.testdomain.model.good.ProperMixinContribution_action4;
import org.apache.causeway.testdomain.model.good.ProperMixinContribution_action5;
import org.apache.causeway.testdomain.model.good.ProperMixinContribution_action6;
import org.apache.causeway.testdomain.model.good.ProperObjectWithAlias;
import org.apache.causeway.testdomain.model.good.ProperServiceWithAlias;
import org.apache.causeway.testdomain.model.good.ProperServiceWithMixin;
import org.apache.causeway.testdomain.model.good.ProperViewModelInferredFromNotBeingAnEntity;
import org.apache.causeway.testdomain.model.good.ViewModelWithAnnotationOptionalUsingPrivateSupport;
import org.apache.causeway.testdomain.model.good.ViewModelWithEncapsulatedMembers;
import org.apache.causeway.testdomain.util.interaction.DomainObjectTesterFactory;
import org.apache.causeway.testing.integtestsupport.applib.validate.DomainModelValidator;

import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_headless.class,
                Configuration_usingValidDomain.class,

        },
        properties = {
                //"causeway.core.meta-model.introspector.policy=annotation_optional",
                "causeway.core.meta-model.introspector.mode=FULL",
                "causeway.applib.annotation.domain-object.editing=TRUE",
                "causeway.core.meta-model.validator.explicit-object-type=FALSE", // does not override any of the imports
                "logging.level.DependentArgUtils=DEBUG"
        })
@TestPropertySource({
    //CausewayPresets.DebugMetaModel,
    //CausewayPresets.DebugProgrammingModel,
    CausewayPresets.SilenceMetaModel,
    CausewayPresets.SilenceProgrammingModel
})
class DomainModelTest_usingGoodDomain {

    @Inject private MetaModelService metaModelService;
    @Inject private JaxbService jaxbService;
    @Inject private ServiceRegistry serviceRegistry;
    @Inject private SpecificationLoader specificationLoader;
    @Inject private TitleService titleService;
    @Inject private CausewayConfiguration causewayConfig;
    @Inject private DomainObjectTesterFactory testerFactory;

    void debug() {
        val config = Config.builder().build()
                .withNamespacePrefix("org.apache.causeway.testdomain.");

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

        // guard against left overs from shared context
        val validationFailures = validateDomainModel.getFailures().stream()
                .filter(f->!f.getOrigin().getClassName().contains("bad"))
                .collect(Collectors.toSet());

        if(!validationFailures.isEmpty()) {

            fail(String.format("%d problems found:\n%s",
                    validationFailures.size(),
                    validationFailures.stream()
                    .map(validationFailure->validationFailure.getMessage())
                    .collect(Collectors.joining("\n"))));
        }
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

        val mx_action = holderSpec.getActionElseFail("action1"); // when @Action at type level
        assertNotNull(mx_action);
        assertEquals("action1", mx_action.getId());
        assertEquals("foo", mx_action.getStaticFriendlyName().get());
        assertEquals("bar", mx_action.getStaticDescription().get());
        assertHasPublishedActionFacet(mx_action);

        val mx_action2 = holderSpec.getActionElseFail("action2"); // proper mixed-in action support
        assertNotNull(mx_action2);
        assertHasPublishedActionFacet(mx_action2);

        val mx_property = holderSpec.getAssociationElseFail("property1"); // when @Property at type level
        assertNotNull(mx_property);
        assertEquals("property1", mx_property.getId());
        assertEquals("foo", mx_property.getStaticFriendlyName().get());
        assertEquals("bar", mx_property.getStaticDescription().get());

        val mx_property2 = holderSpec.getAssociationElseFail("property2"); // when @Property at method level
        assertNotNull(mx_property2);
        assertEquals("property2", mx_property2.getId());
        assertEquals("foo", mx_property2.getStaticFriendlyName().get());
        assertEquals("bar", mx_property2.getStaticDescription().get());

        val mx_collection = holderSpec.getAssociationElseFail("collection1"); // when @Collection at type level
        assertNotNull(mx_collection);
        assertEquals("collection1", mx_collection.getId());
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

            val domainObject = ManagedObject.adaptSingular(spec, instance);
            assertEquals("inherited title", domainObject.getTitle());
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
    void elementTypeInference_fromGenerics_usingNoWildcards() {

        // when using generic type (no wild-cards)

        val vmSpec = specificationLoader.specForTypeElseFail(ProperElementTypeVm.class);

        // scenario 1

        //abstract Set<ElementTypeInterface> getSetOfInterfaceType();
        //override SortedSet<ElementTypeInterface> getSetOfInterfaceType();

        val interfaceSet = vmSpec.getCollectionElseFail("setOfInterfaceType");
        val interfaceSetSpec = interfaceSet.getElementType();

        assertEquals(ElementTypeInterface.class, interfaceSetSpec.getCorrespondingClass());
        assertEquals(BeanSort.ABSTRACT, interfaceSetSpec.getBeanSort());

        //TODO scenario 2

        //abstract ImmutableCollection<ElementTypeInterface> getImmutableOfInterfaceType();
        //override Can<ElementTypeInterface> getImmutableOfInterfaceType();

        val interfaceIterLookup = vmSpec.getAssociationElseFail("immutableOfInterfaceType");
        assertTrue(interfaceIterLookup.isOneToManyAssociation(), "required to be a coll");

        val interfaceIter = vmSpec.getCollectionElseFail("immutableOfInterfaceType");
        val interfaceIterSpec = interfaceIter.getElementType();

        assertEquals(ElementTypeInterface.class, interfaceIterSpec.getCorrespondingClass());
        assertEquals(BeanSort.ABSTRACT, interfaceIterSpec.getBeanSort());

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

        //TODO scenario 1

        //abstract Set<? extends ElementTypeConcrete> getSetOfConcreteType();
        //override SortedSet<? extends ElementTypeConcrete> getSetOfConcreteType();

        //TODO scenario 2

        //abstract Iterable<? extends ElementTypeConcrete> getIterableOfConcreteType();
        //override Can<? extends ElementTypeConcrete> getIterableOfConcreteType();


    }

    @Test
    void domainObjects_ifNatureNotSpecified_shouldConsiderBeanTypeClassifier() {
        val vmSpec = specificationLoader.specForTypeElseFail(ProperViewModelInferredFromNotBeingAnEntity.class);

        assertEquals(BeanSort.VIEW_MODEL, vmSpec.getBeanSort());
        assertNotNull(vmSpec.getFacet(ViewModelFacet.class));
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
        val param0 = action.getParameters().getFirstElseFail();

        assertEquals(
                ActionParameterChoicesFacetFromAction.class,
                param0.lookupFacet(ActionParameterChoicesFacet.class)
                    .map(Object::getClass)
                    .orElse(null));

        assertEquals(
                ActionParameterDefaultsFacetFromAssociatedCollection.class,
                param0.lookupFacet(ActionParameterDefaultsFacet.class)
                    .map(Object::getClass)
                    .orElse(null));
    }

    @Test
    void actionParamChoices_shouldBeAvailable_whenMixedInActionHasParamSupportingMethodTypeOfString() {

        val spec = specificationLoader.specForTypeElseFail(ProperChoicesWhenActionHasParamSupportingMethodTypeOfString.class);

        val action = spec.getActionElseFail("remove");
        val param0 = action.getParameters().getFirstElseFail();

        assertEquals(
                ActionParameterChoicesFacetViaMethod.class,
                param0.lookupFacet(ActionParameterChoicesFacet.class)
                    .map(Object::getClass)
                    .orElse(null));
    }

    @Test
    void actionParamChoices_shouldBeAvailable_whenMixedInActionHasParamSupportingMethodTypeOfReference() {

        val spec = specificationLoader
                .specForTypeElseFail(ProperChoicesWhenActionHasParamSupportingMethodTypeOfReference.class);

        val action = spec.getActionElseFail("remove");
        val param0 = action.getParameters().getFirstElseFail();

        assertEquals(
                ActionParameterChoicesFacetViaMethod.class,
                param0.lookupFacet(ActionParameterChoicesFacet.class)
                    .map(Object::getClass)
                    .orElse(null));
    }

    /**
     *  annotation provided 'choicesFrom' fallback, if no explicit choices member-support is given
     *  (that are params #1 and #3)
     */
    @SuppressWarnings("unchecked")
    @Test
    void actionParamChoices_shouldBeAvailable_whenMixedInActionHasChoicesFromAnnotationAttribute() {

        val actTester = testerFactory
                .actionTester(ProperMemberSupport.class, "action6");
        actTester.assertExists(true);

        // low-level MM inspection
        {
            val action = actTester.getActionMetaModelElseFail();
            action.getParameters()
            .forEach(param->{

               assertTrue(param.getAction().isMixedIn(), ()->String.format(
                       "param %d is expected to belong to a mixed-in action",
                       param.getParameterIndex()));

               val choicesFacet = param.getFacet(ActionParameterChoicesFacet.class);

               assertNotNull(choicesFacet, ()->String.format(
                       "param %d is expected to have an ActionParameterChoicesFacet",
                       param.getParameterIndex()));
            });
        }

        val mixee = actTester.getActionOwnerAs(ProperMemberSupport.class);
        mixee.setMyColl(List.of(
                "Hallo",
                "World"));

        val expectedParamChoices = Can.of(
                "Hallo",
                "World");

        // verify param choices from 'choicesFrom' action annotation attribute (param params #1 and #3)
        actTester.assertParameterChoices(true, String.class,
                choices0->assertEquals(expectedParamChoices, Can.ofIterable(choices0), ()->"param 0 choices mismatch"),
                choices1->assertEquals(expectedParamChoices, Can.ofIterable(choices1), ()->"param 1 choices mismatch"),
                choices2->assertEquals(expectedParamChoices, Can.ofIterable(choices2), ()->"param 2 choices mismatch"),
                choices3->assertEquals(expectedParamChoices, Can.ofIterable(choices3), ()->"param 3 choices mismatch"));
    }

    @ParameterizedTest
    @MethodSource("provideImperativelyNamed")
    void imperativelyNamedMembers(final String memberId, final String named, final String described) {

        val objectSpec = specificationLoader.specForTypeElseFail(ProperMemberSupport.class);
        val member = objectSpec.getMemberElseFail(memberId);
        val sampleObject = ManagedObject.adaptSingular(objectSpec, new ProperMemberSupport());

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
    void aliasesOnDomainServices_shouldBeHonored() {

        val objectSpec = specificationLoader.specForTypeElseFail(ProperServiceWithAlias.class);
        assertTrue(objectSpec.isInjectable());
        assertTrue(objectSpec.getAction("now").isPresent());

        assertEquals(Can.of(
                "testdomain.v1.ProperServiceWithAlias",
                "testdomain.v2.ProperServiceWithAlias"),
                objectSpec.getAliases().map(LogicalType::getLogicalTypeName));

        assertEquals(objectSpec,
                specificationLoader.specForLogicalTypeName("testdomain.v1.ProperServiceWithAlias")
                .orElse(null));
        assertEquals(objectSpec,
                specificationLoader.specForLogicalTypeName("testdomain.v2.ProperServiceWithAlias")
                .orElse(null));
    }

    @Test
    void aliasesOnDomainObjects_shouldBeHonored() {

        val objectSpec = specificationLoader.specForTypeElseFail(ProperObjectWithAlias.class);
        assertTrue(objectSpec.isViewModel());
        assertTrue(objectSpec.getAction("now").isPresent());

        assertEquals(Can.of(
                "testdomain.v1.ProperObjectWithAlias",
                "testdomain.v2.ProperObjectWithAlias"),
                objectSpec.getAliases().map(LogicalType::getLogicalTypeName));

        assertEquals(objectSpec,
                specificationLoader.specForLogicalTypeName("testdomain.v1.ProperObjectWithAlias")
                .orElse(null));
        assertEquals(objectSpec,
                specificationLoader.specForLogicalTypeName("testdomain.v2.ProperObjectWithAlias")
                .orElse(null));
    }

    @Test
    void viewmodelWithEncapsulatedMembers() {

        // OBJECT

        val objectSpec = specificationLoader.specForTypeElseFail(ViewModelWithEncapsulatedMembers.class);

        val introspectionPolicyFacet = objectSpec.getFacet(IntrospectionPolicyFacet.class);
        assertNotNull(introspectionPolicyFacet);

        val introspectionPolicy = introspectionPolicyFacet.getIntrospectionPolicy(causewayConfig);
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

        val introspectionPolicy = introspectionPolicyFacet.getIntrospectionPolicy(causewayConfig);
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

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @ValueSource(classes = {
            ProperMemberSupportDiscovery.WhenEncapsulationEnabled.class,
            ProperMemberSupportDiscovery.WhenAnnotationRequired.class,
            ProperMemberSupportDiscovery.WhenAnnotationOptional.class
            })
    void properMemberSupportDiscovery(final Class<?> classUnderTest) {

        val act = testerFactory
                .actionTester(classUnderTest, "placeOrder");
        val prop = testerFactory
                .propertyTester(classUnderTest, "email");
        val coll = testerFactory
                .collectionTester(classUnderTest, "orders");

        act.assertExists(true);

        // namedPlaceOrder(): String = "my name"
        act.assertFriendlyName("my name");

        // describedPlaceOrder(): String = "my description"
        act.assertDescription("my description");

        // hidePlaceOrder(): boolean = false
        act.assertVisibilityIsNotVetoed();

        // disablePlaceOrder(): String = "my disable reason"
        act.assertUsabilityIsVetoedWith("my disable reason");

        // default0PlaceOrder(): String = "my default-0"
        // default1PlaceOrder(): String = "my default-1"
        act.assertParameterValues(
                false, // skip rule checking
                arg0->assertEquals(
                        "my default-0",
                        arg0),
                arg1->assertEquals(
                        "my default-1",
                        arg1));

        act.assertValidationMessage("my validation",
                false, // skip rule checking
                arg0->"",
                arg1->"");

        // hide0PlaceOrder(x): boolean = true
        // hide1PlaceOrder(y): boolean = false
        act.assertParameterVisibility(
                false, // skip rule checking
                arg0Visible->assertFalse(arg0Visible),
                arg1Visible->assertTrue(arg1Visible));

        // disable0PlaceOrder(x): String = "my disable reason-0"
        // disable1PlaceOrder(z): String = "my disable reason-1"
        act.assertParameterUsability(
                false, // skip rule checking
                arg0Veto->assertEquals(
                        "my disable reason-0",
                        arg0Veto),
                arg1Veto->assertEquals(
                        "my disable reason-1",
                        arg1Veto));

        // choices0PlaceOrder(x): List.of("my choice")
        act.assertParameterModel(
                false, // skip rule checking
                pendingArgsWhen->{},
                pendingArgsThen->{

                    val actualChoices =
                            pendingArgsThen.getObservableParamChoices(0).getValue().map(ManagedObject::getPojo);
                    assertEquals(
                            Can.of("my choice"),
                            actualChoices);

                });

        // autoComplete1PlaceOrder(y, search): List.of("my search arg=" + search)
        act.assertParameterModel(
                false, // skip rule checking
                pendingArgsWhen->{
                    pendingArgsWhen.getBindableParamSearchArgument(1).setValue("hello");
                },
                pendingArgsThen->{

                    val actualChoices =
                            pendingArgsThen.getObservableParamChoices(1).getValue().map(ManagedObject::getPojo);
                    assertEquals(
                            Can.of("my search arg=hello"),
                            actualChoices);

                });

        // validate0PlaceOrder(String x): String = "my validation-0"
        // validate1PlaceOrder(String y): String = "my validation-1"
        // validatePlaceOrder(String x, final String y): String = "my validation"
        act.assertParameterModel(
                false, // skip rule checking
                pendingArgsWhen->{},
                pendingArgsThen->{
                    assertEquals(
                            "my validation-0",
                            pendingArgsThen.getObservableParamValidation(0).getValue());
                    assertEquals(
                            "my validation-1",
                            pendingArgsThen.getObservableParamValidation(1).getValue());
                    assertEquals(
                            "my validation",
                            pendingArgsThen.validateParameterSetForAction().getReasonAsString().orElse(null));
                });

        // namedEmail(): String = "my email"
        prop.assertFriendlyName("my email");

        // describedEmail: String = "my email described"
        prop.assertDescription("my email described");

        // hideEmail(): boolean = true
        prop.assertVisibilityIsVetoed();

        // disableEmail(): String = "my email disable"
        prop.assertUsabilityIsVetoedWith("my email disable");

        // defaultEmail(): String = "my default email"
        prop.assertValueNegotiation(
                propNeg->{
                    assertEquals(
                            "my default email",
                            propNeg.getValue().getValue().getPojo());
                },
                propNegPostCommit->{});


        // choicesEmail(): Collection<String> = List.of("my email choice")
        prop.assertValueNegotiation(
                propNeg->{
                    assertEquals(
                            Can.of("my email choice"),
                            propNeg.getChoices().getValue().map(ManagedObject::getPojo));
                },
                propNegPostCommit->{});


        // validateEmail(final String email): String = "my email validate"
        prop.assertValueNegotiation(
                propNeg->{},
                propNegPostCommit->{
                    assertEquals(
                            "my email validate",
                            propNegPostCommit.getValidationMessage().getValue());
                });

        // -- COLLECTION

        // namedOrders(): String = "my orders"
        coll.assertFriendlyName("my orders");

        // describedOrders: String = "my orders described"
        coll.assertDescription("my orders described");

        // hideOrders(): boolean = true
        coll.assertVisibilityIsVetoed();

        // disableOrders(): String = "my orders disabled"
        coll.assertUsabilityIsVetoedWith("my orders disabled");

    }

    @ParameterizedTest
    @ValueSource(classes = {
            ProperMixinContribution_action1.class,
            ProperMixinContribution_action2.class,
            ProperMixinContribution_action3.class,
            ProperMixinContribution_action4.class,
            ProperMixinContribution_action5.class,
            ProperMixinContribution_action6.class})
    void mixins_shouldBePickedUp_asTheRightContributingFeature(final Class<?> mixinClass) {

        final String actionName = _Strings.splitThenStream(mixinClass.getSimpleName(), "_")
                .reduce((a, b)->b)
                .orElseThrow();

        val vmSpec = specificationLoader.specForTypeElseFail(ProperMixinContribution.class);
        assertHasAction(vmSpec, "myAction"); // regular action (just a sanity check)
        assertHasAction(vmSpec, actionName); // contributed action
        assertMissesProperty(vmSpec, actionName); // verify don't contributes as property
    }

    // -- HELPER

    private void assertHasProperty(final ObjectSpecification spec, final String propertyId) {
        spec.getPropertyElseFail(propertyId);
    }

    private void assertMissesProperty(final ObjectSpecification spec, final String propertyId) {
        assertFalse(spec.getProperty(propertyId).isPresent(),
                ()->String.format("unexpected to find a property '%s'", propertyId));
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
