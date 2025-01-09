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
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

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
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.all.named.MemberNamedFacet;
import org.apache.causeway.core.metamodel.facets.members.publish.execution.ExecutionPublishingFacet;
import org.apache.causeway.core.metamodel.facets.object.icon.IconFacet;
import org.apache.causeway.core.metamodel.facets.object.introspection.IntrospectionPolicyFacet;
import org.apache.causeway.core.metamodel.facets.object.title.TitleFacet;
import org.apache.causeway.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.causeway.core.metamodel.facets.object.viewmodel.ViewModelFacetForJavaRecord;
import org.apache.causeway.core.metamodel.facets.objectvalue.mandatory.MandatoryFacet;
import org.apache.causeway.core.metamodel.facets.objectvalue.mandatory.MandatoryFacet.Semantics;
import org.apache.causeway.core.metamodel.facets.param.choices.ActionParameterChoicesFacet;
import org.apache.causeway.core.metamodel.facets.param.choices.ActionParameterChoicesFacetFromAction;
import org.apache.causeway.core.metamodel.facets.param.choices.ActionParameterChoicesFacetFromElementType;
import org.apache.causeway.core.metamodel.facets.param.choices.methodnum.ActionParameterChoicesFacetViaMethod;
import org.apache.causeway.core.metamodel.facets.param.defaults.ActionParameterDefaultsFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.postprocessors.param.ActionParameterDefaultsFacetFromAssociatedCollection;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.core.metamodel.tabular.simple.DataTable;
import org.apache.causeway.schema.metamodel.v2.DomainClassDto;
import org.apache.causeway.testdomain.conf.Configuration_headless;
import org.apache.causeway.testdomain.model.good.Configuration_usingValidDomain;
import org.apache.causeway.testdomain.model.good.ElementTypeAbstract;
import org.apache.causeway.testdomain.model.good.ElementTypeConcrete;
import org.apache.causeway.testdomain.model.good.ElementTypeInterface;
import org.apache.causeway.testdomain.model.good.ProperActionParamterBoundingWhenUsingEnum;
import org.apache.causeway.testdomain.model.good.ProperChoicesWhenActionHasParamSupportingMethodTypeOfReference;
import org.apache.causeway.testdomain.model.good.ProperChoicesWhenActionHasParamSupportingMethodTypeOfString;
import org.apache.causeway.testdomain.model.good.ProperChoicesWhenChoicesFrom;
import org.apache.causeway.testdomain.model.good.ProperElementTypeVm;
import org.apache.causeway.testdomain.model.good.ProperFullyImpl;
import org.apache.causeway.testdomain.model.good.ProperGenericAbstract;
import org.apache.causeway.testdomain.model.good.ProperGenericInterface;
import org.apache.causeway.testdomain.model.good.ProperInterface2;
import org.apache.causeway.testdomain.model.good.ProperMemberInheritanceInterface;
import org.apache.causeway.testdomain.model.good.ProperMemberInheritanceUsingAbstract;
import org.apache.causeway.testdomain.model.good.ProperMemberInheritanceUsingInterface;
import org.apache.causeway.testdomain.model.good.ProperMemberSupport;
import org.apache.causeway.testdomain.model.good.ProperMemberSupportDiscovery;
import org.apache.causeway.testdomain.model.good.ProperMixinContribution;
import org.apache.causeway.testdomain.model.good.ProperMixinContribution_action1;
import org.apache.causeway.testdomain.model.good.ProperMixinContribution_action1a;
import org.apache.causeway.testdomain.model.good.ProperMixinContribution_action1b;
import org.apache.causeway.testdomain.model.good.ProperMixinContribution_action2;
import org.apache.causeway.testdomain.model.good.ProperMixinContribution_action3;
import org.apache.causeway.testdomain.model.good.ProperMixinContribution_action4;
import org.apache.causeway.testdomain.model.good.ProperMixinContribution_action5;
import org.apache.causeway.testdomain.model.good.ProperMixinContribution_action6;
import org.apache.causeway.testdomain.model.good.ProperObjectWithAlias;
import org.apache.causeway.testdomain.model.good.ProperRecordAsViewModel;
import org.apache.causeway.testdomain.model.good.ProperRecordAsViewModelUsingAnnotations;
import org.apache.causeway.testdomain.model.good.ProperServiceWithAlias;
import org.apache.causeway.testdomain.model.good.ProperServiceWithMixin;
import org.apache.causeway.testdomain.model.good.ProperViewModelInferredFromNotBeingAnEntity;
import org.apache.causeway.testdomain.model.good.ViewModelWithAnnotationOptionalUsingPrivateSupport;
import org.apache.causeway.testdomain.model.good.ViewModelWithEncapsulatedMembers;
import org.apache.causeway.testdomain.util.interaction.DomainObjectTesterFactory;
import org.apache.causeway.testing.integtestsupport.applib.CausewayIntegrationTestAbstract;
import org.apache.causeway.testing.integtestsupport.applib.validate.DomainModelValidator;

