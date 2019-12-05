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
package org.apache.isis.viewer.restfulobjects.viewer.mappers.entity;

import java.util.List;

import javax.jdo.JDOException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.commons.internal.collections._Lists;

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
public class ExceptionDetail {

    private static String format(final StackTraceElement stackTraceElement) {
        return stackTraceElement.toString();
    }

    private String className;
    private String message;

    @XmlElementWrapper()
    @XmlElement(name="element")
    private List<String> stackTrace = _Lists.newArrayList();
    private ExceptionDetail causedBy;

    public ExceptionDetail() {
    }
    public ExceptionDetail(final Throwable ex) {
        this.className = ex.getClass().getName();
        this.message = ex.getMessage();
        final StackTraceElement[] stackTraceElements = ex.getStackTrace();
        for (final StackTraceElement stackTraceElement : stackTraceElements) {
            this.stackTrace.add(format(stackTraceElement));
        }

        final Throwable cause = causeOf(ex);
        if (cause != null && cause != ex) {
            this.causedBy = new ExceptionDetail(cause);
        }
    }

    private static Throwable causeOf(Throwable ex) {
        if (ex instanceof JDOException) {
            final JDOException jdoException = (JDOException) ex;
            final Throwable[] nestedExceptions = jdoException.getNestedExceptions();
            return nestedExceptions != null && nestedExceptions.length > 0? nestedExceptions[0]: null;
        }
        else {
            return ex.getCause();
        }
    }

}
