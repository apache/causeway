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
package org.apache.causeway.viewer.restfulobjects.viewer.mappers;

//import jakarta.inject.Singleton;
//import jakarta.ws.rs.core.Response;
//import jakarta.ws.rs.ext.Provider;

import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

//import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.exceptions.unrecoverable.ObjectNotFoundException;

import lombok.extern.slf4j.Slf4j;
//import org.apache.causeway.viewer.restfulobjects.applib.RestfulResponse;

//@Component
//@Provider
//@Singleton
//@jakarta.annotation.Priority(PriorityPrecedence.MIDPOINT)
@RestControllerAdvice
@Slf4j
public class ExceptionMapperForObjectNotFound
//extends ExceptionMapperAbstract<ObjectNotFoundException>
{

    //TODO[causeway-viewer-restfulobjects-viewer-CAUSEWAY-3892] convert
//    @Override
//    public Response toResponse(final ObjectNotFoundException ex) {
//        return buildResponse(ex, RestfulResponse.HttpStatusCode.NOT_FOUND);
//    }

    @ExceptionHandler(ObjectNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String objectNotFoundHandler(ObjectNotFoundException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String fallback(Exception ex) {
        log.error("not covered by any @ExceptionHandler", ex);
        return "[fallback] " + ex.getMessage();
    }

}