import lombok.RequiredArgsConstructor;

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
class DomainModelTest_usingGoodDomain extends CausewayIntegrationTestAbstract {

    @Inject private MetaModelService metaModelService;
    @Inject private JaxbService jaxbService;
    @Inject private ServiceRegistry serviceRegistry;
    @Inject private SpecificationLoader specificationLoader;
    @Inject private TitleService titleService;
    @Inject private DomainObjectTesterFactory testerFactory;

    void debug() {
        var config = Config.builder().build()
                .withNamespacePrefix("org.apache.causeway.testdomain.");

        System.out.println("=== listing MM");
        var metamodelDto = metaModelService.exportMetaModel(config);
        for (DomainClassDto domainClass : metamodelDto.getDomainClassDto()) {
            System.out.println("dc: " + domainClass.getId());
            var xmlString = jaxbService.toXml(domainClass);
            System.out.println(xmlString);
        }
        System.out.println("==============");
    }

    @Test
    void goodDomain_shouldPassValidation() {
        //debug();
        assertFalse(specificationLoader.snapshotSpecifications().isEmpty());

        var validateDomainModel = new DomainModelValidator(serviceRegistry);

        // guard against left overs from shared context
        var validationFailures = validateDomainModel.getFailures().stream()
                .filter(f->!f.getOrigin().className().contains("bad"))
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

        var tester = testerFactory.actionTester(ProperMemberSupport.class, "hideMe");
        tester.assertExists(true);
        tester.assertMemberId("hideMe");
    }

    @Test
    void typeLevelAnnotations_shouldBeHonored_onMixins() {

        var holderSpec = specificationLoader.specForTypeElseFail(ProperMemberSupport.class);

        var mx_action = holderSpec.getActionElseFail("action1"); // when @Action at type level
        assertNotNull(mx_action);
        assertEquals("action1", mx_action.getId());
        assertEquals("foo", mx_action.getStaticFriendlyName().get());
        assertEquals("bar", mx_action.getStaticDescription().get());
        assertHasPublishedActionFacet(mx_action);

        var mx_action2 = holderSpec.getActionElseFail("action2"); // proper mixed-in action support
        assertNotNull(mx_action2);
        assertHasPublishedActionFacet(mx_action2);

        var mx_property = holderSpec.getAssociationElseFail("property1"); // when @Property at type level
        assertNotNull(mx_property);
        assertEquals("property1", mx_property.getId());
        assertEquals("foo", mx_property.getStaticFriendlyName().get());
        assertEquals("bar", mx_property.getStaticDescription().get());

        var mx_property2 = holderSpec.getAssociationElseFail("property2"); // when @Property at method level
        assertNotNull(mx_property2);
        assertEquals("property2", mx_property2.getId());
        assertEquals("foo", mx_property2.getStaticFriendlyName().get());
        assertEquals("bar", mx_property2.getStaticDescription().get());

        var mx_collection = holderSpec.getAssociationElseFail("collection1"); // when @Collection at type level
        assertNotNull(mx_collection);
        assertEquals("collection1", mx_collection.getId());
        assertEquals("foo", mx_collection.getStaticFriendlyName().get());
        assertEquals("bar", mx_collection.getStaticDescription().get());

        var mx_collection2 = holderSpec.getAssociationElseFail("collection2"); // when @Collection at method level
        assertNotNull(mx_collection2);
        assertEquals("collection2", mx_collection2.getId());
        assertEquals("foo", mx_collection2.getStaticFriendlyName().get());
        assertEquals("bar", mx_collection2.getStaticDescription().get());

    }

