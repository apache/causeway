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
package org.apache.causeway.core.metamodel.specloader.specimpl;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.config.beans.CausewayBeanMetaData;
import org.apache.causeway.core.config.beans.CausewayBeanTypeRegistryDefault;
import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.causeway.core.metamodel.execution.MemberExecutorService;
import org.apache.causeway.core.metamodel.spec.IntrospectionState;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;

import lombok.RequiredArgsConstructor;
import lombok.val;

class MixinIntrospectionPrefilterTest {

    @Test
    void supportedMixinStylesExposeApplicabilityAtTypeIntrospection() {
        val mmc = newMetamodelContext(
                Target_action.class,
                Target_property.class,
                Target_collection.class,
                Target_classic.class);

        assertTypeMetadata(mmc, Target_action.class, "act");
        assertTypeMetadata(mmc, Target_property.class, "prop");
        assertTypeMetadata(mmc, Target_collection.class, "coll");
        assertTypeMetadata(mmc, Target_classic.class, "custom");
    }

    @Test
    void onlyApplicableMixinsAreFullyIntrospectedAndContributed() {
        val mmc = newMetamodelContext(
                Other_irrelevantAction.class,
                Other_irrelevantProperty.class,
                Other_irrelevantCollection.class,
                RegisteredWithoutMixinFacet.class,
                Target_action.class,
                Target_property.class,
                Target_collection.class);

        val targetSpec = mmc.getSpecificationLoader().loadSpecification(Target.class);

        assertTrue(targetSpec.getAction("action", MixedIn.INCLUDED).isPresent());
        assertTrue(targetSpec.getAssociation("property", MixedIn.INCLUDED).isPresent());
        assertTrue(targetSpec.getAssociation("collection", MixedIn.INCLUDED).isPresent());
        assertFalse(targetSpec.getAction("irrelevantAction", MixedIn.INCLUDED).isPresent());
        assertFalse(targetSpec.getAssociation("irrelevantProperty", MixedIn.INCLUDED).isPresent());
        assertFalse(targetSpec.getAssociation("irrelevantCollection", MixedIn.INCLUDED).isPresent());

        assertState(mmc, Target_action.class, IntrospectionState.FULLY_INTROSPECTED);
        assertState(mmc, Target_property.class, IntrospectionState.FULLY_INTROSPECTED);
        assertState(mmc, Target_collection.class, IntrospectionState.FULLY_INTROSPECTED);
        assertState(mmc, Other_irrelevantAction.class, IntrospectionState.TYPE_INTROSPECTED);
        assertState(mmc, Other_irrelevantProperty.class, IntrospectionState.TYPE_INTROSPECTED);
        assertState(mmc, Other_irrelevantCollection.class, IntrospectionState.TYPE_INTROSPECTED);
        assertState(mmc, RegisteredWithoutMixinFacet.class, IntrospectionState.TYPE_INTROSPECTED);
    }

    @Test
    void objectWideActionMixinIsRejectedBeforeFullIntrospectionForAContributingService() {
        val mmc = newMetamodelContext(Object_recentChanges.class);

        val serviceSpec = mmc.getSpecificationLoader().loadSpecification(ContributingService.class);

        assertFalse(serviceSpec.getAction("recentChanges", MixedIn.INCLUDED).isPresent());
        assertState(mmc, Object_recentChanges.class, IntrospectionState.TYPE_INTROSPECTED);
    }

    @Test
    void malformedMixinIsValidatedDuringTypeIntrospectionWithoutAContributionTarget() {
        val mmc = newMetamodelContext(Malformed_action.class);

        mmc.getSpecificationLoader().loadSpecification(
                Malformed_action.class,
                IntrospectionState.TYPE_INTROSPECTED);

        val messages = mmc.getSpecificationLoader().getOrAssessValidationResult().getMessages();
        assertTrue(messages.stream().anyMatch(message ->
                message.contains(Malformed_action.class.getName())
                && message.contains("does not have a public 1-arg constructor")));
    }

