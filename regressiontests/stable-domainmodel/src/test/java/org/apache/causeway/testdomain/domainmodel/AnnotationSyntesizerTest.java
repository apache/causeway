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

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.ReflectionUtils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.events.domain.ActionDomainEvent;
import org.apache.causeway.applib.services.jaxb.JaxbService;
import org.apache.causeway.applib.services.metamodel.Config;
import org.apache.causeway.applib.services.metamodel.MetaModelService;
import org.apache.causeway.commons.internal.reflection._Annotations;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.schema.metamodel.v2.DomainClassDto;
import org.apache.causeway.testdomain.conf.Configuration_headless;
import org.apache.causeway.testdomain.model.bad.Configuration_usingInvalidDomain;
import org.apache.causeway.testdomain.model.bad.InvalidPropertyAnnotationOnAction;
import org.apache.causeway.testdomain.model.good.Configuration_usingValidDomain;

import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_headless.class,
                Configuration_usingValidDomain.class,
                Configuration_usingInvalidDomain.class
        },
        properties = {
                "causeway.core.meta-model.introspector.mode=FULL"
        })
@TestPropertySource({
    //CausewayPresets.DebugMetaModel,
    //CausewayPresets.DebugProgrammingModel,
    CausewayPresets.SilenceMetaModel,
    CausewayPresets.SilenceProgrammingModel
})
class AnnotationSyntesizerTest {

    @Inject private MetaModelService metaModelService;
    @Inject private JaxbService jaxbService;

    @Test
    void propertyAnnotationOnAction_shouldNotContributeToSynthesizedAction() {

        val actionMethod = ReflectionUtils.findMethod(InvalidPropertyAnnotationOnAction.class, "exportToJson");
        assertNotNull(actionMethod);

        val action = _Annotations.synthesize(actionMethod, Action.class).get();
        val domainEvent = action.domainEvent();

        if(!ActionDomainEvent.class.isAssignableFrom(domainEvent)) {
            fail(String.format("wrong event type resolved on %s -> %s", actionMethod, domainEvent));
        }

    }

    //@Test
    void debug() {

        val config = Config.builder().build()
                .withNamespacePrefix("org.apache.causeway.testdomain.model.");

        System.out.println("!!! listing MM");
        val metamodelDto = metaModelService.exportMetaModel(config);
        for (DomainClassDto domainClass : metamodelDto.getDomainClassDto()) {
            System.out.println("dc: " + domainClass.getId());
            val xmlString = jaxbService.toXml(domainClass);
            System.out.println(xmlString);
        }
        System.out.println("!!! ---");

//        val validateDomainModel = new DomainModelValidator();
//        validateDomainModel.run(); // should not throw

    }

}
