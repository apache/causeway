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
package org.apache.causeway.viewer.restfulobjects.rendering.exhandling;

import java.util.List;
import java.util.stream.Stream;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.services.exceprecog.RootCauseFinder;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.exceptions._Exceptions;

import lombok.Getter;
import lombok.NoArgsConstructor;

@XmlRootElement(
        name = "exceptionDetail"
        )
@XmlType(
        name = "exceptionDetail"
        , propOrder = {
                "className",
                "message",
                "stackTrace",
                "causedBy"
        }
        )
@XmlAccessorType(XmlAccessType.FIELD)
@NoArgsConstructor
public class ExceptionDetail {

    @Getter private String className;
    @Getter private String message;

    @XmlElementWrapper()
    @XmlElement(name="element")
    private List<String> stackTrace = _Lists.newArrayList();

    @Getter private ExceptionDetail causedBy;

    public ExceptionDetail(
            final Throwable ex,
            final @Nullable List<RootCauseFinder> rootCauseFinders) {
        this.className = ex.getClass().getName();
        this.message = ex.getMessage();

        Stream.of(ex.getStackTrace())
            .map(ExceptionDetail::format)
            .forEach(stackTrace::add);

        this.causedBy = _Exceptions.getRootCause(ex, rootCauseFinders)
            .filter(cause->cause!=ex)
            .map(cause->new ExceptionDetail(cause, rootCauseFinders))
            .orElse(null);
    }

    // -- HELPER

    private static String format(final StackTraceElement stackTraceElement) {
        return stackTraceElement.toString();
    }

}
