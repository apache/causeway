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
package org.apache.isis.testdomain.interact;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.wrapper.control.AsyncControl;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.testdomain.conf.Configuration_headless;
import org.apache.isis.testdomain.util.interaction.InteractionTestAbstract;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_headless.class,
                CommandArgumentTest.CommandArgDemo.class,
        },
        properties = {
        })
@TestPropertySource({
    IsisPresets.SilenceMetaModel,
    IsisPresets.SilenceProgrammingModel
})
class CommandArgumentTest extends InteractionTestAbstract {

    @XmlRootElement(name = "root")
    @XmlType
    @XmlAccessorType(XmlAccessType.FIELD)
    @DomainObject(nature=Nature.VIEW_MODEL, logicalTypeName="regressiontests.CommandResult")
    @NoArgsConstructor
    @AllArgsConstructor(staticName = "of")
    public static class CommandResult {

        @Getter @Setter
        private String resultAsString;

    }


    @XmlRootElement(name = "root")
    @XmlType
    @XmlAccessorType(XmlAccessType.FIELD)
    @DomainObject(nature=Nature.VIEW_MODEL, logicalTypeName="regressiontests.CommandArgDemo")
    public static class CommandArgDemo {

        @Action
        public CommandResult list(final List<Long> someIds){
            val stringified = ""+someIds;
            assertEquals("[1, 2, 3]", stringified);
            return CommandResult.of(stringified);
        }

    }

    @Test
    void listParam_shouldAllowInvocation() {

        val actionInteraction = startActionInteractionOn(CommandArgDemo.class, "list", Where.OBJECT_FORMS)
        .checkVisibility()
        .checkUsability();

        val pendingArgs = actionInteraction.startParameterNegotiation().get();

        pendingArgs.setParamValue(0, objectManager.adapt(Arrays.asList(1L, 2L, 3L)));

        val resultOrVeto = actionInteraction.invokeWith(pendingArgs);
        assertTrue(resultOrVeto.isLeft());

        val stringified = resultOrVeto.left()
                .map(ManagedObject::getPojo)
                .map(CommandResult.class::cast)
                .map(CommandResult::getResultAsString)
                .orElse(null);
        assertEquals("[1, 2, 3]", stringified);
    }

    @Test
    void listParam_shouldAllowAsyncInvocation() throws InterruptedException, ExecutionException, TimeoutException {

        val commandArgDemo = new CommandArgDemo();

        val control = AsyncControl.returning(CommandResult.class);

        wrapperFactory.asyncWrap(commandArgDemo, control)
        .list(Arrays.asList(1L, 2L, 3L));

        val stringified = control.getFuture().get(3L, TimeUnit.DAYS).getResultAsString();

        assertEquals("[1, 2, 3]", stringified);
    }


}
