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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.services.wrapper.DisabledException;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.specimpl.IntrospectionState;
import org.apache.isis.testdomain.Incubating;
import org.apache.isis.testdomain.Smoketest;
import org.apache.isis.testdomain.conf.Configuration_headless;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

@Smoketest
@SpringBootTest(
        classes = { 
                Configuration_headless.class,
        }, 
        properties = {
                "isis.applib.annotation.domain-object.editing=TRUE",
                "isis.core.meta-model.validator.explicit-object-type=FALSE", // does not override any of the imports
        })
@TestPropertySource({
    IsisPresets.IntrospectFully,
    IsisPresets.UseLog4j2Test,
    IsisPresets.SilenceMetaModel,
    IsisPresets.SilenceProgrammingModel
})
@DirtiesContext
@Incubating("fails when run with surefire")
class DomainModelTest_forEditing {

    @Inject private WrapperFactory wrapper;
    @Inject private SpecificationLoader specificationLoader;

    
    // -------------------------------------------------------
    // type:ENABLED + property:NOT_SPECIFIED -> effective ENABLED
    // -------------------------------------------------------


    @DomainObject(
            nature=Nature.INMEMORY_ENTITY, 
            editing=org.apache.isis.applib.annotation.Editing.ENABLED)
    public static class CustomerEna {
        @Property
        @Getter @Setter private String name;
    }

    @Test @Disabled("fails ... Non-cloneable view models are read-only; Immutable")
    void classLevelAnnotation_whenEnabling_shouldSetTheDefault() {

        val holderSpec = specificationLoader.loadSpecification(CustomerEna.class, 
                IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED);
        holderSpec.getAssociationElseFail("name");

        //then ... should not fail
        wrapper.wrap(new CustomerEna()).setName("Bob");
    }
    

    // -------------------------------------------------------
    // type:DISABLED + property:NOT_SPECIFIED -> effective DISABLED
    // -------------------------------------------------------

    @DomainObject(
            nature=Nature.INMEMORY_ENTITY, 
            editing=org.apache.isis.applib.annotation.Editing.DISABLED,
            editingDisabledReason = "you cannot edit this object")
    public static class CustomerDis {
        @Property
        @Getter @Setter private String name;
    }

    @Test @Disabled("fails for the wrong reason: Non-cloneable view models are read-only; Immutable")
    void classLevelAnnotation_whenDisabling_shouldSetTheDefault() {

        val holderSpec = specificationLoader.loadSpecification(CustomerDis.class, 
                IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED);
        holderSpec.getAssociationElseFail("name");

        //then ... should fail
        val disabledException = assertThrows(DisabledException.class, 
                ()->wrapper.wrap(new CustomerDis()).setName("Bob"));

        assertTrue(disabledException.getMessage()
                        .contains("you cannot edit this object"));
    }
    
    // -------------------------------------------------------
    // type:ENABLED + property:DISABLED -> effective DISABLED
    // -------------------------------------------------------

    @DomainObject(
            nature=Nature.INMEMORY_ENTITY, 
            editing=org.apache.isis.applib.annotation.Editing.ENABLED)
    public static class CustomerEnaDis {
        @Property(
                // should disable editing regardless of class-level
                editing = org.apache.isis.applib.annotation.Editing.DISABLED, 
                editingDisabledReason = "you cannot edit the name property"
                )
        @Getter @Setter private String name;
    }

    @Test
    void classLevelAnnotation_whenEnabling_shouldBeOverridable() {

        val holderSpec = specificationLoader.loadSpecification(CustomerEnaDis.class, 
                IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED);
        holderSpec.getAssociationElseFail("name");

        //then ... should fail
        val disabledException = assertThrows(DisabledException.class, 
                ()->wrapper.wrap(new CustomerEnaDis()).setName("Bob"));

        assertTrue(disabledException.getMessage()
                .startsWith("Reason: you cannot edit the name property. "));
    }
    
    // -------------------------------------------------------
    // type:DISABLED + property:ENABLED -> effective ENABLED
    // -------------------------------------------------------

    @DomainObject(
            nature=Nature.INMEMORY_ENTITY,
            editing=org.apache.isis.applib.annotation.Editing.DISABLED,
            editingDisabledReason = "you cannot edit this object")
    public static class CustomerDisEna {
        @Property(
                // should enable editing regardless of class-level
                editing = org.apache.isis.applib.annotation.Editing.ENABLED)
        @Getter @Setter private String name;
    }


    @Test
    void classLevelAnnotation_whenDisabling_shouldBeOverridable() {

        val holderSpec = specificationLoader.loadSpecification(CustomerDisEna.class, 
                IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED);
        holderSpec.getAssociationElseFail("name");

        //then ... should not fail
        wrapper.wrap(new CustomerDisEna()).setName("Bob");
    }



}
