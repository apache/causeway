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

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.ReflectionUtils;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.events.domain.ActionDomainEvent;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.services.metamodel.MetaModelService;
import org.apache.isis.commons.internal.reflection._Annotations;
import org.apache.isis.schema.metamodel.v1.DomainClassDto;
import org.apache.isis.testdomain.Smoketest;
import org.apache.isis.testdomain.conf.Configuration_headless;
import org.apache.isis.testdomain.model.bad.Configuration_usingInvalidDomain;
import org.apache.isis.testdomain.model.bad.InvalidPropertyAnnotationOnAction;
import org.apache.isis.testdomain.model.good.Configuration_usingValidDomain;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import lombok.val;

@Smoketest
@SpringBootTest(
        classes = { 
                Configuration_headless.class,
                Configuration_usingValidDomain.class,
                Configuration_usingInvalidDomain.class
        }, 
        properties = {
                "isis.reflector.introspector.mode=FULL"
        })
@TestPropertySource({
    //IsisPresets.DebugMetaModel,
    //IsisPresets.DebugProgrammingModel,

})
class AnnotationSyntesizerTest {

    @Inject private MetaModelService metaModelService;
    @Inject private JaxbService jaxbService;

    @Test
    void propertyAnnotationOnAction_shouldNotContributeToSynthesizedAction() {

        val actionMethod = ReflectionUtils.findMethod(InvalidPropertyAnnotationOnAction.class, "exportToJson");
        assertNotNull(actionMethod);

        val action = _Annotations.synthesizeInherited(actionMethod, Action.class).get();
        val domainEvent = action.domainEvent();

        if(!ActionDomainEvent.class.isAssignableFrom(domainEvent)) {
            fail(String.format("wrong event type resolved on %s -> %s", actionMethod, domainEvent));
        }

    }

    //@Test 
    void debug() {


        val config = new MetaModelService.Config()
                //            .withIgnoreNoop()
                //            .withIgnoreAbstractClasses()
                //            .withIgnoreBuiltInValueTypes()
                //            .withIgnoreInterfaces()
                //.withPackagePrefix("*")
                .withPackagePrefix("org.apache.isis.testdomain.model.")
                ;

        System.out.println("!!! listing MM");
        val metamodelDto = metaModelService.exportMetaModel(config);
        for (DomainClassDto domainClass : metamodelDto.getDomainClassDto()) {
            System.out.println("dc: " + domainClass.getId());
            val xmlString = jaxbService.toXml(domainClass);
            System.out.println(xmlString);
        }
        System.out.println("!!! ---");

//        val validateDomainModel = new ValidateDomainModel();
//        validateDomainModel.run(); // should not throw

    }

}