    @Test
    void memberLevelAnnotations_shouldResolveUnambiguous_onMixins() {

        var holderSpec = specificationLoader.specForTypeElseFail(ProperMemberSupport.class);

        var mx_openRestApi = holderSpec.getDeclaredAction("openRestApi"); // built-in mixin support
        assertNotNull(mx_openRestApi);

        assertThrows(Exception.class, ()->holderSpec.getAssociationElseFail("openRestApi")); // should not be picked up as a property

    }

    @Test
    void fullyAbstractObject_whenImplemented_shouldBeSupported() {
        var tester = testerFactory.objectTester(ProperFullyImpl.class);
        tester.assertTitle("title");
        tester.assertIcon("icon");
        tester.assertCssClass("css");
        tester.assertLayout("layout");
    }

    @ParameterizedTest
    @ValueSource(classes = {
            ProperGenericInterface.Impl.class,
            ProperGenericAbstract.Impl.class,
    })
    void genericBaseType_whenImplemented_shouldBeSupported(final Class<?> classUnderTest) {
        var propTester = testerFactory.propertyTester(classUnderTest, "value");
        propTester.assertExists(true);
        propTester.assertValue("aValue");

        var actTester = testerFactory.actionTester(classUnderTest, "sampleAction");
        actTester.assertExists(true);
        actTester.assertInvocationResult("aValue", arg0->"aValue");

        // assert that we properly pickup the @Nullable annotation on arg-0
        var argMeta = actTester.getActionMetaModelElseFail().getParameterByIndex(0);
        var argMandatoryFacet = argMeta.lookupFacet(MandatoryFacet.class)
            .orElseThrow(()->_Exceptions.unrecoverable("missing MandatoryFacet on action parameter"));
        assertEquals(Semantics.OPTIONAL, argMandatoryFacet.getSemantics());
        actTester.assertInvocationResult(null, arg0->null);
    }

    @ParameterizedTest
    @MethodSource("provideProperMemberInheritanceTypes")
    void titleAndIconName_shouldBeInheritable(final Class<?> type) throws Exception {

        var spec = specificationLoader.specForTypeElseFail(type);

        var titleFacet = spec.getFacet(TitleFacet.class);
        assertNotNull(titleFacet);

        var iconFacet = spec.getFacet(IconFacet.class);
        assertNotNull(iconFacet);

        if(!spec.isAbstract()) {
            var instance = type.getDeclaredConstructor().newInstance();
            assertEquals("inherited title", titleService.titleOf(instance));
            assertEquals("inherited icon", titleService.iconNameOf(instance));

            var domainObject = ManagedObject.adaptSingular(spec, instance);
            assertEquals("inherited title", domainObject.getTitle());
            assertEquals("inherited icon", iconFacet.iconName(domainObject));
        }
    }

    @ParameterizedTest
    @MethodSource("provideProperMemberInheritanceTypes")
    void metamodelContributingMembers_shouldBeInheritable(final Class<?> type) {

        var holderSpec = specificationLoader.specForTypeElseFail(type);

        var action = holderSpec.getActionElseFail("sampleAction");
        assertNotNull(action);
        assertEquals("sampleAction", action.getId());
        assertEquals("foo", action.getStaticFriendlyName().get());
        assertEquals("bar", action.getStaticDescription().get());

        var property = holderSpec.getAssociationElseFail("sampleProperty");
        assertNotNull(property);
        assertEquals("sampleProperty", property.getId());
        assertEquals("foo", property.getStaticFriendlyName().get());
        assertEquals("bar", property.getStaticDescription().get());

        var collection = holderSpec.getAssociationElseFail("sampleCollection");
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

        var holderSpec = specificationLoader.specForTypeElseFail(type);

        var super_action = holderSpec.getActionElseFail("sampleActionOverride");
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

        var holderSpec = specificationLoader.specForTypeElseFail(type);

        var super_property = holderSpec.getAssociationElseFail("samplePropertyOverride");
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

        var vmSpec = specificationLoader.specForTypeElseFail(ProperElementTypeVm.class);

        var concreteColl = vmSpec.getCollectionElseFail("concreteColl");
        var concreteCollSpec = concreteColl.getElementType();

        assertEquals(ElementTypeConcrete.class, concreteCollSpec.getCorrespondingClass());
        assertEquals(BeanSort.VIEW_MODEL, concreteCollSpec.getBeanSort());
        assertHasAction(concreteCollSpec, "abstractAction");
        assertHasAction(concreteCollSpec, "interfaceAction");
        assertHasProperty(concreteCollSpec, "abstractProp");
        assertHasProperty(concreteCollSpec, "interfaceProp");

        var interfaceColl = vmSpec.getCollectionElseFail("interfaceColl");
        var interfaceCollSpec = interfaceColl.getElementType();

        assertEquals(ElementTypeInterface.class, interfaceCollSpec.getCorrespondingClass());
        assertEquals(BeanSort.ABSTRACT, interfaceCollSpec.getBeanSort());
        assertHasAction(interfaceCollSpec, "interfaceAction");
        assertHasProperty(interfaceCollSpec, "interfaceProp");

        var abstractColl = vmSpec.getCollectionElseFail("abstractColl");
        var abstractCollSpec = abstractColl.getElementType();

        assertEquals(ElementTypeAbstract.class, abstractCollSpec.getCorrespondingClass());
        assertEquals(BeanSort.ABSTRACT, abstractCollSpec.getBeanSort());
        assertHasAction(abstractCollSpec, "abstractAction");
        assertHasProperty(abstractCollSpec, "abstractProp");
    }

