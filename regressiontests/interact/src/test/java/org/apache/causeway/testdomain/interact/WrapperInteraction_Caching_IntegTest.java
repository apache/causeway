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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.wrapper.control.AsyncControl;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.testdomain.conf.Configuration_headless;
import org.apache.causeway.testdomain.model.interaction.Configuration_usingInteractionDomain;
import org.apache.causeway.testdomain.util.interaction.InteractionTestAbstract;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@SpringBootTest(
        classes = {
                Configuration_headless.class,
                Configuration_usingInteractionDomain.class,
                WrapperInteraction_Caching_IntegTest.StatefulCalculator.class,
                WrapperInteraction_Caching_IntegTest.StatefulCalculator_add.class
        }
)
@TestPropertySource({
    CausewayPresets.SilenceMetaModel,
    CausewayPresets.SilenceProgrammingModel
})
class WrapperInteraction_Caching_IntegTest
extends InteractionTestAbstract {

    @Data @DomainObject(nature = Nature.VIEW_MODEL)
    static class StatefulCalculator {
        @Getter int total;
        @Action public Integer inc(final int amount) { return total += amount; }
        @Action(semantics = SemanticsOf.IDEMPOTENT) public void reset() { total = 0; }
    }

    @Action
    @RequiredArgsConstructor
    public static class StatefulCalculator_add {
        private final StatefulCalculator mixee;
        public Integer act(final int amount) {
            return mixee.inc(amount);
        }
    }

    StatefulCalculator calculator1;
    StatefulCalculator calculator2;

    @BeforeEach
    void before() {
        calculator1 = new StatefulCalculator();
        calculator2 = new StatefulCalculator();

        Assertions.assertThat(calculator1.total).isEqualTo(0);
        Assertions.assertThat(calculator2.total).isEqualTo(0);
    }

    @Test
    void sync_wrapped() throws ExecutionException, InterruptedException, TimeoutException {

        // when
        wrap(calculator1).inc(5);
        wrap(calculator2).inc(10);

        // then
        Assertions.assertThat(calculator1.total).isEqualTo(5);
        Assertions.assertThat(calculator2.total).isEqualTo(10);
    }

    @Test
    void sync_mixin() throws ExecutionException, InterruptedException, TimeoutException {

        // when
        wrapMixin(StatefulCalculator_add.class, calculator1).act(5);
        wrapMixin(StatefulCalculator_add.class, calculator2).act(10);

        // then
        Assertions.assertThat(calculator1.total).isEqualTo(5);
        Assertions.assertThat(calculator2.total).isEqualTo(10);
    }

    @Disabled
    @Test
    void async_wrapped() throws ExecutionException, InterruptedException, TimeoutException {

        // when
        var asyncControlForCalculator1 = AsyncControl.defaults();
        var asyncCalculator1 = wrapperFactory.asyncWrap(calculator1, asyncControlForCalculator1)
                .applyAsync(calc->calc.inc(12));

        var asyncControlForCalculator2 = AsyncControl.defaults();
        var asyncCalculator2 = wrapperFactory.asyncWrap(calculator2, asyncControlForCalculator2)
                .applyAsync(calc->calc.inc(24));

        // then
        Assertions.assertThat(asyncCalculator1.tryGet(10, TimeUnit.SECONDS).valueAsNonNullElseFail().intValue()).isEqualTo(12);
        Assertions.assertThat(calculator1.getTotal()).isEqualTo(12);

        Assertions.assertThat(asyncCalculator2.tryGet(10, TimeUnit.SECONDS).valueAsNonNullElseFail().intValue()).isEqualTo(24);
        Assertions.assertThat(calculator2.getTotal()).isEqualTo(24);
    }


}
