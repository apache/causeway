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
package org.apache.isis.extensions.commandreplay.secondary.fetch;

import java.util.Optional;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.apache.isis.applib.util.JaxbUtil;
import org.apache.isis.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.isis.extensions.commandreplay.secondary.StatusException;
import org.apache.isis.extensions.commandreplay.secondary.config.SecondaryConfig;
import org.apache.isis.schema.cmd.v2.CommandsDto;

import lombok.val;

class CommandFetcher_Test {

    @Disabled // intended only for manual verification.
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

        val fetcher = new CommandFetcher();
        fetcher.secondaryConfig = new SecondaryConfig(mmc.getConfiguration());
        fetcher.useRequestDebugLogging = true;

        // when
        CommandsDto entity = fetcher.callPrimary(null);

        System.out.println(JaxbUtil.toXml(entity));
    }
}