    @Test
    void elementTypeInference_fromGenerics_usingNoWildcards() {

        // when using generic type (no wild-cards)

        var vmSpec = specificationLoader.specForTypeElseFail(ProperElementTypeVm.class);

        // scenario 1

        //abstract Set<ElementTypeInterface> getSetOfInterfaceType();
        //override SortedSet<ElementTypeInterface> getSetOfInterfaceType();

        var interfaceSet = vmSpec.getCollectionElseFail("setOfInterfaceType");
        var interfaceSetSpec = interfaceSet.getElementType();

        assertEquals(ElementTypeInterface.class, interfaceSetSpec.getCorrespondingClass());
        assertEquals(BeanSort.ABSTRACT, interfaceSetSpec.getBeanSort());

        //TODO scenario 2

        //abstract ImmutableCollection<ElementTypeInterface> getImmutableOfInterfaceType();
        //override Can<ElementTypeInterface> getImmutableOfInterfaceType();

        var interfaceIterLookup = vmSpec.getAssociationElseFail("immutableOfInterfaceType");
        assertTrue(interfaceIterLookup.isOneToManyAssociation(), "required to be a coll");

        var interfaceIter = vmSpec.getCollectionElseFail("immutableOfInterfaceType");
        var interfaceIterSpec = interfaceIter.getElementType();

        assertEquals(ElementTypeInterface.class, interfaceIterSpec.getCorrespondingClass());
        assertEquals(BeanSort.ABSTRACT, interfaceIterSpec.getBeanSort());

    }

    @Test
    void elementTypes_shouldBeIntrospected_whenDiscoveredViaGenerics_usingWildcards() {

        // when using generic type (w/ wild-cards)

        var vmSpec = specificationLoader.specForTypeElseFail(ProperElementTypeVm.class);

        var concreteColl = vmSpec.getCollectionElseFail("concreteColl2");
        var concreteCollSpec = concreteColl.getElementType();

        assertEquals(ElementTypeConcrete.class, concreteCollSpec.getCorrespondingClass());
        assertEquals(BeanSort.VIEW_MODEL, concreteCollSpec.getBeanSort());
        assertHasAction(concreteCollSpec, "abstractAction");
        assertHasAction(concreteCollSpec, "interfaceAction");
        assertHasProperty(concreteCollSpec, "abstractProp");
        assertHasProperty(concreteCollSpec, "interfaceProp");

        var interfaceColl = vmSpec.getCollectionElseFail("interfaceColl2");
        var interfaceCollSpec = interfaceColl.getElementType();

        assertEquals(ElementTypeInterface.class, interfaceCollSpec.getCorrespondingClass());
        assertEquals(BeanSort.ABSTRACT, interfaceCollSpec.getBeanSort());
        assertHasAction(interfaceCollSpec, "interfaceAction");
        assertHasProperty(interfaceCollSpec, "interfaceProp");

        var abstractColl = vmSpec.getCollectionElseFail("abstractColl2");
        var abstractCollSpec = abstractColl.getElementType();

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
        var vmSpec = specificationLoader.specForTypeElseFail(ProperViewModelInferredFromNotBeingAnEntity.class);

        assertEquals(BeanSort.VIEW_MODEL, vmSpec.getBeanSort());
        assertNotNull(vmSpec.getFacet(ViewModelFacet.class));
    }

