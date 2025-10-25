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
package org.apache.causeway.applib.layout.grid.bootstrap;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO reflecting the bootstrap grid XML format.
 * @since 4.0 {@index}
 */
@XmlRootElement(name = "grid")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "grid", propOrder = {"rows", "metadataErrors"})
@Data @NoArgsConstructor @AllArgsConstructor
public final class BSGridDto implements BSElement, BSRowOwner {
    private static final long serialVersionUID = 1L;

    @XmlAttribute(required = false)
    private String cssClass;

    @XmlElement(name = "row", required = true)
    private List<BSRow> rows = new ArrayList<>();

    @XmlElement(name = "metadataError", required = false)
    private List<String> metadataErrors = new ArrayList<>();
}
