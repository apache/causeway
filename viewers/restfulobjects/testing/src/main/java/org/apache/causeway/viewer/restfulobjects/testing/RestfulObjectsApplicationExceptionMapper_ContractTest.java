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
package org.apache.causeway.viewer.restfulobjects.testing;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.applib.util.JsonMapper;
import org.apache.causeway.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;
import org.apache.causeway.viewer.restfulobjects.rendering.exhandling.ExceptionResponseFactory;

/**
 * contract test.
 */
public abstract class RestfulObjectsApplicationExceptionMapper_ContractTest {

    private ExceptionResponseFactory exceptionMapper;

    final HttpHeaders mockHttpHeaders = Mockito.mock(HttpHeaders.class);

    @BeforeEach
    public void setUp() throws Exception {
        exceptionMapper = new ExceptionResponseFactory(List.of());
    }

    @Test
    public void simpleNoMessage() throws Exception {

        // given
        final var status = HttpStatus.BAD_REQUEST;
        final RestfulObjectsApplicationException ex = RestfulObjectsApplicationException.create(status);

        // when
        var response = exceptionMapper.buildResponse(ex, mockHttpHeaders);

        // then
        assertThat(HttpStatus.valueOf(response.getStatusCode().value()), is(status));
        assertThat(response.getHeaders().get("Warning"), is(nullValue()));

        // and then
        final String entity = (String) response.getBody();
        assertThat(entity, is(not(nullValue())));
    }

    @Test
    public void entity_withMessage() throws Exception {

        // givens
        final RestfulObjectsApplicationException ex =
                RestfulObjectsApplicationException.createWithMessage(HttpStatus.BAD_REQUEST, "foobar");

        // when
        var response = exceptionMapper.buildResponse(ex, mockHttpHeaders);

        // then
        assertThat(response.getHeaders().get("Warning").get(0), is("199 RestfulObjects " + ex.getMessage()));

        // and then
        final String entity = (String) response.getBody();
        assertThat(entity, is(not(nullValue())));
    }

    @Test
    public void entity_forException() throws Exception {

        // given
        final Exception exception = new Exception("barfoo");
        final RestfulObjectsApplicationException ex =
                RestfulObjectsApplicationException.createWithCauseAndMessage(HttpStatus.BAD_REQUEST, exception, "foobar");

        // when
        var response = exceptionMapper.buildResponse(ex, mockHttpHeaders);
        final String entity = (String) response.getBody();
        assertThat(entity, is(not(nullValue())));
        final JsonRepresentation jsonRepr = JsonMapper.instance().read(entity, JsonRepresentation.class);

        // then
        assertThat(response.getHeaders().get("Warning").get(0), is("199 RestfulObjects foobar"));
        assertThat(jsonRepr.getString("message"), is("foobar"));
        final JsonRepresentation causedByRepr = jsonRepr.getRepresentation("causedBy");
        assertThat(causedByRepr, is(nullValue()));
    }

    @Test
    public void entity_forExceptionWithCause() throws Exception {

        // given
        var cause = new Exception("barfoo", new Exception("root-cause-message"));
        var ex = RestfulObjectsApplicationException
                .createWithCauseAndMessage(HttpStatus.BAD_REQUEST, cause, "foobar");

        // when
        var response = exceptionMapper.buildResponse(ex, mockHttpHeaders);
        final String entity = (String) response.getBody();
        assertThat(entity, is(not(nullValue())));
        final JsonRepresentation jsonRepr = JsonMapper.instance().read(entity, JsonRepresentation.class);

        // then
        assertThat(response.getHeaders().get("Warning").get(0), is("199 RestfulObjects foobar"));
        assertThat(jsonRepr.getString("message"), is("foobar"));
        var detailJson = jsonRepr.getRepresentation("detail");
        assertThat(detailJson, is(not(nullValue())));
        assertThat(detailJson.getString("message"), is("foobar"));
        var causedByJson = detailJson.getRepresentation("causedBy");
        assertThat(causedByJson, is(not(nullValue())));
        assertThat(causedByJson.getString("message"), is("root-cause-message"));
    }

}
