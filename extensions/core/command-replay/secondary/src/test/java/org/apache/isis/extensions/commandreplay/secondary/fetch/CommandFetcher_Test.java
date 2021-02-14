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

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.apache.isis.applib.services.jaxb.JaxbService.Simple;
import org.apache.isis.applib.util.JaxbUtil;
import org.apache.isis.extensions.jaxrsclient.applib.client.JaxRsClient;
import org.apache.isis.extensions.jaxrsclient.applib.client.JaxRsResponse;
import org.apache.isis.extensions.jaxrsclient.impl.client.JaxRsClientDefault;
import org.apache.isis.schema.cmd.v2.CommandsDto;

import lombok.val;


public class CommandFetcher_Test {

    @Disabled // intended only for manual verification.
    @Test
    public void testing_the_unmarshalling() {
        val jaxRsClient = new JaxRsClientDefault();
        final UriBuilder uriBuilder = UriBuilder.fromUri(
                        String.format(
                        "%s%s?batchSize=%d",
                        "http://localhost:8080/restful/", CommandFetcher.URL_SUFFIX, 10)
        );
        URI uri = uriBuilder.build();
        JaxRsResponse invoke = jaxRsClient.get(uri, CommandsDto.class, JaxRsClient.ReprType.ACTION_RESULT, "sven", "pass");
        CommandsDto entity = invoke.readEntity(CommandsDto.class);
        System.out.println(JaxbUtil.toXml(entity));
    }
}
