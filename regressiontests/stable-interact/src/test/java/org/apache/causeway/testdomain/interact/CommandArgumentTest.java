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
package org.apache.causeway.testdomain.interact;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Named;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.services.wrapper.control.AsyncControl;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.testdomain.conf.Configuration_headless;
import org.apache.causeway.testdomain.util.interaction.InteractionTestAbstract;

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
    CausewayPresets.SilenceMetaModel,
    CausewayPresets.SilenceProgrammingModel
})
class CommandArgumentTest extends InteractionTestAbstract {

    @XmlRootElement(name = "root")
    @XmlType
    @XmlAccessorType(XmlAccessType.FIELD)
    @Named("regressiontests.CommandResult")
    @DomainObject(nature=Nature.VIEW_MODEL)
    @NoArgsConstructor
    @AllArgsConstructor(staticName = "of")
    public static class CommandResult {

        @Getter @Setter
        private String resultAsString;

    }


    @XmlRootElement(name = "root")
    @XmlType
    @XmlAccessorType(XmlAccessType.FIELD)
    @Named("regressiontests.CommandArgDemo")
    @DomainObject(nature=Nature.VIEW_MODEL)
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
        assertTrue(resultOrVeto.isSuccess());

        val stringified = resultOrVeto.getSuccess()
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
