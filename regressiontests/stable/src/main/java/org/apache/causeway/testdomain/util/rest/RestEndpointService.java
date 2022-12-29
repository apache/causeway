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
package org.apache.causeway.testdomain.util.rest;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;
import javax.xml.bind.JAXBException;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.client.SuppressionType;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.core.config.RestEasyConfiguration;
import org.apache.causeway.core.config.viewer.web.WebAppContextPath;
import org.apache.causeway.testdomain.jdo.JdoInventoryJaxbVm;
import org.apache.causeway.testdomain.jdo.JdoTestFixtures;
import org.apache.causeway.testdomain.jdo.entities.JdoBook;
import org.apache.causeway.testdomain.ldap.LdapConstants;
import org.apache.causeway.testdomain.util.dto.BookDto;
import org.apache.causeway.viewer.restfulobjects.client.RestfulClient;
import org.apache.causeway.viewer.restfulobjects.client.RestfulClientConfig;
import org.apache.causeway.viewer.restfulobjects.client.RestfulClientMediaType;
import org.apache.causeway.viewer.restfulobjects.client.log.ClientConversationFilter;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class RestEndpointService {

    private final Environment environment;
    private final RestEasyConfiguration restEasyConfiguration;
    private final WebAppContextPath webAppContextPath;
    private final JdoTestFixtures jdoTestFixtures;
    private final InteractionService interactionService;

    public int getPort() {
        if(port==null) {
            init();
        }
        return port;
    }

    private static final String INVENTORY_RESOURCE = "services/testdomain.jdo.InventoryResourceAlias";

    // -- NEW CLIENT

    public RestfulClient newClient(final boolean useRequestDebugLogging) {
        return newClient(useRequestDebugLogging, Can.empty());
    }


    public RestfulClient newClient(
            final boolean useRequestDebugLogging,
            final @NonNull Can<ClientConversationFilter> additionalFilters) {

        val restRootPath =
                String.format("http://localhost:%d%s/",
                        getPort(),
                        webAppContextPath
                            .prependContextPath(this.restEasyConfiguration.getJaxrs().getDefaultPath())
                );

        log.debug("new restful client created for {}", restRootPath);

        val clientConfig = RestfulClientConfig.builder()
                .restfulBase(restRootPath)
                // setup basic-auth
                .useBasicAuth(true)
                .restfulAuthUser(LdapConstants.SVEN_PRINCIPAL)
                .restfulAuthPassword("pass")
                // setup request/response debug logging
                .useRequestDebugLogging(useRequestDebugLogging)
                // register additional filter if any
                .clientConversationFilters(additionalFilters.toList())
                .build();

        val client = RestfulClient.ofConfig(clientConfig);
        return client;
    }

    // -- NEW REQUEST BUILDER

    public Invocation.Builder newInvocationBuilder(final RestfulClient client, final String endpointPath) {
        return client.request(endpointPath)
                .accept(RestfulClientMediaType.SIMPLE_JSON.mediaTypeFor(Object.class, SuppressionType.all()));
    }

    // -- ENDPOINTS

    public Try<JdoBook> getRecommendedBookOfTheWeek(final RestfulClient client) {

        val request = newInvocationBuilder(client,
                INVENTORY_RESOURCE + "/actions/recommendedBookOfTheWeek/invoke");
        val args = client.arguments()
                .build();

        val response = request.post(args);
        val digest = client.digest(response, JdoBook.class);

        return digest;
    }

    public Try<BookDto> getRecommendedBookOfTheWeekDto(final RestfulClient client) {

        val request = newInvocationBuilder(client,
                INVENTORY_RESOURCE + "/actions/recommendedBookOfTheWeekDto/invoke");
        val args = client.arguments()
                .build();

        val response = request.post(args);
        val digest = client.digest(response, BookDto.class);

        return digest;
    }

    public Try<Can<JdoBook>> getMultipleBooks(final RestfulClient client) throws JAXBException {

        val request = newInvocationBuilder(client,
                INVENTORY_RESOURCE + "/actions/multipleBooks/invoke");
        val args = client.arguments()
                .addActionParameter("nrOfBooks", 2)
                .build();

        val response = request.post(args);
        val digest = client.digestList(response, JdoBook.class, new GenericType<List<JdoBook>>() {});

        return digest;
    }


    public Try<JdoBook> storeBook(final RestfulClient client, final JdoBook newBook) throws JAXBException {

        val request = newInvocationBuilder(client,
                INVENTORY_RESOURCE + "/actions/storeBook/invoke");
        val args = client.arguments()
                .addActionParameter("newBook", BookDto.from(newBook).encode())
                .build();

        val response = request.post(args);
        val digest = client.digest(response, JdoBook.class);

        return digest;
    }

    public Try<BookDto> getRecommendedBookOfTheWeekAsDto(final RestfulClient client) {

        val request = newInvocationBuilder(client,
                INVENTORY_RESOURCE + "/actions/recommendedBookOfTheWeekAsDto/invoke");
        val args = client.arguments()
                .build();

        val response = request.post(args);
        val digest = client.digest(response, BookDto.class);

        return digest;
    }

    public Try<Can<BookDto>> getMultipleBooksAsDto(final RestfulClient client) throws JAXBException {

        val request = newInvocationBuilder(client,
                INVENTORY_RESOURCE + "/actions/multipleBooksAsDto/invoke");
        val args = client.arguments()
                .addActionParameter("nrOfBooks", 2)
                .build();

        val response = request.post(args);
        val digest = client.digestList(response, BookDto.class, new GenericType<List<BookDto>>() {});

        return digest;
    }

    public Try<JdoInventoryJaxbVm> getInventoryAsJaxbVm(final RestfulClient client) {

        val request = newInvocationBuilder(client,
                INVENTORY_RESOURCE + "/actions/inventoryAsJaxbVm/invoke");
        val args = client.arguments()
                .build();

        val response = request.post(args);
        val digest = client.digest(response, JdoInventoryJaxbVm.class);
        return digest;
    }

    public Try<Can<JdoBook>> getBooksFromInventoryAsJaxbVm(final RestfulClient client) {

        val objectId = interactionService.callAnonymous(
                ()->jdoTestFixtures.getJdoInventoryJaxbVmAsBookmark().getIdentifier());

        // using domain object alias ...
        val request = newInvocationBuilder(client,
                "objects/testdomain.jdo.JdoInventoryJaxbVmAlias/"
                        + objectId + "/actions/listBooks/invoke");

        val args = client.arguments()
                .build();

        val response = request.post(args);
        val digest = client.digestList(response, JdoBook.class, new GenericType<List<JdoBook>>() {});

        return digest;
    }

    public Try<String> getHttpSessionInfo(final RestfulClient client) {

        val request = newInvocationBuilder(client,
                INVENTORY_RESOURCE + "/actions/httpSessionInfo/invoke");
        val args = client.arguments()
                .build();

        val response = request.post(args);
        val digest = client.digest(response, String.class);

        return digest;
    }

    // -- HELPER

    private Integer port;

    private void init() {
        // spring embedded web server port
        port = Integer.parseInt(environment.getProperty("local.server.port"));
    }

}