    @Test
    void interfaces_shouldSupport_inheritedMembers() {

        var i2Spec = specificationLoader.specForTypeElseFail(ProperInterface2.class);

        assertEquals(BeanSort.ABSTRACT, i2Spec.getBeanSort());
        assertHasProperty(i2Spec, "a");
        assertHasProperty(i2Spec, "b");
        assertHasProperty(i2Spec, "c");
        assertHasProperty(i2Spec, "d");
        assertHasProperty(i2Spec, "e");
        assertHasProperty(i2Spec, "f");
    }

    @SuppressWarnings("unchecked")
    @Test
    void actionParamChoices_shouldBeImplicitlyBounded_whenEnum() {

        var spec = specificationLoader.specForTypeElseFail(ProperActionParamterBoundingWhenUsingEnum.class);

        var action = spec.getActionElseFail("sampleAction");
        var param0 = action.getParameters().getFirstElseFail();

        assertEquals(
                ActionParameterChoicesFacetFromElementType.class,
                param0.lookupFacet(ActionParameterChoicesFacet.class)
                    .map(Object::getClass)
                    .orElse(null));

        var act = testerFactory
                .actionTester(ProperActionParamterBoundingWhenUsingEnum.class, "sampleAction");

        var expectedParamChoices = Can.ofArray(
                ProperActionParamterBoundingWhenUsingEnum.SampleEnum.values());

        act.assertParameterChoices(true, ProperActionParamterBoundingWhenUsingEnum.SampleEnum.class,
                choices0->assertEquals(expectedParamChoices, Can.ofIterable(choices0), ()->"param 0 choices mismatch"));
    }

    @Test
    void actionParamChoices_shouldBeAllowed_toBeDerivedFromChoicesFrom() {

        var spec = specificationLoader.specForTypeElseFail(ProperChoicesWhenChoicesFrom.class);

        var action = spec.getActionElseFail("appendACharacterToCandidates");
        var param0 = action.getParameters().getFirstElseFail();

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

        var spec = specificationLoader.specForTypeElseFail(ProperChoicesWhenActionHasParamSupportingMethodTypeOfString.class);

        var action = spec.getActionElseFail("remove");
        var param0 = action.getParameters().getFirstElseFail();

        assertEquals(
                ActionParameterChoicesFacetViaMethod.class,
                param0.lookupFacet(ActionParameterChoicesFacet.class)
                    .map(Object::getClass)
                    .orElse(null));
    }