    private void assertTypeMetadata(
            final MetaModelContext_forTesting mmc,
            final Class<?> mixinType,
            final String expectedMainMethodName) {
        val spec = mmc.getSpecificationLoader().loadSpecification(
                mixinType,
                IntrospectionState.TYPE_INTROSPECTED);
        val mixinFacet = spec.mixinFacet().orElseThrow();

        assertEquals(IntrospectionState.TYPE_INTROSPECTED,
                ((ObjectSpecificationAbstract) spec).introspectionStateForDiagnostics());
        assertEquals(expectedMainMethodName, mixinFacet.getMainMethodName());
        assertTrue(mixinFacet.isMixinFor(Target.class));
        assertFalse(mixinFacet.isMixinFor(Other.class));
    }

    private void assertState(
            final MetaModelContext_forTesting mmc,
            final Class<?> type,
            final IntrospectionState expected) {
        val spec = mmc.getSpecificationLoader().loadSpecification(type, IntrospectionState.TYPE_INTROSPECTED);
        assertEquals(expected, ((ObjectSpecificationAbstract) spec).introspectionStateForDiagnostics());
    }

    private MetaModelContext_forTesting newMetamodelContext(final Class<?>... mixinTypes) {
        return MetaModelContext_forTesting.builder()
                .memberExecutor(Mockito.mock(MemberExecutorService.class))
                .causewayBeanTypeRegistry(new CausewayBeanTypeRegistryDefault(Can.ofArray(mixinTypes)
                        .map(mixinType -> CausewayBeanMetaData.notManaged(BeanSort.MIXIN, mixinType))))
                .build();
    }

    @DomainObject(nature = Nature.VIEW_MODEL)
    static class Target {}

    @DomainObject(nature = Nature.VIEW_MODEL)
    static class Other {}

    @Action
    @RequiredArgsConstructor
    static class Target_action {
        @SuppressWarnings("unused")
        private final Target mixee;
        public void act() {
        }
    }

    @Property
    @RequiredArgsConstructor
    static class Target_property {
        @SuppressWarnings("unused")
        private final Target mixee;
        public String prop() {
            return "property";
        }
    }

    @org.apache.causeway.applib.annotation.Collection
    @RequiredArgsConstructor
    static class Target_collection {
        @SuppressWarnings("unused")
        private final Target mixee;
        @MemberSupport public List<String> coll() {
            return new ArrayList<>();
        }
    }

    @DomainObject(nature = Nature.MIXIN, mixinMethod = "custom")
    @RequiredArgsConstructor
    static class Target_classic {
        @SuppressWarnings("unused")
        private final Target mixee;
        public void custom() {
        }
    }

    @Action
    @RequiredArgsConstructor
    static class Other_irrelevantAction {
        @SuppressWarnings("unused")
        private final Other mixee;
        public void act() {
        }
    }

    @Property
    @RequiredArgsConstructor
    static class Other_irrelevantProperty {
        @SuppressWarnings("unused")
        private final Other mixee;
        public String prop() {
            return "irrelevant";
        }
    }

    @org.apache.causeway.applib.annotation.Collection
    @RequiredArgsConstructor
    static class Other_irrelevantCollection {
        @SuppressWarnings("unused")
        private final Other mixee;
        @MemberSupport public List<String> coll() {
            return new ArrayList<>();
        }
    }

    @RequiredArgsConstructor
    static class RegisteredWithoutMixinFacet {
        @SuppressWarnings("unused")
        private final Other mixee;
    }

    @DomainService
    static class ContributingService {}

    @Action
    @RequiredArgsConstructor
    static class Object_recentChanges {
        @SuppressWarnings("unused")
        private final Object mixee;
        public void act() {
        }
    }

    @Action
    static class Malformed_action {
        public void act() {
        }
    }
}
