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
package org.apache.causeway.extensions.commandreplay.secondary.fetch;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.causeway.extensions.commandreplay.secondary.config.SecondaryConfig;
import org.apache.causeway.extensions.commandreplay.secondary.status.StatusException;

import lombok.val;

@SpringBootTest(
        classes = {
                CommandFetcher_Test.TestManifest.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                // "causeway.core.meta-model.introspector.parallelize=false",
                // "logging.level.ObjectSpecificationAbstract=TRACE"
        })
@TestPropertySource({
    CausewayPresets.UseLog4j2Test,
})
//intended only for manual verification.
@DisabledIfSystemProperty(named = "isRunningWithSurefire", matches = "true")
//@Log4j2
class CommandFetcher_Test {

    @Configuration
    static class TestManifest {

    }

    @Test
    void testing_the_fetcher() throws StatusException {

        // given
        val mmc = MetaModelContext_forTesting.buildDefault();

        val config = mmc.getConfiguration().getExtensions().getCommandReplay();
        config.getPrimaryAccess().setUser(Optional.of("sven"));
        config.getPrimaryAccess().setPassword(Optional.of("pass"));
        config.getPrimaryAccess().setBaseUrlRestful(Optional.of("http://localhost:8080/restful/"));
        config.getPrimaryAccess().setBaseUrlWicket(Optional.of("http://localhost:8080/wicket/"));
        config.setBatchSize(10);

        val secondaryConfig = new SecondaryConfig(mmc.getConfiguration());
        val useRequestDebugLogging = true;
        val fetcher = new CommandFetcher(secondaryConfig, useRequestDebugLogging);

        // when
        //log.info("about to call REST endpoint ...");
        val commands = fetcher.callPrimary(null);
        assertNotNull(commands);
        System.out.println(commands);
    }


}
