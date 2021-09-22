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
package org.apache.isis.testdomain.viewers;

import javax.inject.Inject;

import org.apache.wicket.Application;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.protocol.http.mock.MockHttpServletRequest;
import org.apache.wicket.protocol.http.mock.MockHttpServletResponse;
import org.apache.wicket.protocol.http.mock.MockHttpSession;
import org.apache.wicket.protocol.http.mock.MockServletContext;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.cycle.RequestCycleContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.server.MockWebSession;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.testdomain.conf.Configuration_headless;
import org.apache.isis.testdomain.conf.Configuration_usingWicket;
import org.apache.isis.testdomain.conf.Configuration_usingWicket.RequestCycleFactory;
import org.apache.isis.testdomain.model.interaction.Configuration_usingInteractionDomain;
import org.apache.isis.testdomain.model.interaction.InteractionDemo;
import org.apache.isis.testdomain.util.interaction.InteractionTestAbstract;
import org.apache.isis.viewer.common.model.object.ObjectUiModel.RenderingHint;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.ScalarPropertyModel;
import org.apache.isis.viewer.wicket.model.util.PageParameterUtils;

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
class ActionInteractionTest extends InteractionTestAbstract {

    @Inject IsisAppCommonContext commonContext;
    @Inject RequestCycleFactory requestCycleFactory;

    private ManagedObject domainObject;

    @BeforeEach
    void setUp() {
        domainObject = newViewmodel(InteractionDemo.class);
        requestCycleFactory.newRequestCycle(PageParameterUtils.createPageParametersForObject(domainObject));
    }

    @AfterEach
    void cleanUp() {
        requestCycleFactory.clearRequestCycle();
    }

    @Test
    void shouldHaveARequestCycle() {
        assertNotNull(RequestCycle.get());
    }

    @Test
    void whenEnabled_shouldHaveNoVeto() {

        final var objectSpec = domainObject.getSpecification();
        final var entityModel = EntityModel.ofAdapter(commonContext, domainObject);

        assertEquals(domainObject.getBookmark().get(), entityModel.getOwnerBookmark());
        assertEquals(domainObject.getTitle(), entityModel.getTitle());

        objectSpec.streamProperties(MixedIn.INCLUDED)
        .forEach(prop->{

            final ScalarPropertyModel scalarModel = (ScalarPropertyModel) entityModel
                    .getPropertyModel(
                            prop,
                            EntityModel.EitherViewOrEdit.VIEW,
                            RenderingHint.PARENTED_PROPERTY_COLUMN);


            final var propValue = scalarModel.getObject();
            propValue.getPojo();

            // owner sharing (should be the same object)
            assertEquals(domainObject, scalarModel.getOwner());

        });





        final var managedAction = startActionInteractionOn(InteractionDemo.class, "noArgEnabled", Where.OBJECT_FORMS)
                .getManagedAction().get(); // should not throw

        assertFalse(managedAction.checkVisibility().isPresent()); // is visible
        assertFalse(managedAction.checkUsability().isPresent()); // can invoke
    }
}