    @Test
    void actionParamChoices_shouldBeAvailable_whenMixedInActionHasParamSupportingMethodTypeOfReference() {

        var spec = specificationLoader
                .specForTypeElseFail(ProperChoicesWhenActionHasParamSupportingMethodTypeOfReference.class);

        var action = spec.getActionElseFail("remove");
        var param0 = action.getParameters().getFirstElseFail();

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

        var actTester = testerFactory
                .actionTester(ProperMemberSupport.class, "action6");
        actTester.assertExists(true);

        // low-level MM inspection
        {
            var action = actTester.getActionMetaModelElseFail();
            action.getParameters()
            .forEach(param->{

               assertTrue(param.getAction().isMixedIn(), ()->String.format(
                       "param %d is expected to belong to a mixed-in action",
                       param.getParameterIndex()));

               var choicesFacet = param.getFacet(ActionParameterChoicesFacet.class);

               assertNotNull(choicesFacet, ()->String.format(
                       "param %d is expected to have an ActionParameterChoicesFacet",
                       param.getParameterIndex()));
            });
        }

        var mixee = actTester.getActionOwnerAs(ProperMemberSupport.class);
        mixee.setMyColl(List.of(
                "Hallo",
                "World"));

        var expectedParamChoices = Can.of(
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

        var objectSpec = specificationLoader.specForTypeElseFail(ProperMemberSupport.class);
        var member = objectSpec.getMemberElseFail(memberId);
        var sampleObject = ManagedObject.adaptSingular(objectSpec, new ProperMemberSupport());

        assertEquals(named, member.getFriendlyName(()->sampleObject));
        assertEquals(described, member.getDescription(()->sampleObject).orElse(null));
    }

    @Test
    void mixinsOnDomainServices_shouldBeAllowed() {

        var objectSpec = specificationLoader.specForTypeElseFail(ProperServiceWithMixin.class);

        var mixinSpec = specificationLoader.specForTypeElseFail(ProperServiceWithMixin.Now.class);

        assertTrue(mixinSpec.isMixin());

        assertEquals(
                1L,
                objectSpec.streamRuntimeActions(MixedIn.INCLUDED)
                .filter(ObjectAction::isMixedIn)
                .peek(act->{
                    //System.out.println("act: " + act);
                    var memberNamedFacet = act.getFacet(MemberNamedFacet.class);
                    assertNotNull(memberNamedFacet);
                    assertTrue(memberNamedFacet.getSpecialization().isLeft());
                })
                .count());

    }

    @Test
    void aliasesOnDomainServices_shouldBeHonored() {

        var objectSpec = specificationLoader.specForTypeElseFail(ProperServiceWithAlias.class);
        assertTrue(objectSpec.isInjectable());
        assertTrue(objectSpec.getAction("now").isPresent());

        assertEquals(Can.of(
                "testdomain.v1.ProperServiceWithAlias",
                "testdomain.v2.ProperServiceWithAlias"),
                objectSpec.getAliases().map(LogicalType::logicalName));

        assertEquals(objectSpec,
                specificationLoader.specForLogicalTypeName("testdomain.v1.ProperServiceWithAlias")
                .orElse(null));
        assertEquals(objectSpec,
                specificationLoader.specForLogicalTypeName("testdomain.v2.ProperServiceWithAlias")
                .orElse(null));
    }

    @Test
    void aliasesOnDomainObjects_shouldBeHonored() {

        var objectSpec = specificationLoader.specForTypeElseFail(ProperObjectWithAlias.class);
        assertTrue(objectSpec.isViewModel());
        assertTrue(objectSpec.getAction("now").isPresent());

        assertEquals(Can.of(
                "testdomain.v1.ProperObjectWithAlias",
                "testdomain.v2.ProperObjectWithAlias"),
                objectSpec.getAliases().map(LogicalType::logicalName));

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

        var objectSpec = specificationLoader.specForTypeElseFail(ViewModelWithEncapsulatedMembers.class);

        var introspectionPolicyFacet = objectSpec.getFacet(IntrospectionPolicyFacet.class);
        assertNotNull(introspectionPolicyFacet);

        var introspectionPolicy = introspectionPolicyFacet.getIntrospectionPolicy();
        assertEquals(
                EncapsulationPolicy.ENCAPSULATED_MEMBERS_SUPPORTED,
                introspectionPolicy.getEncapsulationPolicy());
        assertEquals(
                MemberAnnotationPolicy.MEMBER_ANNOTATIONS_REQUIRED,
                introspectionPolicy.getMemberAnnotationPolicy());

        // PRIVATE ACTION

        var act = testerFactory
                .actionTester(ViewModelWithEncapsulatedMembers.class, "myAction");
        act.assertExists(true);
        act.assertIsExplicitlyAnnotated(true);
        act.assertVisibilityIsNotVetoed();
        act.assertUsabilityIsVetoedWith("action disabled for testing purposes");
        act.assertInvocationResultNoRules("Hallo World!");

        // -- PROPERTY WITH PRIVATE GETTER AND SETTER

        var prop = testerFactory
                .propertyTester(ViewModelWithEncapsulatedMembers.class, "propWithPrivateAccessors");
        prop.assertExists(true);
        prop.assertIsExplicitlyAnnotated(true);
        prop.assertVisibilityIsNotVetoed();
        prop.assertUsabilityIsVetoedWith("property disabled for testing purposes");
        prop.assertValue("Foo");
        prop.assertValueUpdate("Bar");

        // -- COLLECTION WITH PRIVATE GETTER AND SETTER

        var coll = testerFactory
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

        var objectSpec = specificationLoader.specForTypeElseFail(ViewModelWithAnnotationOptionalUsingPrivateSupport.class);

        var introspectionPolicyFacet = objectSpec.getFacet(IntrospectionPolicyFacet.class);
        assertNotNull(introspectionPolicyFacet);

        var introspectionPolicy = introspectionPolicyFacet.getIntrospectionPolicy();
        assertEquals(
                EncapsulationPolicy.ONLY_PUBLIC_MEMBERS_SUPPORTED,
                introspectionPolicy.getEncapsulationPolicy());
        assertEquals(
                MemberAnnotationPolicy.MEMBER_ANNOTATIONS_OPTIONAL,
                introspectionPolicy.getMemberAnnotationPolicy());

        // PRIVATE ACTION

        var act = testerFactory
                .actionTester(ViewModelWithAnnotationOptionalUsingPrivateSupport.class, "myAction");
        act.assertExists(true);
        act.assertIsExplicitlyAnnotated(true);
        act.assertVisibilityIsNotVetoed();
        act.assertUsabilityIsVetoedWithAll(
                Can.of("object disabled for testing purposes", "action disabled for testing purposes"));
        act.assertInvocationResultNoRules("Hallo World!");

        // -- PROPERTY WITH PRIVATE GETTER AND SETTER

        var prop = testerFactory
                .propertyTester(ViewModelWithAnnotationOptionalUsingPrivateSupport.class, "propWithPrivateAccessors");
        prop.assertExists(true);
        prop.assertIsExplicitlyAnnotated(true);
        prop.assertVisibilityIsNotVetoed();
        prop.assertUsabilityIsVetoedWithAll(
                Can.of("object disabled for testing purposes", "property disabled for testing purposes"));
        prop.assertValue("Foo");
        prop.assertValueUpdate("Bar");

        // -- COLLECTION WITH PRIVATE GETTER AND SETTER

        var coll = testerFactory
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

        var act = testerFactory
                .actionTester(classUnderTest, "placeOrder");
        var prop = testerFactory
                .propertyTester(classUnderTest, "email");
        var coll = testerFactory
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

                    var actualChoices =
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

                    var actualChoices =
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
            ProperMixinContribution_action1a.class, // relaxed inclusion policy for mixin main
            ProperMixinContribution_action1b.class, // relaxed inclusion policy for mixin main
            ProperMixinContribution_action2.class,
            ProperMixinContribution_action3.class,
            ProperMixinContribution_action4.class,
            ProperMixinContribution_action5.class,
            ProperMixinContribution_action6.class})
    void mixins_shouldBePickedUp_asTheRightContributingFeature(final Class<?> mixinClass) {

        final String actionName = _Strings.splitThenStream(mixinClass.getSimpleName(), "_")
                .reduce((a, b)->b)
                .orElseThrow();

        var vmSpec = specificationLoader.specForTypeElseFail(ProperMixinContribution.class);
        assertHasAction(vmSpec, "myAction"); // regular action (just a sanity check)
        assertHasAction(vmSpec, actionName); // contributed action
        assertMissesProperty(vmSpec, actionName); // verify don't contributes as property
    }

