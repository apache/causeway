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

import java.util.stream.Stream;

import javax.inject.Inject;

import org.junit.jupiter.api.Disabled;
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

import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.services.metamodel.BeanSort;
import org.apache.isis.applib.services.metamodel.Config;
import org.apache.isis.applib.services.metamodel.MetaModelService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.title.TitleService;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.members.publish.execution.ExecutionPublishingFacet;
import org.apache.isis.core.metamodel.facets.object.icon.IconFacet;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.specimpl.IntrospectionState;
import org.apache.isis.schema.metamodel.v2.DomainClassDto;
import org.apache.isis.testdomain.conf.Configuration_headless;
import org.apache.isis.testdomain.model.good.Configuration_usingValidDomain;
import org.apache.isis.testdomain.model.good.ElementTypeConcrete;
import org.apache.isis.testdomain.model.good.ElementTypeInterface;
import org.apache.isis.testdomain.model.good.ProperElementTypeVm;
import org.apache.isis.testdomain.model.good.ProperMemberInheritanceInterface;
import org.apache.isis.testdomain.model.good.ProperMemberInheritance_usingAbstract;
import org.apache.isis.testdomain.model.good.ProperMemberInheritance_usingInterface;
import org.apache.isis.testdomain.model.good.ProperMemberSupport;
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
//    @Inject private FactoryService factoryService;
    @Inject private SpecificationLoader specificationLoader;
    @Inject private TitleService titleService;

    void debug() {
        val config = new Config()
//              .withIgnoreNoop()
//              .withIgnoreAbstractClasses()
//              .withIgnoreBuiltInValueTypes()
//              .withIgnoreInterfaces()
                //.withPackagePrefix("*")
                .withPackagePrefix("org.apache.isis.testdomain.")
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
     
        val holderSpec = specificationLoader.loadSpecification(ProperMemberSupport.class,
                IntrospectionState.FULLY_INTROSPECTED);
        
        val prefixed_action = holderSpec.getActionElseFail("hideMe");
        assertNotNull(prefixed_action);
        assertEquals("hideMe", prefixed_action.getId());
    }

    @Test
    void typeLevelAnnotations_shouldBeHonored_onMixins() {

        val holderSpec = specificationLoader.loadSpecification(ProperMemberSupport.class,
                        IntrospectionState.FULLY_INTROSPECTED);

        val mx_action = holderSpec.getActionElseFail("action"); // when @Action at type level
        assertNotNull(mx_action);
        assertEquals("action", mx_action.getId());
        assertEquals("foo", mx_action.getName());
        assertEquals("bar", mx_action.getDescription());
        assertHasPublishedActionFacet(mx_action);

        val mx_action2 = holderSpec.getActionElseFail("action2"); // proper mixed-in action support
        assertNotNull(mx_action2);
        assertHasPublishedActionFacet(mx_action2);

        val mx_property = holderSpec.getAssociationElseFail("property"); // when @Property at type level
        assertNotNull(mx_property);
        assertEquals("property", mx_property.getId());
        assertEquals("foo", mx_property.getName());
        assertEquals("bar", mx_property.getDescription());

        val mx_property2 = holderSpec.getAssociationElseFail("property2"); // when @Property at method level
        assertNotNull(mx_property2);
        assertEquals("property2", mx_property2.getId());
        assertEquals("foo", mx_property2.getName());
        assertEquals("bar", mx_property2.getDescription());

        val mx_collection = holderSpec.getAssociationElseFail("collection"); // when @Collection at type level
        assertNotNull(mx_collection);
        assertEquals("collection", mx_collection.getId());
        assertEquals("foo", mx_collection.getName());
        assertEquals("bar", mx_collection.getDescription());

        val mx_collection2 = holderSpec.getAssociationElseFail("collection2"); // when @Collection at method level
        assertNotNull(mx_collection2);
        assertEquals("collection2", mx_collection2.getId());
        assertEquals("foo", mx_collection2.getName());
        assertEquals("bar", mx_collection2.getDescription());

    }

    @Test
    void memberLevelAnnotations_shouldResolveUnambiguous_onMixins() {

        val holderSpec = specificationLoader.loadSpecification(ProperMemberSupport.class);

        val mx_openRestApi = holderSpec.getDeclaredAction("openRestApi"); // built-in mixin support
        assertNotNull(mx_openRestApi);

        assertThrows(Exception.class, ()->holderSpec.getAssociationElseFail("openRestApi")); // should not be picked up as a property

    }

    @ParameterizedTest
    @MethodSource("provideProperMemberInheritanceTypes")
    void titleAndIconName_shouldBeInheritable(Class<?> type) {

        val spec = specificationLoader.loadSpecification(type,
                        IntrospectionState.FULLY_INTROSPECTED);

        val titleFacet = spec.getFacet(TitleFacet.class);
        assertNotNull(titleFacet);

        val iconFacet = spec.getFacet(IconFacet.class);
        assertNotNull(iconFacet);

        val properMemberInheritance = new ProperMemberInheritance_usingAbstract();
        assertEquals(properMemberInheritance.title(), titleService.titleOf(properMemberInheritance));
        assertEquals(properMemberInheritance.iconName(), titleService.iconNameOf(properMemberInheritance));
    }

    @ParameterizedTest
    @MethodSource("provideProperMemberInheritanceTypes")
    void metamodelContributingMembers_shouldBeInheritable(Class<?> type) {

        val holderSpec = specificationLoader.loadSpecification(type,
                        IntrospectionState.FULLY_INTROSPECTED);

        val action = holderSpec.getActionElseFail("sampleAction");
        assertNotNull(action);
        assertEquals("sampleAction", action.getId());
        assertEquals("foo", action.getName());
        assertEquals("bar", action.getDescription());

        val property = holderSpec.getAssociationElseFail("sampleProperty");
        assertNotNull(property);
        assertEquals("sampleProperty", property.getId());
        assertEquals("foo", property.getName());
        assertEquals("bar", property.getDescription());

        val collection = holderSpec.getAssociationElseFail("sampleCollection");
        assertNotNull(collection);
        assertEquals("sampleCollection", collection.getId());
        assertEquals("foo", collection.getName());
        assertEquals("bar", collection.getDescription());

    }

    @ParameterizedTest
    @MethodSource("provideProperMemberInheritanceTypes")
    void metamodelContributingActions_shouldBeUnique_whenOverridden(Class<?> type) {

        if(type.isInterface()
                && type.getSuperclass()==null) {
            return; // not implemented for interface that don't extend from others
        }

        val holderSpec = specificationLoader.loadSpecification(type,
                IntrospectionState.FULLY_INTROSPECTED);

        val super_action = holderSpec.getActionElseFail("sampleActionOverride");
        assertNotNull(super_action);
        assertEquals("sampleActionOverride", super_action.getId());
        assertEquals("foo", super_action.getName());
        assertEquals("bar", super_action.getDescription());

        assertEquals(1L, holderSpec.streamActions(MixedIn.EXCLUDED)
                .filter(prop->prop.getId().equals("sampleActionOverride"))
                .count());

    }

    @ParameterizedTest
    @MethodSource("provideProperMemberInheritanceTypes")
    void metamodelContributingProperties_shouldBeUnique_whenOverridden(Class<?> type) {

        if(type.isInterface()
                && type.getSuperclass()==null) {
            return; // not implemented for interface that don't extend from others
        }

        val holderSpec = specificationLoader.loadSpecification(type,
                        IntrospectionState.FULLY_INTROSPECTED);

        val super_property = holderSpec.getAssociationElseFail("samplePropertyOverride");
        assertNotNull(super_property);
        assertEquals("samplePropertyOverride", super_property.getId());
        assertEquals("foo", super_property.getName());
        assertEquals("bar", super_property.getDescription());

        assertEquals(1L, holderSpec.streamProperties(MixedIn.EXCLUDED)
                .filter(prop->prop.getId().equals("samplePropertyOverride"))
                .count());

    }
    
    @Test @Disabled("ISIS-2641,ISIS-2642")
    void elementTypes_shouldBeIntrospected_whenNotConcrete() {
        
        val vmSpec = specificationLoader.loadSpecification(ProperElementTypeVm.class,
                IntrospectionState.FULLY_INTROSPECTED);
        
        val concreteColl = vmSpec.getCollectionElseFail("concreteColl");
        val concreteCollSpec = concreteColl.getSpecification();
        
        assertEquals(ElementTypeConcrete.class, concreteCollSpec.getCorrespondingClass());
        assertEquals(BeanSort.VIEW_MODEL, concreteCollSpec.getBeanSort());
        
        val interfaceColl = vmSpec.getCollectionElseFail("interfaceColl");
        val interfaceCollSpec = interfaceColl.getSpecification();
        
        assertEquals(ElementTypeInterface.class, interfaceCollSpec.getCorrespondingClass());
        assertEquals(BeanSort.ABSTRACT, interfaceCollSpec.getBeanSort());
        
        // when using generic type wild-cards
        
        val concreteColl2 = vmSpec.getCollectionElseFail("concreteColl2");
        val concreteCollSpec2 = concreteColl2.getSpecification();
        
        assertEquals(ElementTypeConcrete.class, concreteCollSpec2.getCorrespondingClass());
        assertEquals(BeanSort.VIEW_MODEL, concreteCollSpec2.getBeanSort());
        
        val interfaceColl2 = vmSpec.getCollectionElseFail("interfaceColl2");
        val interfaceCollSpec2 = interfaceColl2.getSpecification();
        
        assertEquals(ElementTypeInterface.class, interfaceCollSpec2.getCorrespondingClass());
        assertEquals(BeanSort.ABSTRACT, interfaceCollSpec2.getBeanSort());
        
        // TODO for the abstract case, we also want to see any members and the title-facet 
    }

    // -- HELPER

    private void assertHasPublishedActionFacet(FacetHolder facetHolder) {
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


}
