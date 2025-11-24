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
package org.apache.causeway.testdomain.jpa.rest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.xml.bind.JAXBException;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.Builder;
import org.springframework.web.client.RestClient.RequestBodySpec;
import org.springframework.web.client.RestClient.RequestBodyUriSpec;

import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.value.semantics.ValueDecomposition;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.base._Bytes;
import org.apache.causeway.commons.io.JsonUtils;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.applib.RestfulPathProvider;
import org.apache.causeway.core.config.viewer.web.WebAppContextPath;
import org.apache.causeway.extensions.fullcalendar.applib.value.CalendarEvent;
import org.apache.causeway.extensions.fullcalendar.applib.value.CalendarEventSemantics;
import org.apache.causeway.schema.common.v2.ValueType;
import org.apache.causeway.testdomain.jpa.JpaInventoryJaxbVm;
import org.apache.causeway.testdomain.jpa.JpaTestFixtures;
import org.apache.causeway.testdomain.jpa.entities.JpaBook;
import org.apache.causeway.testdomain.ldap.LdapConstants;
import org.apache.causeway.testdomain.util.dto.BookDto;
import org.apache.causeway.viewer.restfulobjects.applib.client.ActionParameterModel;
import org.apache.causeway.viewer.restfulobjects.applib.client.CausewayMediaTypes;
import org.apache.causeway.viewer.restfulobjects.applib.client.ConversationLogger;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import tools.jackson.databind.DeserializationFeature;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class JpaRestEndpointService {

    private final Environment environment;
    private final CausewayConfiguration causewayConfiguration;
    private final WebAppContextPath webAppContextPath;
    private final JpaTestFixtures jpaTestFixtures;
    private final InteractionService interactionService;

    public int getPort() {
        if(port==null) {
            init();
        }
        return port;
    }

    private static final String INVENTORY_RESOURCE = "services/testdomain.jpa.InventoryResource";

    // -- NEW CLIENT

    @Getter(lazy = true) @Accessors(fluent=true)
    private final String baseUrl = "http://0.0.0.0:%d%s/".formatted(getPort(), webAppContextPath
            .prependContextPath(new RestfulPathProvider(causewayConfiguration).getRestfulPath().orElse("")));

    record ValueHolder(String type, Object value) {
        ValueDecomposition parseValueDecomposition() {
            return ValueDecomposition.destringify(ValueType.COMPOSITE, (String)value);
        }
        @SuppressWarnings("unchecked")
        <T> T value(final Class<T> requiredType){
            return (T) value;
        }
    }
    record CausewayMessageConverter() implements HttpMessageConverter<Object> {

        @Override
        public boolean canRead(final Class<?> clazz, @Nullable final MediaType mediaType) {
            return clazz.equals(ValueDecomposition.class);
        }

        @Override
        public boolean canWrite(final Class<?> clazz, @Nullable final MediaType mediaType) {
            return false;
        }

        @Override
        public List<MediaType> getSupportedMediaTypes() {
            return List.of(MediaType.APPLICATION_JSON);
        }

        @Override
        public Object read(final Class<? extends Object> clazz, final HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
            var bytes = _Bytes.of(inputMessage.getBody());
            var json = new String(bytes, StandardCharsets.UTF_8);
            var valueHolder = JsonUtils.tryRead(ValueHolder.class, json)
                .valueAsNonNullElseFail();
            return ValueDecomposition.destringify(ValueType.COMPOSITE, valueHolder.value(String.class));
        }

        @Override
        public void write(final Object t, @Nullable final MediaType contentType, final HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
            // TODO Auto-generated method stub

        }

    }

    protected Builder restClient() {
        return RestClient.builder()
            .messageConverters(converters->converters.add(0, new CausewayMessageConverter()))
            .baseUrl(baseUrl())
            .defaultHeaders(headers -> headers.setBasicAuth(LdapConstants.SVEN_PRINCIPAL, "pass"));
    }
    protected Builder restClient(final Logger logger) {
        return restClient()
            .bufferContent((uri, method)->true)
            .requestInterceptor(new ConversationLogger(msg->logger.info(msg)));
    }
    protected ActionParameterModel actParamModel() {
        return ActionParameterModel.create(baseUrl());
    }

    public RestClient newClient(final boolean useRequestDebugLogging) {
        log.debug("new restful client created for {}", baseUrl());
        return useRequestDebugLogging
            ? restClient(log).build()
            : restClient().build();
    }

    // -- NEW REQUEST BUILDER

    public RequestBodySpec request(final RequestBodyUriSpec requestBodyUriSpec, final String uri,
        final ActionParameterModel actParamModel) {
        return requestBodyUriSpec
            .uri(INVENTORY_RESOURCE + uri)
            .accept(CausewayMediaTypes.CAUSEWAY_JSON_V2_LIGHT)
            .body(actParamModel.toJson());
    }

    // -- ENDPOINTS

    public Try<JpaBook> getRecommendedBookOfTheWeek(final RestClient client) {

        var response = request(client.post(), "/actions/recommendedBookOfTheWeek/invoke", actParamModel())
            .retrieve();

        var entity = response.body(JpaBook.class);
        return Try.success(entity);
    }

    public Try<BookDto> getRecommendedBookOfTheWeekDto(final RestClient client) {
        var response = request(client.post(), "/actions/recommendedBookOfTheWeekDto/invoke", actParamModel())
            .retrieve();

        var entity = response.body(BookDto.class);
        return Try.success(entity);
    }

    public Try<Can<JpaBook>> getMultipleBooks(final RestClient client) throws JAXBException {
        var response = request(client.post(),"/actions/multipleBooks/invoke", actParamModel()
                .addActionParameter("nrOfBooks", 3))
            .retrieve();

        List<JpaBook> books = response
            .body(new ParameterizedTypeReference<List<JpaBook>>() {});

        return Try.success(Can.ofCollection(books));
    }

    public Try<JpaBook> storeBook(final RestClient client, final JpaBook newBook) throws JAXBException {
        var response = request(client.post(), "/actions/storeBook/invoke", actParamModel()
            .addActionParameter("newBook", BookDto.from(newBook).encode()))
            .retrieve();

        var entity = response.body(JpaBook.class);
        return Try.success(entity);
    }

    public Try<BookDto> getRecommendedBookOfTheWeekAsDto(final RestClient client) {
        var response = request(client.post(), "/actions/recommendedBookOfTheWeekAsDto/invoke", actParamModel())
            .retrieve();

        var entity = response.body(BookDto.class);
        return Try.success(entity);
    }

    public Try<Can<BookDto>> getMultipleBooksAsDto(final RestClient client) throws JAXBException {
        var response = request(client.post(), "/actions/multipleBooksAsDto/invoke", actParamModel()
            .addActionParameter("nrOfBooks", 2))
            .retrieve();

        List<BookDto> books = response
            .body(new ParameterizedTypeReference<List<BookDto>>() {});

        return Try.success(Can.ofCollection(books));
    }

    public Try<JpaInventoryJaxbVm> getInventoryAsJaxbVm(final RestClient client) {
        var response = request(client.post(), "/actions/inventoryAsJaxbVm/invoke", actParamModel())
            .retrieve();

        return JsonUtils.tryRead(JpaInventoryJaxbVm.class, response.body(String.class),
            JsonUtils::jaxbAnnotationSupport,
            m->m.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES),
            m->m.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
         //var entity = response.body(JpaInventoryJaxbVm.class);
        //return Try.success(entity);
    }

    public Try<Can<JpaBook>> getBooksFromInventoryAsJaxbVm(final RestClient client) {
        var objectId = interactionService.callAnonymous(
                ()->jpaTestFixtures.getInventoryJaxbVmAsBookmark().identifier());

        // using domain object alias ...
        var response = client
            .post()
            .uri("objects/testdomain.jpa.JpaInventoryJaxbVmAlias/%s/actions/listBooks/invoke"
                .formatted(objectId))
            .accept(CausewayMediaTypes.CAUSEWAY_JSON_V2_LIGHT)
            .body(actParamModel().toJson())
            .retrieve();

        List<JpaBook> books = response
            .body(new ParameterizedTypeReference<List<JpaBook>>() {});

        return Try.success(Can.ofCollection(books));
    }

    public Try<CalendarEvent> echoCalendarEvent(
            final RestClient client, final CalendarEvent calendarEvent) {
        var calSemantics = new CalendarEventSemantics();
        var response = request(client.post(),"/actions/echoCalendarEvent/invoke", actParamModel()
            .addActionParameter("calendarEvent", calSemantics.decompose(calendarEvent)))
            .retrieve();
        var entity = response.body(ValueDecomposition.class);

        var calendarEventEcho = calSemantics.compose(entity);
        return Try.success(calendarEventEcho);
    }

    public Try<String> getHttpSessionInfo(final RestClient client) {
        var args = actParamModel();
        var response = request(client.post(), "/actions/httpSessionInfo/invoke", args)
            .retrieve();

        var entity = response.body(ValueHolder.class);
        return Try.success((String)entity.value());
    }

    // -- HELPER

    private Integer port;

    private void init() {
        // spring embedded web server port
        port = Integer.parseInt(environment.getProperty("local.server.port"));
    }

}
