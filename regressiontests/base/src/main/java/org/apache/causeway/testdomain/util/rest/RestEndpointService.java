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

import org.apache.causeway.viewer.restfulobjects.client.AuthenticationMode;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.client.SuppressionType;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.core.config.RestEasyConfiguration;
import org.apache.causeway.core.config.viewer.web.WebAppContextPath;
import org.apache.causeway.extensions.fullcalendar.applib.value.CalendarEvent;
import org.apache.causeway.extensions.fullcalendar.applib.value.CalendarEventSemantics;
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

        var restRootPath =
                String.format("http://localhost:%d%s/",
                        getPort(),
                        webAppContextPath
                            .prependContextPath(this.restEasyConfiguration.getJaxrs().getDefaultPath())
                );

        log.debug("new restful client created for {}", restRootPath);

        var clientConfig = RestfulClientConfig.builder()
                .restfulBaseUrl(restRootPath)
                // setup basic-auth
                .authenticationMode(AuthenticationMode.BASIC)
                .basicAuthUser(LdapConstants.SVEN_PRINCIPAL)
                .basicAuthPassword("pass")
                // setup request/response debug logging
                .useRequestDebugLogging(useRequestDebugLogging)
                // register additional filter if any
                .clientConversationFilters(additionalFilters.toList())
                .build();

        var client = RestfulClient.ofConfig(clientConfig);
        return client;
    }

    // -- NEW REQUEST BUILDER

    public Invocation.Builder newInvocationBuilder(final RestfulClient client, final String endpointPath) {
        return client.request(endpointPath)
                .accept(RestfulClientMediaType.SIMPLE_JSON.mediaTypeFor(Object.class, SuppressionType.all()));
    }

    // -- ENDPOINTS

    public Try<JdoBook> getRecommendedBookOfTheWeek(final RestfulClient client) {

        var request = newInvocationBuilder(client,
                INVENTORY_RESOURCE + "/actions/recommendedBookOfTheWeek/invoke");
        var args = client.arguments()
                .build();

        var response = request.post(args);
        var digest = client.digest(response, JdoBook.class);

        return digest;
    }

    public Try<BookDto> getRecommendedBookOfTheWeekDto(final RestfulClient client) {

        var request = newInvocationBuilder(client,
                INVENTORY_RESOURCE + "/actions/recommendedBookOfTheWeekDto/invoke");
        var args = client.arguments()
                .build();

        var response = request.post(args);
        var digest = client.digest(response, BookDto.class);

        return digest;
    }

    public Try<Can<JdoBook>> getMultipleBooks(final RestfulClient client) throws JAXBException {

        var request = newInvocationBuilder(client,
                INVENTORY_RESOURCE + "/actions/multipleBooks/invoke");
        var args = client.arguments()
                .addActionParameter("nrOfBooks", 2)
                .build();

        var response = request.post(args);
        var digest = client.digestList(response, JdoBook.class, new GenericType<List<JdoBook>>() {});

        return digest;
    }

    public Try<JdoBook> storeBook(final RestfulClient client, final JdoBook newBook) throws JAXBException {

        var request = newInvocationBuilder(client,
                INVENTORY_RESOURCE + "/actions/storeBook/invoke");
        var args = client.arguments()
                .addActionParameter("newBook", BookDto.from(newBook).encode())
                .build();

        var response = request.post(args);
        var digest = client.digest(response, JdoBook.class);

        return digest;
    }

    public Try<BookDto> getRecommendedBookOfTheWeekAsDto(final RestfulClient client) {

        var request = newInvocationBuilder(client,
                INVENTORY_RESOURCE + "/actions/recommendedBookOfTheWeekAsDto/invoke");
        var args = client.arguments()
                .build();

        var response = request.post(args);
        var digest = client.digest(response, BookDto.class);

        return digest;
    }

    public Try<Can<BookDto>> getMultipleBooksAsDto(final RestfulClient client) throws JAXBException {

        var request = newInvocationBuilder(client,
                INVENTORY_RESOURCE + "/actions/multipleBooksAsDto/invoke");
        var args = client.arguments()
                .addActionParameter("nrOfBooks", 2)
                .build();

        var response = request.post(args);
        var digest = client.digestList(response, BookDto.class, new GenericType<List<BookDto>>() {});

        return digest;
    }

    public Try<JdoInventoryJaxbVm> getInventoryAsJaxbVm(final RestfulClient client) {

        var request = newInvocationBuilder(client,
                INVENTORY_RESOURCE + "/actions/inventoryAsJaxbVm/invoke");
        var args = client.arguments()
                .build();

        var response = request.post(args);
        var digest = client.digest(response, JdoInventoryJaxbVm.class);
        return digest;
    }

    public Try<Can<JdoBook>> getBooksFromInventoryAsJaxbVm(final RestfulClient client) {

        var objectId = interactionService.callAnonymous(
                ()->jdoTestFixtures.getInventoryJaxbVmAsBookmark().getIdentifier());

        // using domain object alias ...
        var request = newInvocationBuilder(client,
                "objects/testdomain.jdo.JdoInventoryJaxbVmAlias/"
                        + objectId + "/actions/listBooks/invoke");

        var args = client.arguments()
                .build();

        var response = request.post(args);
        var digest = client.digestList(response, JdoBook.class, new GenericType<List<JdoBook>>() {});

        return digest;
    }

    public Try<CalendarEvent> echoCalendarEvent(
            final RestfulClient client, final CalendarEvent calendarEvent) {

        var calSemantics = new CalendarEventSemantics();

        var request = newInvocationBuilder(client,
                INVENTORY_RESOURCE + "/actions/echoCalendarEvent/invoke");
        var args = client.arguments()
                .addActionParameter("calendarEvent", calSemantics.decompose(calendarEvent))
                .build();

        var response = request.post(args);
        var digest = client.digestValue(response, calSemantics);

        return digest;
    }

    public Try<String> getHttpSessionInfo(final RestfulClient client) {

        var request = newInvocationBuilder(client,
                INVENTORY_RESOURCE + "/actions/httpSessionInfo/invoke");
        var args = client.arguments()
                .build();

        var response = request.post(args);
        var digest = client.digest(response, String.class);

        return digest;
    }

    // -- HELPER

    private Integer port;

    private void init() {
        // spring embedded web server port
        port = Integer.parseInt(environment.getProperty("local.server.port"));
    }

}
