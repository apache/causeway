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

import jakarta.inject.Inject;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.services.wrapper.DisabledException;
import org.apache.causeway.applib.services.wrapper.WrapperFactory;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.testdomain.conf.Configuration_headless;
import org.apache.causeway.testing.integtestsupport.applib.CausewayIntegrationTestAbstract;

import lombok.Getter;
import lombok.Setter;

@SpringBootTest(
        classes = {
                Configuration_headless.class,
        },
        properties = {
                "causeway.applib.annotation.domain-object.editing=FALSE",
                "causeway.core.meta-model.validator.explicit-object-type=FALSE", // does not override any of the imports
        })
@TestPropertySource({
    CausewayPresets.IntrospectFully,
    CausewayPresets.SilenceMetaModel,
    CausewayPresets.SilenceProgrammingModel
})
class DomainModelTest_forEditing extends CausewayIntegrationTestAbstract {

    @Inject private WrapperFactory wrapper;
    @Inject private SpecificationLoader specificationLoader;

    // -------------------------------------------------------
    // type:ENABLED + property:NOT_SPECIFIED -> effective ENABLED
    // -------------------------------------------------------

    @XmlRootElement(name = "CustomerEna")
    @DomainObject(
            nature=Nature.VIEW_MODEL,
            editing=org.apache.causeway.applib.annotation.Editing.ENABLED)
    public static class CustomerEna {
        @Property
        @Getter @Setter private String name;
    }

    @Test
    void classLevelAnnotation_whenEnabling_shouldSetTheDefault() {

        var holderSpec = specificationLoader.specForTypeElseFail(CustomerEna.class);
        holderSpec.getAssociationElseFail("name");

        //then ... should not fail
        wrapper.wrap(new CustomerEna()).setName("Bob");
    }

    // -------------------------------------------------------
    // type:DISABLED + property:NOT_SPECIFIED -> effective DISABLED
    // -------------------------------------------------------

    @XmlRootElement(name = "CustomerDis")
    @DomainObject(
            nature=Nature.VIEW_MODEL,
            editing=org.apache.causeway.applib.annotation.Editing.DISABLED,
            editingDisabledReason = "you cannot edit this object")
    public static class CustomerDis {
        @Property
        @Getter @Setter private String name;
    }

    @Test
    void classLevelAnnotation_whenDisabling_shouldSetTheDefault() {

        var holderSpec = specificationLoader.specForTypeElseFail(CustomerDis.class);
        holderSpec.getAssociationElseFail("name");

        //then ... should fail
        var disabledException = assertThrows(DisabledException.class,
                ()->wrapper.wrap(new CustomerDis()).setName("Bob"));

        assertTrue(disabledException.getMessage()
                        .contains("you cannot edit this object"));
    }

    // -------------------------------------------------------
    // type:ENABLED + property:DISABLED -> effective DISABLED
    // -------------------------------------------------------

    @XmlRootElement(name = "CustomerEnaDis")
    @DomainObject(
            nature=Nature.VIEW_MODEL,
            editing=org.apache.causeway.applib.annotation.Editing.ENABLED)
    public static class CustomerEnaDis {
        @Property(
                // should disable editing regardless of class-level
                editing = org.apache.causeway.applib.annotation.Editing.DISABLED,
                editingDisabledReason = "you cannot edit the name property"
                )
        @Getter @Setter private String name;
    }

    @Test
    void classLevelAnnotation_whenEnabling_shouldBeOverridable() {

        var holderSpec = specificationLoader.specForTypeElseFail(CustomerEnaDis.class);
        holderSpec.getAssociationElseFail("name");

        //then ... should fail
        var disabledException = assertThrows(DisabledException.class,
                ()->wrapper.wrap(new CustomerEnaDis()).setName("Bob"));

        String message = disabledException.getMessage();
        Assertions.assertThat(message).startsWith("you cannot edit the name property");
    }

    // -------------------------------------------------------
    // type:DISABLED + property:ENABLED -> effective ENABLED
    // -------------------------------------------------------

    @XmlRootElement(name = "CustomerDisEna")
    @DomainObject(
            nature=Nature.VIEW_MODEL,
            editing=org.apache.causeway.applib.annotation.Editing.DISABLED,
            editingDisabledReason = "you cannot edit this object")
    public static class CustomerDisEna {
        @Property(
                // should enable editing regardless of class-level
                editing = org.apache.causeway.applib.annotation.Editing.ENABLED)
        @Getter @Setter private String name;
    }

    @Test
    void classLevelAnnotation_whenDisabling_shouldBeOverridable() {

        var holderSpec = specificationLoader.specForTypeElseFail(CustomerDisEna.class);
        holderSpec.getAssociationElseFail("name");

        //then ... should not fail
        wrapper.wrap(new CustomerDisEna()).setName("Bob");
    }

}
