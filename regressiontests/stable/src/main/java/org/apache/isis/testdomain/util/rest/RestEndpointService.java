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
package org.apache.isis.testdomain.util.rest;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;
import javax.xml.bind.JAXBException;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.client.SuppressionType;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.config.RestEasyConfiguration;
import org.apache.isis.core.config.viewer.wicket.WebAppContextPath;
import org.apache.isis.extensions.restclient.ResponseDigest;
import org.apache.isis.extensions.restclient.RestfulClient;
import org.apache.isis.extensions.restclient.RestfulClientConfig;
import org.apache.isis.extensions.restclient.log.ClientConversationFilter;
import org.apache.isis.testdomain.jdo.entities.JdoBook;
import org.apache.isis.testdomain.ldap.LdapConstants;
import org.apache.isis.testdomain.util.dto.BookDto;

import lombok.NonNull;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class RestEndpointService {

    private final Environment environment;
    private final RestEasyConfiguration restEasyConfiguration;
    private final WebAppContextPath webAppContextPath;

    @Inject
    public RestEndpointService(
            final Environment environment,
            final RestEasyConfiguration restEasyConfiguration,
            final WebAppContextPath webAppContextPath) {
        this.environment = environment;
        this.restEasyConfiguration = restEasyConfiguration;
        this.webAppContextPath = webAppContextPath;
    }

    public int getPort() {
        if(port==null) {
            init();
        }
        return port;
    }

    private static final String INVENTORY_RESOURCE = "services/testdomain.jdo.InventoryResource";

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
                        webAppContextPath.prependContextPath(this.restEasyConfiguration.getJaxrs().getDefaultPath())
                );

        log.info("new restful client created for {}", restRootPath);

        val clientConfig = new RestfulClientConfig();
        clientConfig.setRestfulBase(restRootPath);
        // setup basic-auth
        clientConfig.setUseBasicAuth(true); // default = false
        clientConfig.setRestfulAuthUser(LdapConstants.SVEN_PRINCIPAL);
        clientConfig.setRestfulAuthPassword("pass");
        // setup request/response debug logging
        clientConfig.setUseRequestDebugLogging(useRequestDebugLogging);
        // register additional filter if any
        additionalFilters.forEach(clientConfig.getClientConversationFilters()::add);

        //debug
        //clientConfig.setUseRequestDebugLogging(true);

        val client = RestfulClient.ofConfig(clientConfig);

        return client;
    }

    // -- NEW REQUEST BUILDER

    public Invocation.Builder newInvocationBuilder(final RestfulClient client, final String actionPath) {
        return client.request(actionPath, SuppressionType.ALL);
    }

    // -- ENDPOINTS

    public ResponseDigest<JdoBook> getRecommendedBookOfTheWeek(final RestfulClient client) {

        val request = newInvocationBuilder(client, INVENTORY_RESOURCE + "/actions/recommendedBookOfTheWeek/invoke");
        val args = client.arguments()
                .build();

        val response = request.post(args);
        val digest = client.digest(response, JdoBook.class);

        return digest;
    }

    public ResponseDigest<JdoBook> getMultipleBooks(final RestfulClient client) throws JAXBException {

        val request = newInvocationBuilder(client, INVENTORY_RESOURCE + "/actions/multipleBooks/invoke");
        val args = client.arguments()
                .addActionParameter("nrOfBooks", 2)
                .build();

        val response = request.post(args);
        val digest = client.digestList(response, JdoBook.class, new GenericType<List<JdoBook>>() {});

        return digest;
    }


    public ResponseDigest<JdoBook> storeBook(final RestfulClient client, final JdoBook newBook) throws JAXBException {

        val request = newInvocationBuilder(client, INVENTORY_RESOURCE + "/actions/storeBook/invoke");
        val args = client.arguments()
                .addActionParameter("newBook", BookDto.from(newBook).encode())
                .build();

        val response = request.post(args);
        val digest = client.digest(response, JdoBook.class);

        return digest;
    }

    public ResponseDigest<BookDto> getRecommendedBookOfTheWeekAsDto(final RestfulClient client) {

        val request = newInvocationBuilder(client, INVENTORY_RESOURCE + "/actions/recommendedBookOfTheWeekAsDto/invoke");
        val args = client.arguments()
                .build();

        val response = request.post(args);
        val digest = client.digest(response, BookDto.class);

        return digest;
    }

    public ResponseDigest<BookDto> getMultipleBooksAsDto(final RestfulClient client) throws JAXBException {

        val request = newInvocationBuilder(client, INVENTORY_RESOURCE + "/actions/multipleBooksAsDto/invoke");
        val args = client.arguments()
                .addActionParameter("nrOfBooks", 2)
                .build();

        val response = request.post(args);
        val digest = client.digestList(response, BookDto.class, new GenericType<List<BookDto>>() {});

        return digest;
    }


    public ResponseDigest<String> getHttpSessionInfo(final RestfulClient client) {

        val request = newInvocationBuilder(client, INVENTORY_RESOURCE + "/actions/httpSessionInfo/invoke");
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