    // -- JAVA RECORD AS VIEWMODEL

    @RequiredArgsConstructor
    enum RecordScenario {
        PLAIN(ProperRecordAsViewModel.class, Can.of(
                    new ProperRecordAsViewModel("Hello!", 3, true)
                )),
        ANNOTATED(ProperRecordAsViewModelUsingAnnotations.class, Can.of(
                    new ProperRecordAsViewModelUsingAnnotations("Hello!", 3, true)
                ));
        final Class<?> recordClass;
        final Can<?> samples;
        final String classFriendlyName() {
            return _Strings.asNaturalName.apply(recordClass.getSimpleName());
        }
    }

    @ParameterizedTest
    @EnumSource(RecordScenario.class)
    void javaRecordAsViewModel(final RecordScenario scenario) {
        final Class<?> classUnderTest = scenario.recordClass;
        final Object sample = scenario.samples.getFirstElseFail();
        var viewModel = MetaModelContext.instanceElseFail().getObjectManager().adapt(sample);
        var elementType = viewModel.getSpecification();
        var viewmodelFacet = elementType.getFacet(ViewModelFacet.class);

        assertEquals(BeanSort.VIEW_MODEL, elementType.getBeanSort());
        assertEquals(classUnderTest.getName(), elementType.getFeatureIdentifier().logicalTypeName());
        assertTrue(ViewModelFacetForJavaRecord.class.isInstance(viewmodelFacet),
                ()->"Record is expected to have a ViewModelFacetForJavaRecord");

        var bookmark = viewmodelFacet.serializeToBookmark(viewModel);
        var viewModelAfterRoundTrip = viewmodelFacet.instantiate(elementType, Optional.of(bookmark));
        assertEquals(viewModel.getPojo(), viewModelAfterRoundTrip.getPojo());

        var isExpectedExplicitlyAnnotated = scenario == RecordScenario.ANNOTATED;

        var additionalString = testerFactory
                .propertyTester(sample, "additionalString");
        additionalString.assertExists(true);
        additionalString.assertVisibilityIsNotVetoed();
        additionalString.assertUsabilityIsVetoedWith("Disabled, property has no setter.");
        additionalString.assertIsExplicitlyAnnotated(isExpectedExplicitlyAnnotated);
        additionalString.assertValue("add Hello!");

        var arbitraryString = testerFactory
                .propertyTester(sample, "arbitraryString");
        arbitraryString.assertExists(true);
        arbitraryString.assertVisibilityIsNotVetoed();
        arbitraryString.assertUsabilityIsVetoedWith("Disabled, property has no setter.");
        arbitraryString.assertIsExplicitlyAnnotated(isExpectedExplicitlyAnnotated);
        arbitraryString.assertValue("Hello!");

        var arbitraryInt = testerFactory
                .propertyTester(sample, "arbitraryInt");
        arbitraryInt.assertExists(true);
        arbitraryInt.assertVisibilityIsNotVetoed();
        arbitraryInt.assertUsabilityIsVetoedWith("Disabled, property has no setter.");
        arbitraryInt.assertIsExplicitlyAnnotated(isExpectedExplicitlyAnnotated);
        arbitraryInt.assertValue(3);

        var arbitraryBoolean = testerFactory
                .propertyTester(sample, "arbitraryBoolean");
        arbitraryBoolean.assertExists(true);
        arbitraryBoolean.assertVisibilityIsNotVetoed();
        arbitraryBoolean.assertUsabilityIsVetoedWith("Disabled, property has no setter.");
        arbitraryBoolean.assertIsExplicitlyAnnotated(isExpectedExplicitlyAnnotated);
        arbitraryBoolean.assertValue(true);
    }

