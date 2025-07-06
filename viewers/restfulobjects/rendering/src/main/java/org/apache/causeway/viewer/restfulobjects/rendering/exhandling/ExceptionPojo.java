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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import lombok.Getter;

@XmlRootElement(
        name = "exception"
        )
@XmlType(
        name = "exception"
        , propOrder = {
                "httpStatusCode",
                "message",
                "detail",
        }
        )
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
public class ExceptionPojo {

    private int httpStatusCode;
    private String message;
    private ExceptionDetail detail;

    public ExceptionPojo() { }

    public ExceptionPojo(final int statusCode, final String message, final ExceptionDetail detail) {
        this.httpStatusCode = statusCode;
        this.message = message;
        this.detail = detail;
    }

}
