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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;


import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.apache.causeway.core.internaltestsupport.annotations.DisabledIfRunningWithSurefire;
import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.causeway.extensions.commandreplay.secondary.config.SecondaryConfig;
import org.apache.causeway.extensions.commandreplay.secondary.status.StatusException;

@SpringBootTest(
        classes = {
                CommandFetcher_Test.TestManifest.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "causeway.extensions.commandReplay.primaryAccess.user=sven",
                "causeway.extensions.commandReplay.primaryAccess.password=pass",
                "causeway.extensions.commandReplay.primaryAccess.baseUrlRestful=http://localhost:8080/restful/",
                "causeway.extensions.commandReplay.primaryAccess.baseUrlWicket=http://localhost:8080/wicket/",
                "causeway.extensions.commandReplay.batchSize=10",
                // "causeway.core.meta-model.introspector.parallelize=false",
                // "logging.level.ObjectSpecificationAbstract=TRACE"
        })
//intended only for manual verification.
@DisabledIfRunningWithSurefire
//@Slf4j
class CommandFetcher_Test {

    @Configuration
    static class TestManifest {

    }

    @Test
    void testing_the_fetcher() throws StatusException {

        // given
        var mmc = MetaModelContext_forTesting.buildDefault();

        var config = mmc.getConfiguration().extensions().commandReplay();
        assertThat(config.primaryAccess().user(), is(Optional.of("sven")));
        assertThat(config.primaryAccess().password(), is(Optional.of("pass")));
        assertThat(config.primaryAccess().baseUrlRestful(), is(Optional.of("http://localhost:8080/restful/")));
        assertThat(config.primaryAccess().baseUrlWicket(), is(Optional.of("http://localhost:8080/wicket/")));
        assertThat(config.batchSize(), is(10));

        var secondaryConfig = new SecondaryConfig(mmc.getConfiguration());
        var useRequestDebugLogging = true;
        var fetcher = new CommandFetcher(secondaryConfig, useRequestDebugLogging);

        // when
        //log.info("about to call REST endpoint ...");
        var commands = fetcher.callPrimary(null);
        assertNotNull(commands);
        System.out.println(commands);
    }

}
