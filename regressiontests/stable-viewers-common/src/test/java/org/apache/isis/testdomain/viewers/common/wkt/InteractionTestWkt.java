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
package org.apache.isis.testdomain.viewers.common.wkt;

import javax.inject.Inject;

import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.metamodel.commons.ScalarRepresentation;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.testdomain.conf.Configuration_headless;
import org.apache.isis.testdomain.conf.Configuration_usingWicket;
import org.apache.isis.testdomain.conf.Configuration_usingWicket.WicketTesterFactory;
import org.apache.isis.testdomain.model.interaction.Configuration_usingInteractionDomain;
import org.apache.isis.testdomain.model.interaction.InteractionDemo;
import org.apache.isis.testdomain.util.interaction.InteractionTestAbstract;
import org.apache.isis.viewer.common.model.decorator.disable.DisablingUiModel;
import org.apache.isis.viewer.common.model.object.ObjectUiModel.RenderingHint;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.ScalarPropertyModel;
import org.apache.isis.viewer.wicket.model.util.PageParameterUtils;
import org.apache.isis.viewer.wicket.ui.pages.entity.EntityPage;

import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_headless.class,
                Configuration_usingInteractionDomain.class,
                Configuration_usingWicket.class
        },
        properties = {
                //"isis.core.meta-model.introspector.mode=FULL",
                //"isis.applib.annotation.domain-object.editing=TRUE",
                //"isis.core.meta-model.validator.explicit-object-type=FALSE", // does not override any of the imports
                //"logging.level.DependentArgUtils=DEBUG"
        })
@TestPropertySource({
    //IsisPresets.DebugMetaModel,
    //IsisPresets.DebugProgrammingModel,
    IsisPresets.SilenceMetaModel,
    IsisPresets.SilenceProgrammingModel
})
class InteractionTestWkt extends InteractionTestAbstract {

    @Inject private IsisAppCommonContext commonContext;
    @Inject private WicketTesterFactory wicketTesterFactory;
    private WicketTester wktTester;

    private ManagedObject domainObject;
    private PageParameters pageParameters;

    @BeforeEach
    void setUp() {
        wktTester = wicketTesterFactory.createTester(dto->null);
        domainObject = newViewmodel(InteractionDemo.class);
        pageParameters = PageParameterUtils.createPageParametersForObject(domainObject);
    }

    @AfterEach
    void cleanUp() {
        wktTester.destroy();
    }

    @Test
    void shouldHaveARequestCycle() {

        val entityPage = EntityPage.ofPageParameters(commonContext, pageParameters);
        wktTester.startPage(entityPage);

        assertNotNull(RequestCycle.get());

        //TODO broken
//        assertEquals(
//                pageParameters,
//                PageParameterUtils.currentPageParameters());
    }

    @Test
    void propertyModels_shouldBeInSyncWithInteractionAPI() {

        val objectSpec = domainObject.getSpecification();
        val entityModel = EntityModel.ofAdapter(commonContext, domainObject);

        assertEquals(domainObject.getBookmark().get(), entityModel.getOwnerBookmark());
        assertEquals(domainObject.getTitle(), entityModel.getTitle());

        // owner sharing (should be the same object)
        assertTrue(domainObject == entityModel.getBookmarkedOwner());

        final long propertyCount =
        objectSpec.streamProperties(MixedIn.INCLUDED)
        .filter(prop->{

            final ScalarPropertyModel scalarModel = (ScalarPropertyModel) entityModel
                    .getPropertyModel(
                            prop,
                            ScalarRepresentation.VIEWING,
                            RenderingHint.PARENTED_PROPERTY_COLUMN);


            // owner sharing (should be the same object)
            assertTrue(domainObject == scalarModel.getOwner());

            if(!prop.getId().equals("stringMultiline")) {
                return true; // continue
            }

            // the scalar model should be in sync with the underlying interaction API
            val pendingPropModel = scalarModel.getPendingPropertyModel();
            val propBackendValue = pendingPropModel.getValue().getValue();
            val propUIValue = scalarModel.getObject();

            assertEquals(
                    "initial",
                    pendingPropModel.getValueAsParsableText().getValue());

            assertEquals(
                    "initial",
                    propBackendValue.getPojo());

            assertEquals(
                    "initial",
                    propUIValue.getPojo());

            // property value sharing (should be the same object)
            assertEquals(
                    propBackendValue,
                    propUIValue);


            return true; // continue

        })
        .count();

        assertEquals(5L, propertyCount);

        val managedAction = startActionInteractionOn(InteractionDemo.class, "noArgEnabled", Where.OBJECT_FORMS)
                .getManagedAction().get(); // should not throw

        assertFalse(managedAction.checkVisibility().isPresent()); // is visible
        assertFalse(managedAction.checkUsability().isPresent()); // can invoke
    }

    @Test
    void whenEnabled_shouldProvideProperDecoratorModels() {

        val actionInteraction = startActionInteractionOn(InteractionDemo.class, "noArgEnabled", Where.OBJECT_FORMS)
                .checkVisibility()
                .checkUsability();

        val disablingUiModel = DisablingUiModel.of(actionInteraction);
        assertFalse(disablingUiModel.isPresent());
    }

    @Test
    void whenDisabled_shouldProvideProperDecoratorModels() {

        val actionInteraction = startActionInteractionOn(InteractionDemo.class, "noArgDisabled", Where.OBJECT_FORMS)
                .checkVisibility()
                .checkUsability();

        val disablingUiModel = DisablingUiModel.of(actionInteraction).get();
        assertEquals("Disabled for demonstration.", disablingUiModel.getReason());
    }
}