    @ParameterizedTest
    @EnumSource(RecordScenario.class)
    void javaRecordAsDataRowModel(final RecordScenario scenario) {
        final Class<?> classUnderTest = scenario.recordClass;

        var dataTable = DataTable.forDomainType(classUnderTest)
            .withDataElementPojos(scenario.samples);

        assertEquals(scenario.classFriendlyName(), dataTable.tableFriendlyName());
        assertEquals(scenario.samples.size(), dataTable.dataRows().size());
        assertEquals(4, dataTable.dataColumns().size());

        assertEquals("""
                Additional String: add Hello!
                Arbitrary Boolean: true
                Arbitrary Int: 3
                Arbitrary String: Hello!
                """, tableToString(dataTable));
    }

    // -- META ANNOTAIONS

    @Test
    void propMeta_shouldHonorSpecification() {
        var propTester = testerFactory
                .propertyTester(ProperMemberSupport.class, "myPropWithMeta");
        propTester.assertExists(true);
        propTester.assertFriendlyName("Proper Name");
        propTester.assertValueUpdateUsingNegotiationTextual("just a string", "injection worked");
    }

    @Test
    void actionParamMeta_shouldHonorSpecification() {
        var actTester = testerFactory
                .actionTester(ProperMemberSupport.class, "myActionWithMetaOnParam");
        actTester.assertExists(true);
        //TODO[CAUSEWAY-3326] perhaps tester needs fixing?
        //actTester.assertValidationMessage("injection worked", true, p0->"just a string");

    }

    // -- HELPER

    private String tableToString(final DataTable dataTable) {
        var sb = new StringBuilder();
        dataTable.visit((column, cellValues) ->
            sb.append(String.format("%s: %s\n",
                    column.columnFriendlyName(),
                    "" + cellValues.getFirstElseFail().getPojo())));
        return sb.toString();
    }

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
        var facet = facetHolder.getFacet(ExecutionPublishingFacet.class);
        assertNotNull(facet);
    }

    static Stream<Arguments> provideProperMemberInheritanceTypes() {
        return Stream.of(
                Arguments.of(ProperMemberInheritanceUsingAbstract.class),
                Arguments.of(ProperMemberInheritanceUsingInterface.class),
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
